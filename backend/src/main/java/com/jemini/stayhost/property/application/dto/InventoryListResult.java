package com.jemini.stayhost.property.application.dto;

import com.jemini.stayhost.property.domain.model.Inventory;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;

@Builder
public record InventoryListResult(
    Long roomTypeId,
    List<InventoryEntry> inventory
) {

    @Builder
    public record InventoryEntry(
        LocalDate date,
        int totalCount,
        int reservedCount,
        int availableCount
    ) {

    }

    public static InventoryListResult of(final Long roomTypeId, final List<Inventory> inventories) {
        return InventoryListResult.builder()
            .roomTypeId(roomTypeId)
            .inventory(mapToInventories(inventories))
            .build();
    }

    private static List<InventoryEntry> mapToInventories(List<Inventory> inventories) {
        return inventories.stream()
            .map(InventoryListResult::toInventory)
            .toList();
    }

    private static InventoryEntry toInventory(Inventory inventory) {
        return InventoryEntry.builder()
            .date(inventory.getDate())
            .totalCount(inventory.getTotalCount())
            .reservedCount(inventory.getReservedCount())
            .availableCount(inventory.getAvailableCount())
            .build();
    }
}
