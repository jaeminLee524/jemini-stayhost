package com.jemini.stayhost.property.presentation.dto;

import com.jemini.stayhost.property.application.dto.InventoryBulkSetResult;

import java.time.LocalDate;

public record InventoryBulkSetResponse(
    Long roomTypeId,
    int appliedDates,
    LocalDate startDate,
    LocalDate endDate,
    int totalCount
) {

  public static InventoryBulkSetResponse from(final InventoryBulkSetResult result) {
    return new InventoryBulkSetResponse(
        result.roomTypeId(),
        result.appliedDates(),
        result.startDate(),
        result.endDate(),
        result.totalCount()
    );
  }
}
