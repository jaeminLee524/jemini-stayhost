package com.jemini.stayhost.property.presentation.dto;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record InventoryEntryResponse(
    LocalDate date,
    int totalCount,
    int reservedCount,
    int availableCount
) {

}
