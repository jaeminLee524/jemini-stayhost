package com.jemini.stayhost.property.presentation.dto;

import com.jemini.stayhost.property.application.dto.InventoryListResult;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record InventoryListResponse(Long roomTypeId, List<InventoryEntry> inventory) {

    @Builder
    public record InventoryEntry(LocalDate date, int totalCount, int reservedCount, int availableCount) {

    }

    public static InventoryListResponse from(final InventoryListResult result) {
        return InventoryListResponse.builder()
            .roomTypeId(result.roomTypeId())
            .inventory(result.inventory().stream().map(e -> InventoryEntry.builder()
                .date(e.date())
                .totalCount(e.totalCount())
                .reservedCount(e.reservedCount())
                .availableCount(e.availableCount())
                .build()).toList())
            .build();
    }
}
