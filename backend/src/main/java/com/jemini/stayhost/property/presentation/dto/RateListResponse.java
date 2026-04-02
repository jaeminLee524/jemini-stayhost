package com.jemini.stayhost.property.presentation.dto;

import com.jemini.stayhost.property.application.dto.RateEntryResult;
import com.jemini.stayhost.property.application.dto.RateListResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "요금 목록 응답")
public record RateListResponse(
    @Schema(description = "객실 유형 ID", example = "10")
    Long roomTypeId,

    @Schema(description = "날짜별 요금 목록")
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
