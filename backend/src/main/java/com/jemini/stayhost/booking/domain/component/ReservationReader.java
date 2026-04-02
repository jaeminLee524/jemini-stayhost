package com.jemini.stayhost.booking.domain.component;

import com.jemini.stayhost.booking.domain.model.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ReservationReader {

    Reservation getById(Long id);

    Reservation getByIdForUpdate(Long id);

    Page<Reservation> findByUserId(Long userId, Pageable pageable);

    Page<Reservation> findByUserIdAndStatus(Long userId, String status, Pageable pageable);

    Page<Reservation> findByPropertyIds(List<Long> propertyIds, Pageable pageable);

    Page<Reservation> findByPropertyIdsWithFilters(
        List<Long> propertyIds,
        Long propertyId,
        String status,
        LocalDate checkInFrom,
        LocalDate checkInTo,
        Pageable pageable
    );
}
