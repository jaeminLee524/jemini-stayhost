package com.jemini.stayhost.property.domain.component;

import com.jemini.stayhost.property.domain.model.Inventory;

import java.time.LocalDate;
import java.util.List;

public interface InventoryReader {

    List<Inventory> findByRoomTypeIdAndDateBetween(Long roomTypeId, LocalDate startDate, LocalDate endDate);

    List<Inventory> findByRoomTypeIdsAndDateBetween(List<Long> roomTypeIds, LocalDate startDate, LocalDate endDate);

    List<Inventory> findByRoomTypeIdAndDateRangeForUpdate(Long roomTypeId, LocalDate checkIn, LocalDate checkOut);
}
