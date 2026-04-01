package com.jemini.stayhost.booking.infrastructure.persistence;

import com.jemini.stayhost.booking.domain.model.Reservation;
import com.jemini.stayhost.booking.domain.model.ReservationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Page<Reservation> findByUserId(Long userId, Pageable pageable);

    Page<Reservation> findByUserIdAndStatus(Long userId, ReservationStatus status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.id = :id")
    Optional<Reservation> findByIdWithLock(Long id);
}
