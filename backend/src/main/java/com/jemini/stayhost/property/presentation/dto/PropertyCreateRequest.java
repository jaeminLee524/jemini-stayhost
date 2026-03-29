package com.jemini.stayhost.property.presentation.dto;

import com.jemini.stayhost.property.application.dto.PropertyCreateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

@Schema(description = "숙소 등록 요청")
public record PropertyCreateRequest(
    @Schema(description = "숙소명", example = "스테이호스트 서울 강남점")
    @NotBlank @Size(max = 200) String name,

    @Schema(description = "숙소 유형", example = "HOTEL")
    @NotBlank String type,

    @Schema(description = "숙소 설명", example = "강남역 도보 5분, 비즈니스 여행에 최적화된 숙소입니다.")
    @Size(max = 2000) String description,

    @Schema(description = "주소", example = "서울특별시 강남구 테헤란로 123")
    @Size(max = 500) String address,

    @Schema(description = "지역", example = "서울")
    @NotBlank @Size(max = 100) String region,

    @Schema(description = "체크인 시간", example = "15:00")
    LocalTime checkInTime,

    @Schema(description = "체크아웃 시간", example = "11:00")
    LocalTime checkOutTime,

    @Schema(description = "썸네일 URL", example = "https://cdn.example.com/img/thumb.jpg")
    @Size(max = 500) String thumbnailUrl
) {

  public PropertyCreateCommand toCommand() {
    return PropertyCreateCommand.builder()
        .name(this.name)
        .type(this.type)
        .description(this.description)
        .address(this.address)
        .region(this.region)
        .checkInTime(this.checkInTime)
        .checkOutTime(this.checkOutTime)
        .thumbnailUrl(this.thumbnailUrl)
        .build();
  }
}
