package com.jemini.stayhost.property.application.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record InventoryBulkSetResult(
        Long roomTypeId,
        int appliedDates,
        LocalDate startDate,
        LocalDate endDate,
        int totalCount
) {
}
