package com.jemini.stayhost.property.presentation.dto;

import com.jemini.stayhost.property.application.dto.RateBulkSetResult;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RateBulkSetResponse(
    Long roomTypeId,
    int appliedDates,
    LocalDate startDate,
    LocalDate endDate,
    BigDecimal price
) {

  public static RateBulkSetResponse from(final RateBulkSetResult result) {
    return new RateBulkSetResponse(
        result.roomTypeId(),
        result.appliedDates(),
        result.startDate(),
        result.endDate(),
        result.price()
    );
  }
}
