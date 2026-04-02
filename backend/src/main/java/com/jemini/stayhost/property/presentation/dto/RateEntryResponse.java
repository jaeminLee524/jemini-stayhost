package com.jemini.stayhost.property.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Schema(description = "날짜별 요금 항목")
public record RateEntryResponse(
    @Schema(description = "날짜", example = "2024-07-01")
    LocalDate date,

    @Schema(description = "요금", example = "100000")
    BigDecimal price
) {

}
