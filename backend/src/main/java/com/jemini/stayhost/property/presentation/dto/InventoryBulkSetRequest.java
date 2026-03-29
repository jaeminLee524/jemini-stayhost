package com.jemini.stayhost.property.presentation.dto;

import com.jemini.stayhost.property.application.dto.InventoryBulkSetCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "재고 일괄 설정 요청")
public record InventoryBulkSetRequest(
        @Schema(description = "시작일", example = "2026-04-01")
        @NotNull LocalDate startDate,

        @Schema(description = "종료일", example = "2026-04-30")
        @NotNull LocalDate endDate,

        @Schema(description = "총 객실 수", example = "10")
        @NotNull @Min(1) Integer totalCount
) {

    public InventoryBulkSetCommand toCommand() {
        return InventoryBulkSetCommand.builder()
                .startDate(this.startDate)
                .endDate(this.endDate)
                .totalCount(this.totalCount)
                .build();
    }
}
