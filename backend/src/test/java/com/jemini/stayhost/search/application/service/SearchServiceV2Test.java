package com.jemini.stayhost.search.application.service;

import com.jemini.stayhost.common.exception.BusinessException;
import com.jemini.stayhost.common.exception.ErrorCode;
import com.jemini.stayhost.common.response.PageResult;
import com.jemini.stayhost.property.domain.model.*;
import com.jemini.stayhost.search.application.dto.PropertyDetailResult;
import com.jemini.stayhost.search.application.dto.PropertySearchResult;
import com.jemini.stayhost.search.application.dto.RoomTypeRateResult;
import com.jemini.stayhost.search.domain.component.InventoryReaderV2;
import com.jemini.stayhost.search.domain.component.PropertyReaderV2;
import com.jemini.stayhost.search.domain.component.RateReaderV2;
import com.jemini.stayhost.search.domain.component.RoomTypeReaderV2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SearchServiceV2Test {

    @InjectMocks
    private SearchServiceV2 searchServiceV2;

    @Mock
    private PropertyReaderV2 propertyReader;

    @Mock
    private RoomTypeReaderV2 roomTypeReader;

    @Mock
    private RateReaderV2 rateReader;

    @Mock
    private InventoryReaderV2 inventoryReader;

    private static final Long PROPERTY_ID = 42L;
    private static final Long ROOM_TYPE_ID = 101L;
    private static final LocalDate START_DATE = LocalDate.of(2026, 4, 1);
    private static final LocalDate END_DATE = LocalDate.of(2026, 4, 3);

    @Test
    @DisplayName("숙소 검색 시 ACTIVE 객실이 포함된 결과를 반환한다")
    void 숙소_검색_시_ACTIVE_객실이_포함된_결과를_반환한다() {
        final Property property = createProperty();
        final RoomType roomType = createRoomType(ROOM_TYPE_ID, "디럭스");
        final Pageable pageable = PageRequest.of(0, 10);

        given(propertyReader.searchActive("서울", null, pageable)).willReturn(new PageImpl<>(List.of(property)));
        given(roomTypeReader.findActiveByPropertyIds(List.of(PROPERTY_ID))).willReturn(List.of(roomType));

        final PageResult<PropertySearchResult> result = searchServiceV2.searchProperties("서울", null, pageable);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().name()).isEqualTo("테스트 호텔");
    }

    @Test
    @DisplayName("숙소 상세 조회 시 객실 유형 목록을 포함한다")
    void 숙소_상세_조회_시_객실_유형_목록을_포함한다() {
        final Property property = createProperty();
        final RoomType roomType = createRoomType(ROOM_TYPE_ID, "디럭스");

        given(propertyReader.getActiveById(PROPERTY_ID)).willReturn(property);
        given(roomTypeReader.findActiveByPropertyId(PROPERTY_ID)).willReturn(List.of(roomType));

        final PropertyDetailResult result = searchServiceV2.getPropertyDetail(PROPERTY_ID);

        assertThat(result.id()).isEqualTo(PROPERTY_ID);
        assertThat(result.name()).isEqualTo("테스트 호텔");
        assertThat(result.roomTypes()).hasSize(1);
    }

    @Test
    @DisplayName("요금 조회 시 roomType별 일별 요금을 반환한다")
    void 요금_조회_시_roomType별_일별_요금을_반환한다() {
        final Property property = createProperty();
        final RoomType roomType = createRoomType(ROOM_TYPE_ID, "디럭스");
        final Rate rate = Rate.create(ROOM_TYPE_ID, START_DATE, BigDecimal.valueOf(150000));
        final Inventory inventory = Inventory.create(ROOM_TYPE_ID, START_DATE, 5);

        given(propertyReader.getActiveById(PROPERTY_ID)).willReturn(property);
        given(roomTypeReader.findActiveByPropertyId(PROPERTY_ID)).willReturn(List.of(roomType));
        given(rateReader.findByRoomTypeIdAndDateBetween(ROOM_TYPE_ID, START_DATE, END_DATE)).willReturn(List.of(rate));
        given(inventoryReader.findByRoomTypeIdsAndDateBetween(any(), any(), any())).willReturn(List.of(inventory));

        final RoomTypeRateResult result = searchServiceV2.getRoomTypeRates(PROPERTY_ID, START_DATE, END_DATE);

        assertThat(result.propertyId()).isEqualTo(PROPERTY_ID);
        assertThat(result.roomTypes()).hasSize(1);
        assertThat(result.roomTypes().getFirst().rates()).hasSize(3);
        assertThat(result.roomTypes().getFirst().rates().getFirst().price()).isEqualByComparingTo(BigDecimal.valueOf(150000));
        assertThat(result.roomTypes().getFirst().rates().getFirst().available()).isTrue();
    }

    @Test
    @DisplayName("rate가 없는 날짜는 basePrice로 폴백한다")
    void rate가_없는_날짜는_basePrice로_폴백한다() {
        final Property property = createProperty();
        final RoomType roomType = createRoomType(ROOM_TYPE_ID, "디럭스");

        given(propertyReader.getActiveById(PROPERTY_ID)).willReturn(property);
        given(roomTypeReader.findActiveByPropertyId(PROPERTY_ID)).willReturn(List.of(roomType));
        given(rateReader.findByRoomTypeIdAndDateBetween(ROOM_TYPE_ID, START_DATE, END_DATE)).willReturn(List.of());
        given(inventoryReader.findByRoomTypeIdsAndDateBetween(any(), any(), any())).willReturn(List.of());

        final RoomTypeRateResult result = searchServiceV2.getRoomTypeRates(PROPERTY_ID, START_DATE, END_DATE);

        result.roomTypes().getFirst().rates().forEach(dailyRate ->
            assertThat(dailyRate.price()).isEqualByComparingTo(BigDecimal.valueOf(120000))
        );
    }

    @Test
    @DisplayName("재고가 없는 날짜는 available이 false다")
    void 재고가_없는_날짜는_available이_false다() {
        final Property property = createProperty();
        final RoomType roomType = createRoomType(ROOM_TYPE_ID, "디럭스");

        given(propertyReader.getActiveById(PROPERTY_ID)).willReturn(property);
        given(roomTypeReader.findActiveByPropertyId(PROPERTY_ID)).willReturn(List.of(roomType));
        given(rateReader.findByRoomTypeIdAndDateBetween(ROOM_TYPE_ID, START_DATE, END_DATE)).willReturn(List.of());
        given(inventoryReader.findByRoomTypeIdsAndDateBetween(any(), any(), any())).willReturn(List.of());

        final RoomTypeRateResult result = searchServiceV2.getRoomTypeRates(PROPERTY_ID, START_DATE, END_DATE);

        result.roomTypes().getFirst().rates().forEach(dailyRate ->
            assertThat(dailyRate.available()).isFalse()
        );
    }

    @Test
    @DisplayName("시작일이 종료일보다 후면 예외가 발생한다")
    void 시작일이_종료일보다_후면_예외가_발생한다() {
        assertThatThrownBy(() -> searchServiceV2.getRoomTypeRates(PROPERTY_ID, END_DATE, START_DATE))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_DATE_RANGE);
    }

    @Test
    @DisplayName("날짜 범위가 30일을 초과하면 예외가 발생한다")
    void 날짜_범위가_30일을_초과하면_예외가_발생한다() {
        final LocalDate farEnd = START_DATE.plusDays(31);

        assertThatThrownBy(() -> searchServiceV2.getRoomTypeRates(PROPERTY_ID, START_DATE, farEnd))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.DATE_RANGE_TOO_LONG);
    }

    private Property createProperty() {
        final Property property = Property.create(1L, "테스트 호텔", PropertyType.HOTEL, "설명",
            "서울시 강남구", "서울", LocalTime.of(15, 0), LocalTime.of(11, 0), null, null, null);
        ReflectionTestUtils.setField(property, "id", PROPERTY_ID);
        return property;
    }

    private RoomType createRoomType(final Long id, final String name) {
        final RoomType roomType = RoomType.create(PROPERTY_ID, name, "설명", 2, BigDecimal.valueOf(120000), null, 10);
        ReflectionTestUtils.setField(roomType, "id", id);
        return roomType;
    }
}
