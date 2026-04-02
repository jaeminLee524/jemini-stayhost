package com.jemini.stayhost.search.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(description = "객실 유형별 요금 항목")
public record RoomTypeRateEntryResponse(
    @Schema(description = "객실 유형 ID", example = "10")
    Long id,

    @Schema(description = "객실 유형명", example = "스탠다드 더블")
    String name,

    @Schema(description = "최대 수용 인원", example = "2")
    int maxOccupancy,

    @Schema(description = "날짜별 요금 및 재고 목록")
    List<DailyRateResponse> rates
) {

}
