package com.jemini.stayhost.property.application.dto;

import com.jemini.stayhost.property.domain.model.Rate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RateListResult(Long roomTypeId, List<RateEntry> rates) {

  public record RateEntry(LocalDate date, BigDecimal price) {}

  public static RateListResult of(final Long roomTypeId, final List<Rate> rates) {
    return new RateListResult(roomTypeId,
        rates.stream().map(r -> new RateEntry(r.getDate(), r.getPrice())).toList());
  }
}
