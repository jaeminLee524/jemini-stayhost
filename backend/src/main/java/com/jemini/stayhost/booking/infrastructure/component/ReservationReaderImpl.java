package com.jemini.stayhost.booking.infrastructure.component;

import com.jemini.stayhost.booking.domain.component.ReservationReader;
import com.jemini.stayhost.booking.domain.model.Reservation;
import com.jemini.stayhost.booking.domain.model.ReservationStatus;
import com.jemini.stayhost.booking.infrastructure.persistence.ReservationRepository;
import com.jemini.stayhost.common.exception.ErrorCode;
import com.jemini.stayhost.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservationReaderImpl implements ReservationReader {

    private final ReservationRepository reservationRepository;

    @Override
    public Reservation getById(final Long id) {
        return reservationRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    @Override
    public Reservation getByIdForUpdate(final Long id) {
        return reservationRepository.findByIdForUpdate(id)
            .orElseThrow(() -> new NotFoundException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    @Override
    public Page<Reservation> findByUserId(final Long userId, final Pageable pageable) {
        return reservationRepository.findByUserId(userId, pageable);
    }

    @Override
    public Page<Reservation> findByUserIdAndStatus(
        final Long userId,
        final String status,
        final Pageable pageable
    ) {
        return reservationRepository.findByUserIdAndStatus(userId, ReservationStatus.valueOf(status), pageable);
    }

    @Override
    public Page<Reservation> findByPropertyIds(final List<Long> propertyIds, final Pageable pageable) {
        return reservationRepository.findByPropertyIdIn(propertyIds, pageable);
    }

    @Override
    public Page<Reservation> findByPropertyIdsWithFilters(
        final List<Long> propertyIds,
        final Long propertyId,
        final String status,
        final LocalDate checkInFrom,
        final LocalDate checkInTo,
        final Pageable pageable
    ) {
        final ReservationStatus reservationStatus = status != null ? ReservationStatus.valueOf(status) : null;
        return reservationRepository.findByPropertyIdsWithFilters(
            propertyIds, propertyId, reservationStatus, checkInFrom, checkInTo, pageable);
    }
}
