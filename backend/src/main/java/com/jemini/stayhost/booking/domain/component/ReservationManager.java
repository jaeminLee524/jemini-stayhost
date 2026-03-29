package com.jemini.stayhost.booking.domain.component;

import com.jemini.stayhost.booking.domain.model.Reservation;

public interface ReservationManager {

    Reservation save(Reservation reservation);
}
