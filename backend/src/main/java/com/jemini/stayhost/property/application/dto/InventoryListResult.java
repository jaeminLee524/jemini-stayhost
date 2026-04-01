package com.jemini.stayhost.property.application.dto;

import com.jemini.stayhost.property.domain.model.Inventory;
import java.util.List;
import lombok.Builder;

@Builder
public record InventoryListResult(
    Long roomTypeId,
    List<InventoryEntryResult> inventory
) {

    public static InventoryListResult of(final Long roomTypeId, final List<Inventory> inventories) {
        return InventoryListResult.builder()
            .roomTypeId(roomTypeId)
            .inventory(mapToInventories(inventories))
            .build();
    }

    private static List<InventoryEntryResult> mapToInventories(List<Inventory> inventories) {
        return inventories.stream()
            .map(InventoryListResult::toInventory)
            .toList();
    }

    private static InventoryEntryResult toInventory(Inventory inventory) {
        return InventoryEntryResult.builder()
            .date(inventory.getDate())
            .totalCount(inventory.getTotalCount())
            .reservedCount(inventory.getReservedCount())
            .availableCount(inventory.getAvailableCount())
            .build();
    }
}
