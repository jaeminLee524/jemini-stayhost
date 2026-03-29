package com.jemini.stayhost.property.presentation.dto;

import com.jemini.stayhost.property.application.dto.RateListResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RateListResponse(Long roomTypeId, List<RateEntry> rates) {

  public record RateEntry(LocalDate date, BigDecimal price) {}

  public static RateListResponse from(final RateListResult result) {
    return new RateListResponse(result.roomTypeId(),
        result.rates().stream().map(e -> new RateEntry(e.date(), e.price())).toList());
  }
}
