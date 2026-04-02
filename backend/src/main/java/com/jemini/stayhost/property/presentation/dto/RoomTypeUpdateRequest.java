package com.jemini.stayhost.property.presentation.dto;

import com.jemini.stayhost.property.application.dto.RoomTypeUpdateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "객실 유형 수정 요청")
public record RoomTypeUpdateRequest(
    @Schema(description = "객실 유형명", example = "스탠다드 더블 (개선)")
    @Size(max = 200) String name,

    @Schema(description = "객실 설명", example = "새롭게 리뉴얼된 객실입니다.")
    @Size(max = 2000) String description,

    @Schema(description = "최대 수용 인원", example = "2")
    @Min(1) Integer maxOccupancy,

    @Schema(description = "기본 가격", example = "130000")
    BigDecimal basePrice,

    @Schema(description = "어메니티 목록", example = "[\"WiFi\", \"TV\", \"에어컨\", \"냉장고\", \"스마트 TV\"]")
    List<String> amenities,

    @Schema(description = "객실 이미지 URL 목록")
    List<String> imageUrls
) {

    public RoomTypeUpdateCommand toCommand() {
        return RoomTypeUpdateCommand.builder()
            .name(this.name)
            .description(this.description)
            .maxOccupancy(this.maxOccupancy)
            .basePrice(this.basePrice)
            .amenities(this.amenities)
            .imageUrls(this.imageUrls)
            .build();
    }
}
