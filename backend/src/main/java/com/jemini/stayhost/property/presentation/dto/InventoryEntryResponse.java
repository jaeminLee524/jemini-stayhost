package com.jemini.stayhost.property.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;

@Builder
@Schema(description = "날짜별 재고 항목")
public record InventoryEntryResponse(
    @Schema(description = "날짜", example = "2024-07-01")
    LocalDate date,

    @Schema(description = "전체 재고 수", example = "5")
    int totalCount,

    @Schema(description = "예약된 재고 수", example = "2")
    int reservedCount,

    @Schema(description = "예약 가능 재고 수", example = "3")
    int availableCount
) {

}
