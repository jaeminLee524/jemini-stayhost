package com.jemini.stayhost.booking.application.facade;

import com.jemini.stayhost.booking.application.dto.CreateReservationCommand;
import com.jemini.stayhost.booking.application.dto.ReservationResult;
import com.jemini.stayhost.booking.application.service.ReservationService;
import com.jemini.stayhost.booking.infrastructure.cache.InventoryCache;
import com.jemini.stayhost.common.exception.BusinessException;
import com.jemini.stayhost.common.exception.ErrorCode;
import com.jemini.stayhost.property.application.service.PropertyService;
import com.jemini.stayhost.property.application.service.RoomTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReservationFacadeTest {

    private ReservationFacade reservationFacade;

    @Mock
    private ReservationService reservationService;

    @Mock
    private PropertyService propertyService;

    @Mock
    private RoomTypeService roomTypeService;

    @Mock
    private InventoryCache inventoryCache;

    private static final Long USER_ID = 1L;
    private static final Long PROPERTY_ID = 10L;
    private static final Long ROOM_TYPE_ID = 100L;

    @BeforeEach
    void setUp() {
        reservationFacade = new ReservationFacade(
            reservationService, propertyService, roomTypeService, inventoryCache
        );
    }

    @Test
    @DisplayName("비활성 숙소에 예약하면 PROPERTY_NOT_ACTIVE 예외")
    void 비활성_숙소에_예약하면_예외() {
        final CreateReservationCommand command = createCommand(
            LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));

        willThrow(new BusinessException(ErrorCode.PROPERTY_NOT_ACTIVE))
            .given(propertyService).validatePropertyActive(PROPERTY_ID);

        assertThatThrownBy(() -> reservationFacade.createReservation(USER_ID, command))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.PROPERTY_NOT_ACTIVE);

        verify(inventoryCache, never()).tryDecreaseAll(any(), any());
    }

    @Test
    @DisplayName("체크인이 체크아웃 이후이면 INVALID_DATE_RANGE 예외")
    void 체크인이_체크아웃_이후이면_예외() {
        final CreateReservationCommand command = createCommand(
            LocalDate.now().plusDays(3), LocalDate.now().plusDays(1));

        assertThatThrownBy(() -> reservationFacade.createReservation(USER_ID, command))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_DATE_RANGE);

        verify(inventoryCache, never()).tryDecreaseAll(any(), any());
    }

    @Test
    @DisplayName("서비스 예외 시 캐시가 롤백된다")
    void 서비스_예외시_캐시가_롤백된다() {
        final LocalDate checkIn = LocalDate.now().plusDays(1);
        final LocalDate checkOut = LocalDate.now().plusDays(2);
        final CreateReservationCommand command = createCommand(checkIn, checkOut);
        final List<LocalDate> stayDates = checkIn.datesUntil(checkOut).toList();

        given(reservationService.createWithInventoryLock(eq(USER_ID), eq(command)))
            .willThrow(new BusinessException(ErrorCode.INVENTORY_INSUFFICIENT));

        assertThatThrownBy(() -> reservationFacade.createReservation(USER_ID, command))
            .isInstanceOf(BusinessException.class);

        verify(inventoryCache).rollbackAll(ROOM_TYPE_ID, stayDates);
    }

    @Test
    @DisplayName("정상 예약 시 캐시 롤백이 호출되지 않는다")
    void 정상_예약시_캐시_롤백이_호출되지_않는다() {
        final LocalDate checkIn = LocalDate.now().plusDays(1);
        final LocalDate checkOut = LocalDate.now().plusDays(2);
        final CreateReservationCommand command = createCommand(checkIn, checkOut);

        given(reservationService.createWithInventoryLock(eq(USER_ID), eq(command)))
            .willReturn(org.mockito.Mockito.mock(ReservationResult.class));

        reservationFacade.createReservation(USER_ID, command);

        verify(inventoryCache, never()).rollbackAll(any(), any());
    }

    @Test
    @DisplayName("수용인원 초과 시 캐시 차감 전에 예외 발생")
    void 수용인원_초과시_캐시_차감_전에_예외() {
        final CreateReservationCommand command = createCommand(
            LocalDate.now().plusDays(1), LocalDate.now().plusDays(2), 10);

        willThrow(new BusinessException(ErrorCode.INVALID_GUEST_COUNT))
            .given(roomTypeService).validateGuestCount(ROOM_TYPE_ID, 10);

        assertThatThrownBy(() -> reservationFacade.createReservation(USER_ID, command))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_GUEST_COUNT);

        verify(inventoryCache, never()).tryDecreaseAll(any(), any());
    }

    private CreateReservationCommand createCommand(final LocalDate checkIn, final LocalDate checkOut) {
        return createCommand(checkIn, checkOut, 2);
    }

    private CreateReservationCommand createCommand(final LocalDate checkIn, final LocalDate checkOut, final int guestCount) {
        return CreateReservationCommand.builder()
            .propertyId(PROPERTY_ID)
            .roomTypeId(ROOM_TYPE_ID)
            .checkInDate(checkIn)
            .checkOutDate(checkOut)
            .guestName("테스트")
            .guestPhone("010-1234-5678")
            .guestCount(guestCount)
            .build();
    }
}
