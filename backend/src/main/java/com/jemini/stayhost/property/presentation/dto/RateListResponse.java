package com.jemini.stayhost.property.presentation.dto;

import com.jemini.stayhost.property.application.dto.RateListResult;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record RateListResponse(Long roomTypeId, List<RateEntry> rates) {

    @Builder
    public record RateEntry(LocalDate date, BigDecimal price) {}

    public static RateListResponse from(final RateListResult result) {
        return RateListResponse.builder()
                .roomTypeId(result.roomTypeId())
                .rates(result.rates().stream().map(e -> RateEntry.builder()
                        .date(e.date())
                        .price(e.price())
                        .build()).toList())
                .build();
    }
}
