package com.jemini.stayhost.property.application.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record RateBulkSetResult(
    Long roomTypeId,
    int appliedDates,
    LocalDate startDate,
    LocalDate endDate,
    BigDecimal price
) {
}
