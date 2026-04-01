package com.jemini.stayhost.property.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record RateEntryResult(
    LocalDate date,
    BigDecimal price
) {

}
