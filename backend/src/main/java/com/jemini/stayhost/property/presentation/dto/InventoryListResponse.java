package com.jemini.stayhost.property.presentation.dto;

import com.jemini.stayhost.property.application.dto.InventoryListResult;

import java.time.LocalDate;
import java.util.List;

public record InventoryListResponse(Long roomTypeId, List<InventoryEntry> inventory) {

  public record InventoryEntry(LocalDate date, int totalCount, int reservedCount, int availableCount) {}

  public static InventoryListResponse from(final InventoryListResult result) {
    return new InventoryListResponse(result.roomTypeId(),
        result.inventory().stream().map(e -> new InventoryEntry(
            e.date(), e.totalCount(), e.reservedCount(), e.availableCount()
        )).toList());
  }
}
