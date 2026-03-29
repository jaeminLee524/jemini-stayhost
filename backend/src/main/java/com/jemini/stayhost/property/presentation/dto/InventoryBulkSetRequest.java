package com.jemini.stayhost.property.presentation.dto;

import com.jemini.stayhost.common.exception.BusinessException;
import com.jemini.stayhost.common.exception.ErrorCode;
import com.jemini.stayhost.property.application.dto.InventoryBulkSetCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Schema(description = "재고 일괄 설정 요청")
public record InventoryBulkSetRequest(
        @Schema(description = "시작일", example = "2026-04-01")
        @NotNull LocalDate startDate,

        @Schema(description = "종료일", example = "2026-04-30")
        @NotNull LocalDate endDate,

        @Schema(description = "총 객실 수", example = "10")
        @NotNull @Min(1) Integer totalCount
) {

    private static final int MAX_DATE_RANGE_DAYS = 30;

    public InventoryBulkSetCommand toCommand() {
        validateDateRange();
        return InventoryBulkSetCommand.builder()
                .startDate(this.startDate)
                .endDate(this.endDate)
                .totalCount(this.totalCount)
                .build();
    }

    private void validateDateRange() {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE);
        }
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (days > MAX_DATE_RANGE_DAYS) {
            throw new BusinessException(ErrorCode.DATE_RANGE_TOO_LONG);
        }
    }
}
