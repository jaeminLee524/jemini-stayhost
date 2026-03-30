package com.jemini.stayhost.booking.application.service;

import com.jemini.stayhost.booking.application.dto.CancelReservationResult;
import com.jemini.stayhost.booking.application.dto.CreateReservationCommand;
import com.jemini.stayhost.booking.application.dto.ReservationResult;
import com.jemini.stayhost.booking.domain.component.ReservationManager;
import com.jemini.stayhost.booking.domain.component.ReservationReader;
import com.jemini.stayhost.booking.domain.event.ReservationCancelledEvent;
import com.jemini.stayhost.booking.domain.event.ReservationCreatedEvent;
import com.jemini.stayhost.booking.domain.model.Reservation;
import com.jemini.stayhost.booking.domain.model.ReservationDailyRate;
import com.jemini.stayhost.booking.infrastructure.cache.InventoryCache;
import com.jemini.stayhost.common.exception.BusinessException;
import com.jemini.stayhost.common.exception.ErrorCode;
import com.jemini.stayhost.common.response.PageResult;
import com.jemini.stayhost.property.domain.component.InventoryReader;
import com.jemini.stayhost.property.domain.component.PropertyReader;
import com.jemini.stayhost.property.domain.component.RateReader;
import com.jemini.stayhost.property.domain.component.RoomTypeReader;
import com.jemini.stayhost.property.domain.model.Inventory;
import com.jemini.stayhost.property.domain.model.Property;
import com.jemini.stayhost.property.domain.model.Rate;
import com.jemini.stayhost.property.domain.model.RoomType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationReader reservationReader;
    private final ReservationManager reservationManager;
    private final PropertyReader propertyReader;
    private final RoomTypeReader roomTypeReader;
    private final RateReader rateReader;
    private final InventoryReader inventoryReader;
    private final InventoryCache inventoryCache;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 비관적 락 기반 예약 생성. Facade에서 CAS 1차 필터링 후 호출된다. TOCTOU 방어: 락 획득 후 객실/재고를 재검증한다.
     */
    @Transactional
    public ReservationResult createWithInventoryLock(
        final Long userId,
        final CreateReservationCommand command
    ) {
        final List<LocalDate> stayDates = generateStayDates(command.checkInDate(), command.checkOutDate());
        final LocalDate lastStayDate = stayDates.getLast();

        // 비관적 락으로 재고 행 잠금 (ORDER BY room_type_id, date 보장)
        final List<Inventory> inventories = inventoryReader.findAndLockByRoomTypeIdAndDateRange(command.roomTypeId(), command.checkInDate(), lastStayDate);

        // TOCTOU 방어적 재검증
        final RoomType roomType = roomTypeReader.getById(command.roomTypeId());
        roomType.validateGuestCount(command.guestCount());
        validateInventorySufficient(stayDates, inventories);

        // 재고 차감
        inventories.forEach(Inventory::decreaseStock);

        // 요금 로드 + 예약 생성
        final Map<LocalDate, BigDecimal> dailyPrices = loadDailyPrices(command.roomTypeId(), stayDates, roomType.getBasePrice());
        final Property property = propertyReader.getById(command.propertyId());
        final Reservation reservation = createAndSaveReservation(userId, command, calculateTotalPrice(dailyPrices));
        saveDailyRates(reservation, dailyPrices);

        eventPublisher.publishEvent(ReservationCreatedEvent.create(reservation.getId()));

        return toResult(reservation, property, roomType);
    }

    // -- createWithInventoryLock private methods --

    private List<LocalDate> generateStayDates(final LocalDate checkIn, final LocalDate checkOut) {
        if (!checkIn.isBefore(checkOut)) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE);
        }
        return checkIn.datesUntil(checkOut).toList();
    }

    private void validateInventorySufficient(
        final List<LocalDate> stayDates,
        final List<Inventory> inventories
    ) {
        final Map<LocalDate, Inventory> inventoryMap = inventories.stream()
            .collect(Collectors.toMap(Inventory::getDate, i -> i));

        for (final LocalDate date : stayDates) {
            final Inventory inventory = inventoryMap.get(date);
            if (inventory == null || inventory.getAvailableCount() <= 0) {
                throw new BusinessException(ErrorCode.INVENTORY_INSUFFICIENT);
            }
        }
    }

    private Map<LocalDate, BigDecimal> loadDailyPrices(
        final Long roomTypeId,
        final List<LocalDate> stayDates,
        final BigDecimal basePrice
    ) {
        final Map<LocalDate, BigDecimal> rateMap = rateReader
            .findByRoomTypeIdAndDateBetween(roomTypeId, stayDates.getFirst(), stayDates.getLast())
            .stream()
            .collect(Collectors.toMap(Rate::getDate, Rate::getPrice));

        return stayDates.stream()
            .collect(Collectors.toMap(
                date -> date,
                date -> rateMap.getOrDefault(date, basePrice)
            ));
    }

    private BigDecimal calculateTotalPrice(final Map<LocalDate, BigDecimal> dailyPrices) {
        return dailyPrices.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Reservation createAndSaveReservation(
        final Long userId,
        final CreateReservationCommand command,
        final BigDecimal totalPrice
    ) {
        final Reservation reservation = Reservation.create(
            userId,
            command.propertyId(),
            command.roomTypeId(),
            command.checkInDate(),
            command.checkOutDate(),
            command.guestName(),
            command.guestPhone(),
            command.guestCount(),
            totalPrice
        );
        return reservationManager.save(reservation);
    }

    private void saveDailyRates(
        final Reservation reservation,
        final Map<LocalDate, BigDecimal> dailyPrices
    ) {
        dailyPrices.forEach((date, price) -> {
            final ReservationDailyRate dailyRate = ReservationDailyRate.create(reservation, date, price);
            reservation.addDailyRate(dailyRate);
        });
    }

    /**
     * 내 예약 목록 조회.
     */
    @Transactional(readOnly = true)
    public PageResult<ReservationResult> getMyReservations(
        final Long userId,
        final String status,
        final Pageable pageable
    ) {
        final Page<Reservation> page = (status != null && !status.isBlank())
            ? reservationReader.findByUserIdAndStatus(userId, status, pageable)
            : reservationReader.findByUserId(userId, pageable);

        return PageResult.from(page.map(this::toResultFromReservation));
    }

    /**
     * 예약 상세 조회. 본인 예약만 조회 가능.
     */
    @Transactional(readOnly = true)
    public ReservationResult getReservation(
        final Long reservationId,
        final Long userId
    ) {
        final Reservation reservation = reservationReader.getById(reservationId);
        reservation.validateOwner(userId);

        return toResultFromReservation(reservation);
    }

    // -- getMyReservations/getReservation private methods --

    private ReservationResult toResultFromReservation(final Reservation reservation) {
        final Property property = propertyReader.getById(reservation.getPropertyId());
        final RoomType roomType = roomTypeReader.getById(reservation.getRoomTypeId());

        return toResult(reservation, property, roomType);
    }

    /**
     * 예약 취소. 본인 예약만 취소 가능. 재고를 복원한다.
     */
    @Transactional
    public CancelReservationResult cancelReservation(
        final Long reservationId,
        final Long userId,
        final String cancelReason
    ) {
        final Reservation reservation = reservationReader.getById(reservationId);
        reservation.validateOwner(userId);
        reservation.cancel(cancelReason);

        restoreInventories(reservation);
        inventoryCache.restore(reservation.getRoomTypeId(),
            reservation.getCheckInDate().datesUntil(reservation.getCheckOutDate()).toList());

        eventPublisher.publishEvent(ReservationCancelledEvent.create(reservation.getId()));

        return CancelReservationResult.from(reservation);
    }

    // -- cancelReservation private methods --

    private void restoreInventories(final Reservation reservation) {
        final List<LocalDate> stayDates = reservation.getCheckInDate()
            .datesUntil(reservation.getCheckOutDate()).toList();
        final Map<LocalDate, Inventory> inventoryMap = inventoryReader
            .findByRoomTypeIdAndDateBetween(
                reservation.getRoomTypeId(), stayDates.getFirst(), stayDates.getLast())
            .stream()
            .collect(Collectors.toMap(Inventory::getDate, i -> i));

        for (final LocalDate date : stayDates) {
            final Inventory inventory = inventoryMap.get(date);
            if (inventory != null) {
                inventory.increaseStock();
            }
        }
    }

    // -- shared private methods --

    private ReservationResult toResult(
        final Reservation reservation,
        final Property property,
        final RoomType roomType
    ) {
        return ReservationResult.from(reservation, property.getName(), property.getAddress(),
            roomType.getName(), property.getThumbnailUrl(),
            property.getCheckInTime(), property.getCheckOutTime());
    }
}
