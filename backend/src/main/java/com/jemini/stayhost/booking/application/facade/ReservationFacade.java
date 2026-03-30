package com.jemini.stayhost.booking.application.facade;

import com.jemini.stayhost.booking.application.dto.CreateReservationCommand;
import com.jemini.stayhost.booking.application.dto.ReservationResult;
import com.jemini.stayhost.booking.application.service.ReservationService;
import com.jemini.stayhost.booking.infrastructure.cache.InventoryCache;
import com.jemini.stayhost.property.domain.component.RoomTypeReader;
import com.jemini.stayhost.property.domain.model.RoomType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservationFacade {

    private final ReservationService reservationService;
    private final RoomTypeReader roomTypeReader;
    private final InventoryCache inventoryCache;

    /**
     * 예약 생성 오케스트레이션.
     * <p> 1) 객실 검증
     * <p> 2) Caffeine CAS 1차 필터링 (DB 접근 전 빠른 매진 판단)
     * <p> 3) DB 비관적 락 기반 예약 생성
     * <p> 4) DB 실패 시 CAS 캐시 롤백
     */
    public ReservationResult createReservation(
        final Long userId,
        final CreateReservationCommand command
    ) {
        final RoomType roomType = roomTypeReader.getById(command.roomTypeId());
        roomType.validateGuestCount(command.guestCount());

        final List<LocalDate> stayDates = command.checkInDate()
            .datesUntil(command.checkOutDate()).toList();

        inventoryCache.tryDecreaseAll(command.roomTypeId(), stayDates);

        try {
            return reservationService.createWithInventoryLock(userId, command);
        } catch (final Exception e) {
            inventoryCache.rollbackAll(command.roomTypeId(), stayDates);
            throw e;
        }
    }
}
