package com.jemini.stayhost.booking.application.service;

import com.jemini.stayhost.booking.application.dto.CancelReservationResult;
import com.jemini.stayhost.booking.application.dto.CreateReservationCommand;
import com.jemini.stayhost.booking.application.dto.ReservationResult;
import com.jemini.stayhost.booking.domain.component.ReservationManager;
import com.jemini.stayhost.booking.domain.component.ReservationReader;
import com.jemini.stayhost.booking.domain.event.ReservationCancelledEvent;
import com.jemini.stayhost.booking.domain.model.Reservation;
import com.jemini.stayhost.booking.infrastructure.cache.InventoryCache;
import com.jemini.stayhost.common.exception.AuthorizationException;
import com.jemini.stayhost.common.exception.BusinessException;
import com.jemini.stayhost.common.exception.ErrorCode;
import com.jemini.stayhost.common.exception.NotFoundException;
import com.jemini.stayhost.common.response.PageResult;
import com.jemini.stayhost.property.domain.component.InventoryReader;
import com.jemini.stayhost.property.domain.component.PropertyReader;
import com.jemini.stayhost.property.domain.component.RateReader;
import com.jemini.stayhost.property.domain.component.RoomTypeReader;
import com.jemini.stayhost.property.domain.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @InjectMocks
    private ReservationService reservationService;

    @Mock
    private ReservationReader reservationReader;

    @Mock
    private ReservationManager reservationManager;

    @Mock
    private PropertyReader propertyReader;

    @Mock
    private RoomTypeReader roomTypeReader;

    @Mock
    private RateReader rateReader;

    @Mock
    private InventoryReader inventoryReader;

    @Mock
    private InventoryCache inventoryCache;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private static final Long USER_ID = 1L;
    private static final Long PROPERTY_ID = 100L;
    private static final Long ROOM_TYPE_ID = 200L;
    private static final Long RESERVATION_ID = 5001L;

    // -- createReservation --

    @Test
    @DisplayName("예약 생성 성공")
    void 예약_생성_성공() {
        setupPropertyAndRoomType();
        setupInventories(10);
        given(rateReader.findByRoomTypeIdAndDateBetween(eq(ROOM_TYPE_ID), any(), any()))
            .willReturn(List.of());
        given(reservationManager.save(any(Reservation.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        final ReservationResult result = reservationService.createWithExclusiveLock(USER_ID, createCommand());

        assertThat(result.status()).isEqualTo("CONFIRMED");
        assertThat(result.propertyName()).isEqualTo("테스트 호텔");
        assertThat(result.roomTypeName()).isEqualTo("스탠다드");
        verify(reservationManager).save(any(Reservation.class));
    }

    @Test
    @DisplayName("예약 생성 - 투숙인원 초과이면 예외")
    void 예약_생성_투숙인원_초과이면_예외() {
        given(inventoryReader.findByRoomTypeIdAndDateRangeForUpdate(eq(ROOM_TYPE_ID), any(), any()))
            .willReturn(List.of(
                Inventory.create(ROOM_TYPE_ID, LocalDate.of(2026, 4, 10), 10),
                Inventory.create(ROOM_TYPE_ID, LocalDate.of(2026, 4, 11), 10)));
        given(roomTypeReader.getById(ROOM_TYPE_ID)).willReturn(
            RoomType.create(PROPERTY_ID, "스탠다드", "설명", 2, BigDecimal.valueOf(120000), null, 10));

        assertThatThrownBy(() -> reservationService.createWithExclusiveLock(USER_ID,
            CreateReservationCommand.builder()
                .propertyId(PROPERTY_ID).roomTypeId(ROOM_TYPE_ID)
                .checkInDate(LocalDate.of(2026, 4, 10)).checkOutDate(LocalDate.of(2026, 4, 12))
                .guestName("김민준").guestCount(5).build()))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_GUEST_COUNT);
    }

    @Test
    @DisplayName("예약 생성 - 체크아웃이 체크인 이전이면 예외")
    void 예약_생성_체크아웃이_체크인_이전이면_예외() {
        assertThatThrownBy(() -> reservationService.createWithExclusiveLock(USER_ID,
            CreateReservationCommand.builder()
                .propertyId(PROPERTY_ID).roomTypeId(ROOM_TYPE_ID)
                .checkInDate(LocalDate.of(2026, 4, 12)).checkOutDate(LocalDate.of(2026, 4, 10))
                .guestName("김민준").guestCount(2).build()))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_DATE_RANGE);
    }

    @Test
    @DisplayName("예약 생성 - 재고 정보 없으면 예외")
    void 예약_생성_재고_정보_없으면_예외() {
        given(inventoryReader.findByRoomTypeIdAndDateRangeForUpdate(eq(ROOM_TYPE_ID), any(), any()))
            .willReturn(List.of());
        given(roomTypeReader.getById(ROOM_TYPE_ID)).willReturn(
            RoomType.create(PROPERTY_ID, "스탠다드", "설명", 2, BigDecimal.valueOf(120000), null, 10));

        assertThatThrownBy(() -> reservationService.createWithExclusiveLock(USER_ID, createCommand()))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVENTORY_INSUFFICIENT);
    }

    @Test
    @DisplayName("예약 생성 - 재고 부족이면 예외")
    void 예약_생성_재고_부족이면_예외() {
        setupInventories(0);
        given(roomTypeReader.getById(ROOM_TYPE_ID)).willReturn(
            RoomType.create(PROPERTY_ID, "스탠다드", "설명", 2, BigDecimal.valueOf(120000), null, 10));

        assertThatThrownBy(() -> reservationService.createWithExclusiveLock(USER_ID, createCommand()))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVENTORY_INSUFFICIENT);
    }

    @Test
    @DisplayName("예약 생성 - 특가 요금 적용 확인")
    void 예약_생성_특가_요금_적용_확인() {
        setupPropertyAndRoomType();
        setupInventories(10);
        given(rateReader.findByRoomTypeIdAndDateBetween(eq(ROOM_TYPE_ID), any(), any()))
            .willReturn(List.of(Rate.create(ROOM_TYPE_ID, LocalDate.of(2026, 4, 10), BigDecimal.valueOf(150000))));
        given(reservationManager.save(any(Reservation.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        final ReservationResult result = reservationService.createWithExclusiveLock(USER_ID, createCommand());

        // 4/10: 150000 (특가), 4/11: 120000 (basePrice) = 270000
        assertThat(result.finalPrice()).isEqualByComparingTo(BigDecimal.valueOf(270000));
    }

    // -- getMyReservations --

    @Test
    @DisplayName("내 예약 목록 조회 성공")
    void 내_예약_목록_조회_성공() {
        final Reservation reservation = createConfirmedReservation();
        given(reservationReader.findByUserId(USER_ID, PageRequest.of(0, 20)))
            .willReturn(new PageImpl<>(List.of(reservation)));
        setupPropertyAndRoomType();

        final PageResult<ReservationResult> result = reservationService.getMyReservations(
            USER_ID, null, PageRequest.of(0, 20));

        assertThat(result.content()).hasSize(1);
    }

    @Test
    @DisplayName("내 예약 목록 - 상태 필터 조회 성공")
    void 내_예약_목록_상태_필터_조회_성공() {
        final Reservation reservation = createConfirmedReservation();
        given(reservationReader.findByUserIdAndStatus(USER_ID, "CONFIRMED", PageRequest.of(0, 20)))
            .willReturn(new PageImpl<>(List.of(reservation)));
        setupPropertyAndRoomType();

        final PageResult<ReservationResult> result = reservationService.getMyReservations(
            USER_ID, "CONFIRMED", PageRequest.of(0, 20));

        assertThat(result.content()).hasSize(1);
    }

    // -- getReservation --

    @Test
    @DisplayName("예약 상세 조회 성공")
    void 예약_상세_조회_성공() {
        final Reservation reservation = createConfirmedReservation();
        given(reservationReader.getById(RESERVATION_ID)).willReturn(reservation);
        setupPropertyAndRoomType();

        final ReservationResult result = reservationService.getReservation(RESERVATION_ID, USER_ID);

        assertThat(result.status()).isEqualTo("CONFIRMED");
        assertThat(result.propertyName()).isEqualTo("테스트 호텔");
    }

    @Test
    @DisplayName("예약 상세 조회 - 본인 예약이 아니면 예외")
    void 예약_상세_조회_본인_예약이_아니면_예외() {
        final Reservation reservation = createConfirmedReservation();
        given(reservationReader.getById(RESERVATION_ID)).willReturn(reservation);

        assertThatThrownBy(() -> reservationService.getReservation(RESERVATION_ID, 999L))
            .isInstanceOf(AuthorizationException.class);
    }

    @Test
    @DisplayName("예약 상세 조회 - 존재하지 않으면 예외")
    void 예약_상세_조회_존재하지_않으면_예외() {
        given(reservationReader.getById(999L))
            .willThrow(new NotFoundException(ErrorCode.RESERVATION_NOT_FOUND));

        assertThatThrownBy(() -> reservationService.getReservation(999L, USER_ID))
            .isInstanceOf(NotFoundException.class);
    }

    // -- cancelReservation --

    @Test
    @DisplayName("예약 취소 성공")
    void 예약_취소_성공() {
        final Reservation reservation = createConfirmedReservation();
        given(reservationReader.getByIdForUpdate(RESERVATION_ID)).willReturn(reservation);
        setupInventoriesForCancel();

        final CancelReservationResult result = reservationService.cancelReservation(
            RESERVATION_ID, USER_ID, "일정 변경");

        assertThat(result.status()).isEqualTo("CANCELLED");
        assertThat(result.cancelReason()).isEqualTo("일정 변경");
    }

    @Test
    @DisplayName("예약 취소 - 이미 취소된 예약이면 예외")
    void 예약_취소_이미_취소된_예약이면_예외() {
        final Reservation reservation = createConfirmedReservation();
        reservation.cancel("최초 취소");
        given(reservationReader.getByIdForUpdate(RESERVATION_ID)).willReturn(reservation);

        assertThatThrownBy(() -> reservationService.cancelReservation(
            RESERVATION_ID, USER_ID, "재취소"))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESERVATION_ALREADY_CANCELLED);
    }

    @Test
    @DisplayName("예약 취소 - 본인 예약이 아니면 예외")
    void 예약_취소_본인_예약이_아니면_예외() {
        final Reservation reservation = createConfirmedReservation();
        given(reservationReader.getByIdForUpdate(RESERVATION_ID)).willReturn(reservation);

        assertThatThrownBy(() -> reservationService.cancelReservation(
            RESERVATION_ID, 999L, "취소"))
            .isInstanceOf(AuthorizationException.class);
    }

    @Test
    @DisplayName("예약 취소 시 재고 복원 확인")
    void 예약_취소_시_재고_복원_확인() {
        final Reservation reservation = createConfirmedReservation();
        given(reservationReader.getByIdForUpdate(RESERVATION_ID)).willReturn(reservation);
        final Inventory inv1 = Inventory.create(ROOM_TYPE_ID, LocalDate.of(2026, 4, 10), 10);
        final Inventory inv2 = Inventory.create(ROOM_TYPE_ID, LocalDate.of(2026, 4, 11), 10);
        inv1.decreaseStock();
        inv2.decreaseStock();
        given(inventoryReader.findByRoomTypeIdAndDateBetween(eq(ROOM_TYPE_ID), any(), any()))
            .willReturn(List.of(inv1, inv2));

        reservationService.cancelReservation(RESERVATION_ID, USER_ID, "취소");

        assertThat(inv1.getReservedCount()).isEqualTo(0);
        assertThat(inv2.getReservedCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("예약 취소 실패 시 재고 복원과 캐시 복원이 호출되지 않는다")
    void 예약_취소_실패_시_재고_복원_호출되지_않는다() {
        final Reservation reservation = createConfirmedReservation();
        reservation.cancel("최초 취소");
        given(reservationReader.getByIdForUpdate(RESERVATION_ID)).willReturn(reservation);

        assertThatThrownBy(() -> reservationService.cancelReservation(
            RESERVATION_ID, USER_ID, "재취소"))
            .isInstanceOf(BusinessException.class);

        verify(inventoryReader, never()).findByRoomTypeIdAndDateBetween(any(), any(), any());
        verify(inventoryCache, never()).restore(any(), any());
    }

    @Test
    @DisplayName("예약 취소 성공 시 캐시 재고가 복원된다")
    void 예약_취소_성공_시_캐시_재고가_복원된다() {
        final Reservation reservation = createConfirmedReservation();
        given(reservationReader.getByIdForUpdate(RESERVATION_ID)).willReturn(reservation);
        setupInventoriesForCancel();

        reservationService.cancelReservation(RESERVATION_ID, USER_ID, "취소");

        verify(inventoryCache).restore(eq(ROOM_TYPE_ID), any());
    }

    @Test
    @DisplayName("예약 취소 성공 시 이벤트가 발행된다")
    void 예약_취소_성공_시_이벤트가_발행된다() {
        final Reservation reservation = createConfirmedReservation();
        given(reservationReader.getByIdForUpdate(RESERVATION_ID)).willReturn(reservation);
        setupInventoriesForCancel();

        reservationService.cancelReservation(RESERVATION_ID, USER_ID, "취소");

        verify(eventPublisher).publishEvent(any(ReservationCancelledEvent.class));
    }

    // -- helper methods --

    private CreateReservationCommand createCommand() {
        return CreateReservationCommand.builder()
            .propertyId(PROPERTY_ID)
            .roomTypeId(ROOM_TYPE_ID)
            .checkInDate(LocalDate.of(2026, 4, 10))
            .checkOutDate(LocalDate.of(2026, 4, 12))
            .guestName("김민준")
            .guestPhone("010-1234-5678")
            .guestCount(2)
            .build();
    }

    private void setupPropertyAndRoomType() {
        given(propertyReader.getById(PROPERTY_ID)).willReturn(
            Property.create(1L, "테스트 호텔", PropertyType.HOTEL, "설명",
                "서울시 강남구", "서울", LocalTime.of(15, 0), LocalTime.of(11, 0),
                null, null, null));
        given(roomTypeReader.getById(ROOM_TYPE_ID)).willReturn(
            RoomType.create(PROPERTY_ID, "스탠다드", "설명", 2, BigDecimal.valueOf(120000), null, 10));
    }

    private void setupInventories(final int totalCount) {
        given(inventoryReader.findByRoomTypeIdAndDateRangeForUpdate(eq(ROOM_TYPE_ID), any(), any()))
            .willReturn(List.of(
                Inventory.create(ROOM_TYPE_ID, LocalDate.of(2026, 4, 10), totalCount),
                Inventory.create(ROOM_TYPE_ID, LocalDate.of(2026, 4, 11), totalCount)));
    }

    private void setupInventoriesForCancel() {
        final Inventory inv1 = Inventory.create(ROOM_TYPE_ID, LocalDate.of(2026, 4, 10), 10);
        final Inventory inv2 = Inventory.create(ROOM_TYPE_ID, LocalDate.of(2026, 4, 11), 10);
        inv1.decreaseStock();
        inv2.decreaseStock();
        given(inventoryReader.findByRoomTypeIdAndDateBetween(eq(ROOM_TYPE_ID), any(), any()))
            .willReturn(List.of(inv1, inv2));
    }

    private Reservation createConfirmedReservation() {
        return Reservation.create(
            USER_ID, PROPERTY_ID, ROOM_TYPE_ID,
            LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 12),
            "김민준", "010-1234-5678", 2, BigDecimal.valueOf(240000));
    }
}
