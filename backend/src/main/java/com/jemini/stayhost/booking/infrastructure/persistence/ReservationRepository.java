package com.jemini.stayhost.booking.infrastructure.persistence;

import com.jemini.stayhost.booking.domain.model.Reservation;
import com.jemini.stayhost.booking.domain.model.ReservationStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Page<Reservation> findByUserId(Long userId, Pageable pageable);

    Page<Reservation> findByUserIdAndStatus(Long userId, ReservationStatus status, Pageable pageable);

    Page<Reservation> findByPropertyIdIn(List<Long> propertyIds, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.id = :id")
    Optional<Reservation> findByIdForUpdate(Long id);

    @Query("""
        SELECT r FROM Reservation r
        WHERE r.propertyId IN :propertyIds
        AND (:propertyId IS NULL OR r.propertyId = :propertyId)
        AND (:status IS NULL OR r.status = :status)
        AND (:checkInFrom IS NULL OR r.checkInDate >= :checkInFrom)
        AND (:checkInTo IS NULL OR r.checkInDate <= :checkInTo)
        """)
    Page<Reservation> findByPropertyIdsWithFilters(
        @Param("propertyIds") List<Long> propertyIds,
        @Param("propertyId") Long propertyId,
        @Param("status") ReservationStatus status,
        @Param("checkInFrom") LocalDate checkInFrom,
        @Param("checkInTo") LocalDate checkInTo,
        Pageable pageable
    );
}
