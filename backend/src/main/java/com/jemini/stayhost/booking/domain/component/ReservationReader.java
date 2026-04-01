package com.jemini.stayhost.booking.domain.component;

import com.jemini.stayhost.booking.domain.model.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservationReader {

    Reservation getById(Long id);

    Reservation getByIdWithLock(Long id);

    Page<Reservation> findByUserId(Long userId, Pageable pageable);

    Page<Reservation> findByUserIdAndStatus(Long userId, String status, Pageable pageable);
}
