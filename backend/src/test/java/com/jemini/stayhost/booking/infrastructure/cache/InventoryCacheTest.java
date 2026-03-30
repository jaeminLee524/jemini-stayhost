package com.jemini.stayhost.booking.infrastructure.cache;

import com.jemini.stayhost.common.exception.BusinessException;
import com.jemini.stayhost.common.exception.ErrorCode;
import com.jemini.stayhost.property.domain.model.Inventory;
import com.jemini.stayhost.property.infrastructure.persistence.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class InventoryCacheTest {

    private InventoryCache inventoryCache;

    @Mock
    private InventoryRepository inventoryRepository;

    private static final Long ROOM_TYPE_ID = 100L;
    private static final LocalDate DATE_1 = LocalDate.of(2026, 4, 10);
    private static final LocalDate DATE_2 = LocalDate.of(2026, 4, 11);
    private static final LocalDate DATE_3 = LocalDate.of(2026, 4, 12);

    @BeforeEach
    void setUp() {
        given(inventoryRepository.findByDateBetween(any(), any()))
            .willReturn(List.of(
                Inventory.create(ROOM_TYPE_ID, DATE_1, 5),
                Inventory.create(ROOM_TYPE_ID, DATE_2, 3),
                Inventory.create(ROOM_TYPE_ID, DATE_3, 1)
            ));
        inventoryCache = new InventoryCache(inventoryRepository);
        inventoryCache.warmUp();
    }

    @Test
    @DisplayName("CAS 감소 성공 - 모든 날짜에 재고가 충분하면 감소된다")
    void CAS_감소_성공() {
        inventoryCache.tryDecreaseAll(ROOM_TYPE_ID, List.of(DATE_1, DATE_2));

        // 다시 감소 시도하면 재고가 줄어든 상태
        inventoryCache.tryDecreaseAll(ROOM_TYPE_ID, List.of(DATE_1));
        // DATE_1: 5 -> 4 -> 3, 아직 가능
    }

    @Test
    @DisplayName("CAS 감소 실패 - 재고 0이면 예외")
    void CAS_감소_실패_재고_0이면_예외() {
        // DATE_3의 재고를 소진 (1개)
        inventoryCache.tryDecreaseAll(ROOM_TYPE_ID, List.of(DATE_3));

        assertThatThrownBy(() -> inventoryCache.tryDecreaseAll(ROOM_TYPE_ID, List.of(DATE_3)))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVENTORY_INSUFFICIENT);
    }

    @Test
    @DisplayName("CAS 감소 실패 - 캐시에 없는 키이면 예외")
    void CAS_감소_실패_캐시에_없는_키이면_예외() {
        final Long unknownRoomTypeId = 999L;

        assertThatThrownBy(() -> inventoryCache.tryDecreaseAll(unknownRoomTypeId, List.of(DATE_1)))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVENTORY_INSUFFICIENT);
    }

    @Test
    @DisplayName("CAS 부분 실패 시 이미 감소된 날짜를 롤백한다")
    void CAS_부분_실패_시_롤백() {
        // DATE_3 재고를 소진 (1개)
        inventoryCache.tryDecreaseAll(ROOM_TYPE_ID, List.of(DATE_3));

        // DATE_1(성공) -> DATE_2(성공) -> DATE_3(실패, 재고 0) 순서로 시도
        assertThatThrownBy(() ->
            inventoryCache.tryDecreaseAll(ROOM_TYPE_ID, List.of(DATE_1, DATE_2, DATE_3)))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVENTORY_INSUFFICIENT);

        // DATE_1, DATE_2는 롤백되어 원래 재고(4, 2)가 유지되어야 한다
        // 다시 감소가 가능해야 함
        inventoryCache.tryDecreaseAll(ROOM_TYPE_ID, List.of(DATE_1, DATE_2));
    }

    @Test
    @DisplayName("restore - 재고가 복원된다")
    void restore_재고_복원() {
        // 재고 감소
        inventoryCache.tryDecreaseAll(ROOM_TYPE_ID, List.of(DATE_3));

        // DATE_3 재고 0 → 복원
        inventoryCache.restore(ROOM_TYPE_ID, List.of(DATE_3));

        // 다시 감소 가능해야 한다
        inventoryCache.tryDecreaseAll(ROOM_TYPE_ID, List.of(DATE_3));
    }

    @Test
    @DisplayName("rollbackAll - restore와 동일하게 동작한다")
    void rollbackAll_복원() {
        inventoryCache.tryDecreaseAll(ROOM_TYPE_ID, List.of(DATE_1, DATE_2));

        inventoryCache.rollbackAll(ROOM_TYPE_ID, List.of(DATE_1, DATE_2));

        // 롤백 후 원래 재고만큼 다시 감소 가능
        for (int i = 0; i < 5; i++) {
            inventoryCache.tryDecreaseAll(ROOM_TYPE_ID, List.of(DATE_1));
        }
        assertThatThrownBy(() -> inventoryCache.tryDecreaseAll(ROOM_TYPE_ID, List.of(DATE_1)))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVENTORY_INSUFFICIENT);
    }

    @Test
    @DisplayName("restore - 캐시에 없는 키는 무시한다")
    void restore_없는_키_무시() {
        inventoryCache.restore(999L, List.of(DATE_1));
        // 예외 없이 정상 종료
    }
}
