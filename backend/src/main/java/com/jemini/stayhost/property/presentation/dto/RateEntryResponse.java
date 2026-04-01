package com.jemini.stayhost.property.presentation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record RateEntryResponse(
    LocalDate date,
    BigDecimal price
) {

}
