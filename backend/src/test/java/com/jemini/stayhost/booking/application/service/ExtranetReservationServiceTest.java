package com.jemini.stayhost.booking.application.service;

import com.jemini.stayhost.booking.application.dto.ExtranetReservationSearch;
import com.jemini.stayhost.booking.application.dto.ReservationResult;
import com.jemini.stayhost.booking.domain.component.ReservationReader;
import com.jemini.stayhost.booking.domain.model.Reservation;
import com.jemini.stayhost.common.exception.AuthorizationException;
import com.jemini.stayhost.common.response.PageResult;
import com.jemini.stayhost.property.domain.component.PropertyReader;
import com.jemini.stayhost.property.domain.component.RoomTypeReader;
import com.jemini.stayhost.property.domain.model.Property;
import com.jemini.stayhost.property.domain.model.PropertyType;
import com.jemini.stayhost.property.domain.model.RoomType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ExtranetReservationServiceTest {

    @InjectMocks
    private ExtranetReservationService extranetReservationService;

    @Mock
    private ReservationReader reservationReader;

    @Mock
    private PropertyReader propertyReader;

    @Mock
    private RoomTypeReader roomTypeReader;

    private static final Long PARTNER_ID = 1L;
    private static final Long OTHER_PARTNER_ID = 999L;
    private static final Long PROPERTY_ID = 10L;
    private static final Long ROOM_TYPE_ID = 100L;
    private static final Long RESERVATION_ID = 1000L;

    @Test
    @DisplayName("파트너 숙소의 예약 목록을 조회한다")
    void 파트너_숙소의_예약_목록을_조회한다() {
        final Property property = createProperty(PARTNER_ID);
        final Reservation reservation = createReservation();
        final RoomType roomType = createRoomType();
        final Pageable pageable = PageRequest.of(0, 10);
        final ExtranetReservationSearch search = new ExtranetReservationSearch(null, null, null, null);

        given(propertyReader.findByPartnerId(PARTNER_ID, Pageable.unpaged())).willReturn(new PageImpl<>(List.of(property)));
        given(reservationReader.findByPropertyIdsWithFilters(eq(List.of(PROPERTY_ID)), isNull(), isNull(), isNull(), isNull(), eq(pageable)))
            .willReturn(new PageImpl<>(List.of(reservation)));
        given(propertyReader.getById(PROPERTY_ID)).willReturn(property);
        given(roomTypeReader.getById(ROOM_TYPE_ID)).willReturn(roomType);

        final PageResult<ReservationResult> result = extranetReservationService.getReservations(PARTNER_ID, search, pageable);

        assertThat(result.content()).hasSize(1);
    }

    @Test
    @DisplayName("파트너 숙소가 없으면 빈 결과를 반환한다")
    void 파트너_숙소가_없으면_빈_결과를_반환한다() {
        final Pageable pageable = PageRequest.of(0, 10);
        final ExtranetReservationSearch search = new ExtranetReservationSearch(null, null, null, null);

        given(propertyReader.findByPartnerId(PARTNER_ID, Pageable.unpaged())).willReturn(Page.empty());

        final PageResult<ReservationResult> result = extranetReservationService.getReservations(PARTNER_ID, search, pageable);

        assertThat(result.content()).isEmpty();
    }

    @Test
    @DisplayName("예약 상세를 조회한다")
    void 예약_상세를_조회한다() {
        final Reservation reservation = createReservation();
        final Property property = createProperty(PARTNER_ID);
        final RoomType roomType = createRoomType();

        given(reservationReader.getById(RESERVATION_ID)).willReturn(reservation);
        given(propertyReader.getById(PROPERTY_ID)).willReturn(property);
        given(roomTypeReader.getById(ROOM_TYPE_ID)).willReturn(roomType);

        final ReservationResult result = extranetReservationService.getReservation(RESERVATION_ID, PARTNER_ID);

        assertThat(result.reservationNumber()).isEqualTo(reservation.getReservationNumber());
        assertThat(result.propertyName()).isEqualTo("테스트 호텔");
        assertThat(result.roomTypeName()).isEqualTo("디럭스");
    }

    @Test
    @DisplayName("타 파트너 숙소의 예약을 조회하면 권한 예외가 발생한다")
    void 타_파트너_숙소의_예약을_조회하면_권한_예외가_발생한다() {
        final Reservation reservation = createReservation();
        final Property property = createProperty(PARTNER_ID);

        given(reservationReader.getById(RESERVATION_ID)).willReturn(reservation);
        given(propertyReader.getById(PROPERTY_ID)).willReturn(property);

        assertThatThrownBy(() -> extranetReservationService.getReservation(RESERVATION_ID, OTHER_PARTNER_ID))
            .isInstanceOf(AuthorizationException.class);
    }

    private Property createProperty(final Long partnerId) {
        final Property property = Property.create(partnerId, "테스트 호텔", PropertyType.HOTEL, "설명",
            "서울시 강남구", "서울", LocalTime.of(15, 0), LocalTime.of(11, 0), null, null, "https://example.com/thumb.jpg");
        ReflectionTestUtils.setField(property, "id", PROPERTY_ID);
        return property;
    }

    private RoomType createRoomType() {
        final RoomType roomType = RoomType.create(PROPERTY_ID, "디럭스", "설명", 2, BigDecimal.valueOf(120000), null, 10);
        ReflectionTestUtils.setField(roomType, "id", ROOM_TYPE_ID);
        return roomType;
    }

    private Reservation createReservation() {
        final Reservation reservation = Reservation.create(1L, PROPERTY_ID, ROOM_TYPE_ID,
            LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 3), "홍길동", "010-1234-5678", 2, BigDecimal.valueOf(200000));
        ReflectionTestUtils.setField(reservation, "id", RESERVATION_ID);
        return reservation;
    }
}
