package com.jemini.stayhost.property.presentation.dto;

import com.jemini.stayhost.property.application.dto.InventoryBulkSetResult;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record InventoryBulkSetResponse(
        Long roomTypeId,
        int appliedDates,
        LocalDate startDate,
        LocalDate endDate,
        int totalCount
) {

    public static InventoryBulkSetResponse from(final InventoryBulkSetResult result) {
        return InventoryBulkSetResponse.builder()
                .roomTypeId(result.roomTypeId())
                .appliedDates(result.appliedDates())
                .startDate(result.startDate())
                .endDate(result.endDate())
                .totalCount(result.totalCount())
                .build();
    }
}
