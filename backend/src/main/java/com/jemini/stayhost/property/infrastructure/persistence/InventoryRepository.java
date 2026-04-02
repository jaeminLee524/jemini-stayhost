package com.jemini.stayhost.property.infrastructure.persistence;

import com.jemini.stayhost.property.domain.model.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    List<Inventory> findByRoomTypeIdAndDateBetween(Long roomTypeId, LocalDate startDate, LocalDate endDate);

    List<Inventory> findByDateBetween(LocalDate startDate, LocalDate endDate);

    List<Inventory> findByRoomTypeIdInAndDateBetween(List<Long> roomTypeIds, LocalDate startDate, LocalDate endDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT i FROM Inventory i
        WHERE i.roomTypeId = :roomTypeId
          AND i.date BETWEEN :checkIn AND :checkOut
        ORDER BY i.roomTypeId, i.date
        """)
    List<Inventory> findByRoomTypeIdAndDateRangeForUpdate(
        @Param("roomTypeId") Long roomTypeId,
        @Param("checkIn") LocalDate checkIn,
        @Param("checkOut") LocalDate checkOut
    );
}
