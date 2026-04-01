package com.jemini.stayhost.search.infrastructure.component;

import com.jemini.stayhost.property.domain.model.Inventory;
import com.jemini.stayhost.property.infrastructure.persistence.InventoryRepository;
import com.jemini.stayhost.search.domain.component.InventoryReaderV2;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InventoryReaderV2Impl implements InventoryReaderV2 {

    private final InventoryRepository inventoryRepository;

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
}
