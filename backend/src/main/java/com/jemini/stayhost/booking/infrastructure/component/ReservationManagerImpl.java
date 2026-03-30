package com.jemini.stayhost.booking.infrastructure.component;

import com.jemini.stayhost.booking.domain.component.ReservationManager;
import com.jemini.stayhost.booking.domain.model.Reservation;
import com.jemini.stayhost.booking.domain.model.ReservationStatus;
import com.jemini.stayhost.booking.infrastructure.persistence.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ReservationManagerImpl implements ReservationManager {

    private final ReservationRepository reservationRepository;

    @Override
    public Reservation save(final Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    @Override
    public int cancel(final Long id, final String cancelReason) {
        return reservationRepository.updateStatus(id, ReservationStatus.CONFIRMED, ReservationStatus.CANCELLED,LocalDateTime.now(), cancelReason);
    }
}
