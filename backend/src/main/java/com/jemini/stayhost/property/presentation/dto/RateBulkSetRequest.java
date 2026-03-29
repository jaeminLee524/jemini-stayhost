package com.jemini.stayhost.property.presentation.dto;

import com.jemini.stayhost.property.application.dto.RateBulkSetCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "요금 일괄 설정 요청")
public record RateBulkSetRequest(
    @Schema(description = "시작일", example = "2026-04-01")
    @NotNull LocalDate startDate,

    @Schema(description = "종료일", example = "2026-04-30")
    @NotNull LocalDate endDate,

    @Schema(description = "요금", example = "150000")
    @NotNull BigDecimal price,

    @Schema(description = "적용 요일 (1=월, 7=일). 생략 시 전체 날짜에 적용", example = "[1,2,3,4,5]")
    List<Integer> daysOfWeek
) {

  public RateBulkSetCommand toCommand() {
    return RateBulkSetCommand.builder()
        .startDate(this.startDate)
        .endDate(this.endDate)
        .price(this.price)
        .daysOfWeek(this.daysOfWeek)
        .build();
  }
}
