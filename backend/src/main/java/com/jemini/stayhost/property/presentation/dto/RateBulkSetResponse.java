package com.jemini.stayhost.property.presentation.dto;

import com.jemini.stayhost.property.application.dto.RateBulkSetResult;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record RateBulkSetResponse(
    Long roomTypeId,
    int appliedDates,
    LocalDate startDate,
    LocalDate endDate,
    BigDecimal price
) {

    public static RateBulkSetResponse from(final RateBulkSetResult result) {
        return RateBulkSetResponse.builder()
            .roomTypeId(result.roomTypeId())
            .appliedDates(result.appliedDates())
            .startDate(result.startDate())
            .endDate(result.endDate())
            .price(result.price())
            .build();
    }
}
