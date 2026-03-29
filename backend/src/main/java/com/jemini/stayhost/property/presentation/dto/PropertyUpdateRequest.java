package com.jemini.stayhost.property.presentation.dto;

import com.jemini.stayhost.property.application.dto.PropertyUpdateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

@Schema(description = "숙소 정보 수정 요청")
public record PropertyUpdateRequest(
    @Schema(description = "숙소명", example = "스테이호스트 서울 강남점 (리뉴얼)")
    @Size(max = 200) String name,

    @Schema(description = "숙소 설명", example = "2026년 리뉴얼 오픈!")
    @Size(max = 2000) String description,

    @Schema(description = "체크인 시간", example = "14:00")
    LocalTime checkInTime,

    @Schema(description = "체크아웃 시간", example = "12:00")
    LocalTime checkOutTime,

    @Schema(description = "썸네일 URL", example = "https://cdn.example.com/img/new_thumb.jpg")
    @Size(max = 500) String thumbnailUrl
) {

  public PropertyUpdateCommand toCommand() {
    return PropertyUpdateCommand.builder()
        .name(this.name)
        .description(this.description)
        .checkInTime(this.checkInTime)
        .checkOutTime(this.checkOutTime)
        .thumbnailUrl(this.thumbnailUrl)
        .build();
  }
}
