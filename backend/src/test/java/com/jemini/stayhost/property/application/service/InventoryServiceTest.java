package com.jemini.stayhost.property.application.service;

import com.jemini.stayhost.common.exception.AuthorizationException;
import com.jemini.stayhost.common.exception.BusinessException;
import com.jemini.stayhost.common.exception.ErrorCode;
import com.jemini.stayhost.property.application.dto.InventoryBulkSetCommand;
import com.jemini.stayhost.property.application.dto.InventoryBulkSetResult;
import com.jemini.stayhost.property.application.dto.InventoryListResult;
import com.jemini.stayhost.property.domain.component.InventoryManager;
import com.jemini.stayhost.property.domain.component.InventoryReader;
import com.jemini.stayhost.property.domain.component.PropertyReader;
import com.jemini.stayhost.property.domain.component.RoomTypeReader;
import com.jemini.stayhost.property.domain.model.Inventory;
import com.jemini.stayhost.property.domain.model.Property;
import com.jemini.stayhost.property.domain.model.PropertyType;
import com.jemini.stayhost.property.domain.model.RoomType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @InjectMocks
    private InventoryService inventoryService;

    @Mock
    private RoomTypeReader roomTypeReader;

    @Mock
    private PropertyReader propertyReader;

    @Mock
    private InventoryReader inventoryReader;

    @Mock
    private InventoryManager inventoryManager;

    private static final Long PARTNER_ID = 1L;
    private static final Long PROPERTY_ID = 100L;
    private static final Long ROOM_TYPE_ID = 200L;

    @Test
    @DisplayName("재고 일괄설정 성공 - 신규생성")
    void 재고_일괄설정_성공_신규생성() {
        setupOwnership();
        given(inventoryReader.findByRoomTypeIdAndDateBetween(eq(ROOM_TYPE_ID), any(), any())).willReturn(List.of());

        final InventoryBulkSetResult result = inventoryService.bulkSet(ROOM_TYPE_ID, PARTNER_ID,
            InventoryBulkSetCommand.builder()
                .startDate(LocalDate.of(2026, 4, 1)).endDate(LocalDate.of(2026, 4, 3))
                .totalCount(10).build());

        assertThat(result.appliedDates()).isEqualTo(3);
        assertThat(result.totalCount()).isEqualTo(10);
    }

    @Test
    @DisplayName("재고 일괄설정 성공 - 기존재고 업데이트")
    void 재고_일괄설정_성공_기존재고_업데이트() {
        setupOwnership();
        final Inventory existing = Inventory.create(ROOM_TYPE_ID, LocalDate.of(2026, 4, 1), 5);
        given(inventoryReader.findByRoomTypeIdAndDateBetween(eq(ROOM_TYPE_ID), any(), any()))
            .willReturn(List.of(existing));

        final InventoryBulkSetResult result = inventoryService.bulkSet(ROOM_TYPE_ID, PARTNER_ID,
            InventoryBulkSetCommand.builder()
                .startDate(LocalDate.of(2026, 4, 1)).endDate(LocalDate.of(2026, 4, 2))
                .totalCount(20).build());

        assertThat(result.appliedDates()).isEqualTo(2);
        assertThat(existing.getTotalCount()).isEqualTo(20);
    }

    @Test
    @DisplayName("재고 일괄설정 - 시작일이 종료일 이후면 예외")
    void 재고_일괄설정_시작일이_종료일_이후면_예외() {
        setupOwnership();

        assertThatThrownBy(() -> inventoryService.bulkSet(ROOM_TYPE_ID, PARTNER_ID,
            InventoryBulkSetCommand.builder()
                .startDate(LocalDate.of(2026, 4, 10)).endDate(LocalDate.of(2026, 4, 1))
                .totalCount(10).build()))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_DATE_RANGE);
    }

    @Test
    @DisplayName("재고 일괄설정 - 소유권 없으면 예외")
    void 재고_일괄설정_소유권_없으면_예외() {
        given(roomTypeReader.getById(ROOM_TYPE_ID)).willReturn(createRoomType());
        given(propertyReader.getById(PROPERTY_ID)).willReturn(createProperty(PARTNER_ID));

        assertThatThrownBy(() -> inventoryService.bulkSet(ROOM_TYPE_ID, 999L,
            InventoryBulkSetCommand.builder()
                .startDate(LocalDate.of(2026, 4, 1)).endDate(LocalDate.of(2026, 4, 3))
                .totalCount(10).build()))
            .isInstanceOf(AuthorizationException.class);
    }

    @Test
    @DisplayName("재고 목록조회 성공")
    void 재고_목록조회_성공() {
        setupOwnership();
        given(inventoryReader.findByRoomTypeIdAndDateBetween(ROOM_TYPE_ID,
            LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 3)))
            .willReturn(List.of(
                Inventory.create(ROOM_TYPE_ID, LocalDate.of(2026, 4, 1), 10),
                Inventory.create(ROOM_TYPE_ID, LocalDate.of(2026, 4, 2), 10)));

        final InventoryListResult result = inventoryService.getInventory(ROOM_TYPE_ID, PARTNER_ID,
            LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 3));

        assertThat(result.inventory()).hasSize(2);
    }

    private void setupOwnership() {
        given(roomTypeReader.getById(ROOM_TYPE_ID)).willReturn(createRoomType());
        given(propertyReader.getById(PROPERTY_ID)).willReturn(createProperty(PARTNER_ID));
    }

    private RoomType createRoomType() {
        return RoomType.create(PROPERTY_ID, "스탠다드", "설명", 2, BigDecimal.valueOf(120000), null, 10);
    }

    private Property createProperty(final Long partnerId) {
        return Property.create(partnerId, "테스트 호텔", PropertyType.HOTEL, "설명",
            "서울시 강남구", "서울", LocalTime.of(15, 0), LocalTime.of(11, 0));
    }
}
