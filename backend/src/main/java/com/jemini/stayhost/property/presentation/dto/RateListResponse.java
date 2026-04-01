package com.jemini.stayhost.property.presentation.dto;

import com.jemini.stayhost.property.application.dto.RateEntryResult;
import com.jemini.stayhost.property.application.dto.RateListResult;
import lombok.Builder;

import java.util.List;

@Builder
public record RateListResponse(
    Long roomTypeId,
    List<RateEntryResponse> rates
) {

    public static RateListResponse from(final RateListResult result) {
        return RateListResponse.builder()
            .roomTypeId(result.roomTypeId())
            .rates(mapToRates(result))
            .build();
    }

    private static List<RateEntryResponse> mapToRates(RateListResult result) {
        return result.rates().stream()
            .map(RateListResponse::mapToRate)
            .toList();
    }

    private static RateEntryResponse mapToRate(RateEntryResult e) {
        return RateEntryResponse.builder()
            .date(e.date())
            .price(e.price())
            .build();
    }
}
