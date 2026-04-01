package com.jemini.stayhost.search.presentation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record DailyRateResponse(
    LocalDate date,
    BigDecimal price,
    boolean available
) {

}
