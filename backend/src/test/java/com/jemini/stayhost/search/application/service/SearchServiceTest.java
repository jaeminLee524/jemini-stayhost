package com.jemini.stayhost.search.application.service;

import com.jemini.stayhost.property.domain.component.InventoryReader;
import com.jemini.stayhost.property.domain.component.PropertyReader;
import com.jemini.stayhost.property.domain.component.RateReader;
import com.jemini.stayhost.property.domain.component.RoomTypeReader;
import com.jemini.stayhost.property.domain.model.Property;
import com.jemini.stayhost.property.domain.model.PropertyType;
import com.jemini.stayhost.property.domain.model.Rate;
import com.jemini.stayhost.property.domain.model.RoomType;
import com.jemini.stayhost.search.application.dto.RoomTypeRateResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @InjectMocks
    private SearchService searchService;

    @Mock
    private PropertyReader propertyReader;

    @Mock
    private RoomTypeReader roomTypeReader;

    @Mock
    private RateReader rateReader;

    @Mock
    private InventoryReader inventoryReader;

    private static final Long PROPERTY_ID = 42L;
    private static final Long ROOM_TYPE_ID_1 = 101L;
    private static final Long ROOM_TYPE_ID_2 = 102L;
    private static final LocalDate START_DATE = LocalDate.of(2026, 4, 1);
    private static final LocalDate END_DATE = LocalDate.of(2026, 4, 3);

    @Test
    @DisplayName("요금 조회 시 roomType별 개별 캐시를 경유한다")
    void 요금_조회_시_roomType별_개별_캐시를_경유한다() {
        final Property property = createProperty();
        final RoomType roomType1 = createRoomType(ROOM_TYPE_ID_1, "디럭스");
        final RoomType roomType2 = createRoomType(ROOM_TYPE_ID_2, "스탠다드");

        given(propertyReader.getActiveById(PROPERTY_ID)).willReturn(property);
        given(roomTypeReader.findActiveByPropertyId(PROPERTY_ID)).willReturn(List.of(roomType1, roomType2));
        given(rateReader.findByRoomTypeIdAndDateBetween(ROOM_TYPE_ID_1, START_DATE, END_DATE))
            .willReturn(List.of(Rate.create(ROOM_TYPE_ID_1, START_DATE, BigDecimal.valueOf(150000))));
        given(rateReader.findByRoomTypeIdAndDateBetween(ROOM_TYPE_ID_2, START_DATE, END_DATE))
            .willReturn(List.of(Rate.create(ROOM_TYPE_ID_2, START_DATE, BigDecimal.valueOf(100000))));
        given(inventoryReader.findByRoomTypeIdsAndDateBetween(any(), any(), any())).willReturn(List.of());

        final RoomTypeRateResult result = searchService.getRoomTypeRates(PROPERTY_ID, START_DATE, END_DATE);

        verify(rateReader).findByRoomTypeIdAndDateBetween(ROOM_TYPE_ID_1, START_DATE, END_DATE);
        verify(rateReader).findByRoomTypeIdAndDateBetween(ROOM_TYPE_ID_2, START_DATE, END_DATE);
        verify(rateReader, never()).findByRoomTypeIdsAndDateBetween(any(), any(), any());
        assertThat(result.roomTypes()).hasSize(2);
    }

    @Test
    @DisplayName("rate가 없는 날짜는 basePrice로 폴백한다")
    void rate가_없는_날짜는_basePrice로_폴백한다() {
        final Property property = createProperty();
        final RoomType roomType = createRoomType(ROOM_TYPE_ID_1, "디럭스");

        given(propertyReader.getActiveById(PROPERTY_ID)).willReturn(property);
        given(roomTypeReader.findActiveByPropertyId(PROPERTY_ID)).willReturn(List.of(roomType));
        given(rateReader.findByRoomTypeIdAndDateBetween(ROOM_TYPE_ID_1, START_DATE, END_DATE)).willReturn(List.of());
        given(inventoryReader.findByRoomTypeIdsAndDateBetween(any(), any(), any())).willReturn(List.of());

        final RoomTypeRateResult result = searchService.getRoomTypeRates(PROPERTY_ID, START_DATE, END_DATE);

        result.roomTypes().getFirst().rates().forEach(dailyRate ->
            assertThat(dailyRate.price()).isEqualByComparingTo(BigDecimal.valueOf(120000))
        );
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
