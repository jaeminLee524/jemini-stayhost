package com.jemini.stayhost.search.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
@Schema(description = "객실 유형 항목")
public record RoomTypeEntryResponse(
    @Schema(description = "객실 유형 ID", example = "10")
    Long id,

    @Schema(description = "객실 유형명", example = "스탠다드 더블")
    String name,

    @Schema(description = "객실 설명")
    String description,

    @Schema(description = "최대 수용 인원", example = "2")
    int maxOccupancy,

    @Schema(description = "기본 요금", example = "100000")
    BigDecimal basePrice,

    @Schema(description = "편의시설 목록", example = "와이파이,에어컨,냉장고")
    String amenities
) {

}
