package com.jemini.stayhost.property.presentation.dto;

import com.jemini.stayhost.property.application.dto.RoomTypeCreateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "객실 유형 등록 요청")
public record RoomTypeCreateRequest(
    @Schema(description = "객실 유형명", example = "스탠다드 더블")
    @NotBlank @Size(max = 200) String name,

    @Schema(description = "객실 설명", example = "편안한 더블 침대가 있는 표준 객실입니다.")
    @Size(max = 2000) String description,

    @Schema(description = "최대 수용 인원", example = "2")
    @NotNull @Min(1) Integer maxOccupancy,

    @Schema(description = "기본 가격", example = "120000")
    @NotNull BigDecimal basePrice,

    @Schema(description = "어메니티 목록", example = "[\"WiFi\", \"TV\", \"에어컨\", \"냉장고\"]")
    List<String> amenities,

    @Schema(description = "총 객실 수", example = "10")
    @NotNull @Min(1) Integer totalRoomCount
) {

  public RoomTypeCreateCommand toCommand() {
    return RoomTypeCreateCommand.builder()
        .name(this.name)
        .description(this.description)
        .maxOccupancy(this.maxOccupancy)
        .basePrice(this.basePrice)
        .amenities(this.amenities)
        .totalRoomCount(this.totalRoomCount)
        .build();
  }
}
