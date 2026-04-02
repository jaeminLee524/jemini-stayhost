package com.jemini.stayhost.search.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;

@Builder
@Schema(description = "날짜별 요금 및 재고 현황")
public record DailyRateResponse(
    @Schema(description = "날짜", example = "2024-07-01")
    LocalDate date,

    @Schema(description = "요금", example = "100000")
    BigDecimal price,

    @Schema(description = "예약 가능 여부", example = "true")
    boolean available
) {

}
