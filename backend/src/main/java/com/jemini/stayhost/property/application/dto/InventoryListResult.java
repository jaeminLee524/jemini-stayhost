package com.jemini.stayhost.property.application.dto;

import com.jemini.stayhost.property.domain.model.Inventory;

import java.time.LocalDate;
import java.util.List;

public record InventoryListResult(Long roomTypeId, List<InventoryEntry> inventory) {

  public record InventoryEntry(LocalDate date, int totalCount, int reservedCount, int availableCount) {}

  public static InventoryListResult of(final Long roomTypeId, final List<Inventory> inventories) {
    return new InventoryListResult(roomTypeId,
        inventories.stream().map(i -> new InventoryEntry(
            i.getDate(), i.getTotalCount(), i.getReservedCount(), i.getAvailableCount()
        )).toList());
  }
}
