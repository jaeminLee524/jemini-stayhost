package com.jemini.stayhost.booking.infrastructure.component;

import com.jemini.stayhost.booking.domain.component.ReservationManager;
import com.jemini.stayhost.booking.domain.model.Reservation;
import com.jemini.stayhost.booking.infrastructure.persistence.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationManagerImpl implements ReservationManager {

    private final ReservationRepository reservationRepository;

    @Override
    public Reservation save(final Reservation reservation) {
        return reservationRepository.save(reservation);
    }
}
