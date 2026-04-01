package com.jemini.stayhost.property.application.dto;

import com.jemini.stayhost.property.domain.model.Rate;
import lombok.Builder;

import java.util.List;

@Builder
public record RateListResult(
    Long roomTypeId,
    List<RateEntryResult> rates
) {

    public static RateListResult of(final Long roomTypeId, final List<Rate> rates) {
        return RateListResult.builder()
            .roomTypeId(roomTypeId)
            .rates(mapToRateLists(rates))
            .build();
    }

    private static List<RateEntryResult> mapToRateLists(List<Rate> rates) {
        return rates.stream()
            .map(RateListResult::mapToRate)
            .toList();
    }

    private static RateEntryResult mapToRate(Rate r) {
        return RateEntryResult.builder()
            .date(r.getDate())
            .price(r.getPrice())
            .build();
    }
}
