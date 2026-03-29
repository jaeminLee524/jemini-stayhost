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
}
