package com.jemini.stayhost.property.presentation.dto;

import com.jemini.stayhost.property.application.dto.RateBulkSetResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Schema(description = "요금 일괄 설정 응답")
public record RateBulkSetResponse(
    @Schema(description = "객실 유형 ID", example = "10")
    Long roomTypeId,

    @Schema(description = "적용된 날짜 수", example = "7")
    int appliedDates,

    @Schema(description = "시작 날짜", example = "2024-07-01")
    LocalDate startDate,

    @Schema(description = "종료 날짜", example = "2024-07-07")
    LocalDate endDate,

    @Schema(description = "설정된 요금", example = "100000")
    BigDecimal price
) {

    public static RateBulkSetResponse from(final RateBulkSetResult result) {
        return RateBulkSetResponse.builder()
            .roomTypeId(result.roomTypeId())
            .appliedDates(result.appliedDates())
            .startDate(result.startDate())
            .endDate(result.endDate())
            .price(result.price())
            .build();
    }
}
