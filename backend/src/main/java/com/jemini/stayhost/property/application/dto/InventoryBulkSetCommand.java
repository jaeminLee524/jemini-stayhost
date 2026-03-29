package com.jemini.stayhost.property.application.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record InventoryBulkSetCommand(
        LocalDate startDate,
        LocalDate endDate,
        int totalCount
) {
}
