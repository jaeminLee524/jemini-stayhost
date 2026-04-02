package com.jemini.stayhost.property.infrastructure.component;

import com.jemini.stayhost.property.domain.component.InventoryReader;
import com.jemini.stayhost.property.domain.model.Inventory;
import com.jemini.stayhost.property.infrastructure.persistence.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InventoryReaderImpl implements InventoryReader {

    private final InventoryRepository inventoryRepository;

    @Override
    public List<Inventory> findByRoomTypeIdAndDateBetween(
            final Long roomTypeId,
            final LocalDate startDate,
            final LocalDate endDate
    ) {
        return inventoryRepository.findByRoomTypeIdAndDateBetween(roomTypeId, startDate, endDate);
    }

    @Override
    public List<Inventory> findByRoomTypeIdsAndDateBetween(
            final List<Long> roomTypeIds,
            final LocalDate startDate,
            final LocalDate endDate
    ) {
        if (roomTypeIds.isEmpty()) {
            return List.of();
        }
        return inventoryRepository.findByRoomTypeIdInAndDateBetween(roomTypeIds, startDate, endDate);
    }

    @Override
    public List<Inventory> findByRoomTypeIdAndDateRangeForUpdate(
            final Long roomTypeId,
            final LocalDate checkIn,
            final LocalDate checkOut
    ) {
        return inventoryRepository.findByRoomTypeIdAndDateRangeForUpdate(roomTypeId, checkIn, checkOut);
    }
}
