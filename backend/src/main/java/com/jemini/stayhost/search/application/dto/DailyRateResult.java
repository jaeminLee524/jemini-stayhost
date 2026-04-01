package com.jemini.stayhost.search.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record DailyRateResult(
    LocalDate date,
    BigDecimal price,
    boolean available
) {

    public static DailyRateResult create(
        final LocalDate date,
        final BigDecimal price,
        final boolean available
    ) {
        return DailyRateResult.builder()
            .date(date)
            .price(price)
            .available(available)
            .build();
    }
}
