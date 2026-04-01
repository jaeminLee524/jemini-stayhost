package com.jemini.stayhost.search.domain.component;

import com.jemini.stayhost.property.domain.model.Inventory;

import java.time.LocalDate;
import java.util.List;

public interface InventoryReaderV2 {

    List<Inventory> findByRoomTypeIdsAndDateBetween(List<Long> roomTypeIds, LocalDate startDate, LocalDate endDate);
}
