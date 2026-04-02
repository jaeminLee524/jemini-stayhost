package com.jemini.stayhost.property.presentation.dto;

import com.jemini.stayhost.property.application.dto.InventoryBulkSetResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;

@Builder
@Schema(description = "재고 일괄 설정 응답")
public record InventoryBulkSetResponse(
    @Schema(description = "객실 유형 ID", example = "10")
    Long roomTypeId,

    @Schema(description = "적용된 날짜 수", example = "7")
    int appliedDates,

    @Schema(description = "시작 날짜", example = "2024-07-01")
    LocalDate startDate,

    @Schema(description = "종료 날짜", example = "2024-07-07")
    LocalDate endDate,

    @Schema(description = "설정된 재고 수", example = "5")
    int totalCount
) {

    public static InventoryBulkSetResponse from(final InventoryBulkSetResult result) {
        return InventoryBulkSetResponse.builder()
            .roomTypeId(result.roomTypeId())
            .appliedDates(result.appliedDates())
            .startDate(result.startDate())
            .endDate(result.endDate())
            .totalCount(result.totalCount())
            .build();
    }
}
