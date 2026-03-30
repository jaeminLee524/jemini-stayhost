package com.jemini.stayhost.booking.infrastructure.persistence;

import com.jemini.stayhost.booking.domain.model.Reservation;
import com.jemini.stayhost.booking.domain.model.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Page<Reservation> findByUserId(Long userId, Pageable pageable);

    Page<Reservation> findByUserIdAndStatus(Long userId, ReservationStatus status, Pageable pageable);

    @Modifying
    @Query("""
        UPDATE Reservation r
        SET r.status = 'CANCELLED', r.cancelledAt = :cancelledAt, r.cancelReason = :cancelReason
        WHERE r.id = :id AND r.status = 'CONFIRMED'
        """)
    int cancelById(Long id, LocalDateTime cancelledAt, String cancelReason);
}
