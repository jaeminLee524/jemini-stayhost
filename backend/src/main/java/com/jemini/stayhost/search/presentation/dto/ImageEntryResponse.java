package com.jemini.stayhost.search.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "이미지 항목")
public record ImageEntryResponse(
    @Schema(description = "이미지 URL", example = "https://cdn.example.com/img/1.jpg")
    String imageUrl,

    @Schema(description = "정렬 순서", example = "0")
    int sortOrder
) {

}
