package com.jemini.stayhost.property.presentation.dto;

import com.jemini.stayhost.property.application.dto.InventoryEntryResult;
import com.jemini.stayhost.property.application.dto.InventoryListResult;
import lombok.Builder;

import java.util.List;

@Builder
public record InventoryListResponse(
    Long roomTypeId,
    List<InventoryEntryResponse> inventory
) {

    public static InventoryListResponse from(final InventoryListResult result) {
        return InventoryListResponse.builder()
            .roomTypeId(result.roomTypeId())
            .inventory(mapToInventories(result.inventory()))
            .build();
    }

    private static List<InventoryEntryResponse> mapToInventories(List<InventoryEntryResult> inventoryEntryResults) {
        return inventoryEntryResults.stream()
            .map(InventoryListResponse::mapToInventory)
            .toList();
    }

    private static InventoryEntryResponse mapToInventory(InventoryEntryResult e) {
        return InventoryEntryResponse.builder()
            .date(e.date())
            .totalCount(e.totalCount())
            .reservedCount(e.reservedCount())
            .availableCount(e.availableCount())
            .build();
    }
}
