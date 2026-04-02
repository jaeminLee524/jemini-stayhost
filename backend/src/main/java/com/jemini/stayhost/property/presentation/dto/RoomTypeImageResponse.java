package com.jemini.stayhost.property.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "객실 이미지")
public record RoomTypeImageResponse(
    @Schema(description = "이미지 ID", example = "1")
    Long id,

    @Schema(description = "이미지 URL", example = "https://cdn.example.com/img/room1.jpg")
    String imageUrl,

    @Schema(description = "정렬 순서", example = "0")
    Integer sortOrder
) {

}
