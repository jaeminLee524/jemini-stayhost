package com.jemini.stayhost.property.domain.model;

import com.jemini.stayhost.common.exception.BusinessException;
import com.jemini.stayhost.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryTest {

    @Test
    @DisplayName("재고 생성 성공 - 예약수 0")
    void 재고_생성_성공_예약수_0() {
        final Inventory inventory = createInventory(10);

        assertThat(inventory.getTotalCount()).isEqualTo(10);
        assertThat(inventory.getReservedCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("가용재고 계산 성공")
    void 가용재고_계산_성공() {
        final Inventory inventory = createInventory(10);
        inventory.decreaseStock();
        inventory.decreaseStock();

        assertThat(inventory.getTotalCount()).isEqualTo(10);
        assertThat(inventory.getAvailableCount()).isEqualTo(8);
    }

    @Test
    @DisplayName("재고차감 성공")
    void 재고차감_성공() {
        final Inventory inventory = createInventory(10);

        inventory.decreaseStock();

        assertThat(inventory.getTotalCount()).isEqualTo(10);
        assertThat(inventory.getReservedCount()).isEqualTo(1);
        assertThat(inventory.getAvailableCount()).isEqualTo(9);
    }

    @Test
    @DisplayName("재고차감 - 가용재고 0이면 예외")
    void 재고차감_가용재고_0이면_예외() {
        final Inventory inventory = createInventory(1);
        inventory.decreaseStock();

        assertThatThrownBy(inventory::decreaseStock)
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVENTORY_INSUFFICIENT);
    }

    @Test
    @DisplayName("재고차감 - 가용재고 1일때 성공 후 0")
    void 재고차감_가용재고_1일때_성공후_0() {
        final Inventory inventory = createInventory(1);

        inventory.decreaseStock();

        assertThat(inventory.getAvailableCount()).isEqualTo(0);
        assertThat(inventory.getReservedCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("재고복원 성공")
    void 재고복원_성공() {
        final Inventory inventory = createInventory(10);
        inventory.decreaseStock();
        inventory.decreaseStock();

        inventory.increaseStock();

        assertThat(inventory.getReservedCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("재고복원 - 예약수 0이면 무시")
    void 재고복원_예약수_0이면_무시() {
        final Inventory inventory = createInventory(10);

        inventory.increaseStock();

        assertThat(inventory.getReservedCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("총재고 수정 성공")
    void 총재고_수정_성공() {
        final Inventory inventory = createInventory(10);

        inventory.updateTotalCount(20);

        assertThat(inventory.getTotalCount()).isEqualTo(20);
    }

    @Test
    @DisplayName("총재고 수정 - 예약수보다 적으면 예외")
    void 총재고_수정_예약수보다_적으면_예외() {
        final Inventory inventory = createInventory(10);
        inventory.decreaseStock();
        inventory.decreaseStock();
        inventory.decreaseStock();

        assertThatThrownBy(() -> inventory.updateTotalCount(2))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVENTORY_TOTAL_BELOW_RESERVED);
    }

    @Test
    @DisplayName("총재고 수정 - 예약수와 동일하면 성공")
    void 총재고_수정_예약수와_동일하면_성공() {
        final Inventory inventory = createInventory(10);
        inventory.decreaseStock();
        inventory.decreaseStock();

        inventory.updateTotalCount(2);

        assertThat(inventory.getTotalCount()).isEqualTo(2);
        assertThat(inventory.getAvailableCount()).isEqualTo(0);
    }

    private Inventory createInventory(final int totalCount) {
        return Inventory.create(1L, LocalDate.of(2026, 4, 1), totalCount);
    }
}
