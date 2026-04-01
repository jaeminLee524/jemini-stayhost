package com.jemini.stayhost.property.presentation.dto;

import static com.jemini.stayhost.common.util.DateUtil.dayCountInclusive;

import com.jemini.stayhost.common.exception.BusinessException;
import com.jemini.stayhost.common.exception.ErrorCode;
import com.jemini.stayhost.property.application.dto.RateBulkSetCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

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
    @NotNull @Positive BigDecimal price,

    @Schema(description = "적용 요일 (1=월, 7=일). 생략 시 전체 날짜에 적용", example = "[1,2,3,4,5]")
    List<Integer> daysOfWeek
) {

    private static final int MAX_DATE_RANGE_DAYS = 30;

    public RateBulkSetCommand toCommand() {
        validateDateRange();
        return RateBulkSetCommand.builder()
            .startDate(this.startDate)
            .endDate(this.endDate)
            .price(this.price)
            .daysOfWeek(this.daysOfWeek)
            .build();
    }

    private void validateDateRange() {
        if (this.startDate.isAfter(this.endDate)) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE);
        }
        final long days = dayCountInclusive(this.startDate, this.endDate);
        if (days > MAX_DATE_RANGE_DAYS) {
            throw new BusinessException(ErrorCode.DATE_RANGE_TOO_LONG);
        }
    }
}
