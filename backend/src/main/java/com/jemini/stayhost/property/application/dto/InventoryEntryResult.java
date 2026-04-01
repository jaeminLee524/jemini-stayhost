package com.jemini.stayhost.property.application.dto;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record InventoryEntryResult(
    LocalDate date,
    int totalCount,
    int reservedCount,
    int availableCount
) {

}
