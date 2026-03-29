package com.jemini.stayhost.property.domain.model;

import com.jemini.stayhost.common.exception.BusinessException;
import com.jemini.stayhost.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoomTypeTest {

    @Test
    @DisplayName("객실유형 생성 성공 - ACTIVE 상태")
    void 객실유형_생성_성공_ACTIVE_상태() {
        final RoomType roomType = createRoomType(2);

        assertThat(roomType.getStatus()).isEqualTo(RoomTypeStatus.ACTIVE);
        assertThat(roomType.getName()).isEqualTo("스탠다드 더블");
        assertThat(roomType.getMaxOccupancy()).isEqualTo(2);
    }

    @Test
    @DisplayName("객실유형 수정 성공")
    void 객실유형_수정_성공() {
        final RoomType roomType = createRoomType(2);

        roomType.update("디럭스 더블", "업그레이드", 3, BigDecimal.valueOf(200000), null);

        assertThat(roomType.getName()).isEqualTo("디럭스 더블");
        assertThat(roomType.getDescription()).isEqualTo("업그레이드");
        assertThat(roomType.getMaxOccupancy()).isEqualTo(3);
        assertThat(roomType.getBasePrice()).isEqualByComparingTo(BigDecimal.valueOf(200000));
    }

    @Test
    @DisplayName("투숙인원 검증 - 초과시 예외")
    void 투숙인원_검증_초과시_예외() {
        final RoomType roomType = createRoomType(2);

        assertThatThrownBy(() -> roomType.validateGuestCount(3))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_GUEST_COUNT);
    }

    @Test
    @DisplayName("투숙인원 검증 - 이하이면 통과")
    void 투숙인원_검증_이하이면_통과() {
        final RoomType roomType = createRoomType(2);

        roomType.validateGuestCount(1);
    }

    @Test
    @DisplayName("투숙인원 검증 - 최대치와 동일하면 통과")
    void 투숙인원_검증_최대치와_동일하면_통과() {
        final RoomType roomType = createRoomType(2);

        roomType.validateGuestCount(2);
    }

    private RoomType createRoomType(final int maxOccupancy) {
        return RoomType.create(1L, "스탠다드 더블", "설명", maxOccupancy, BigDecimal.valueOf(120000), null, 10);
    }
}
