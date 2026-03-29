package com.jemini.stayhost.property.application.dto;

import com.jemini.stayhost.property.domain.model.Rate;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record RateListResult(Long roomTypeId, List<RateEntry> rates) {

    @Builder
    public record RateEntry(LocalDate date, BigDecimal price) {}

    public static RateListResult of(final Long roomTypeId, final List<Rate> rates) {
        return RateListResult.builder()
                .roomTypeId(roomTypeId)
                .rates(rates.stream().map(r -> RateEntry.builder()
                        .date(r.getDate())
                        .price(r.getPrice())
                        .build()).toList())
                .build();
    }
}
