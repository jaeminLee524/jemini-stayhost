package com.jemini.stayhost.property.presentation.dto;

import com.jemini.stayhost.property.application.dto.PropertyResult;
import com.jemini.stayhost.property.application.dto.PropertyImageResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Builder
@Schema(description = "숙소 상세 응답")
public record PropertyResponse(
    @Schema(description = "숙소 ID", example = "100")
    Long id,

    @Schema(description = "숙소명", example = "스테이호스트 서울 강남점")
    String name,

    @Schema(description = "숙소 유형", example = "HOTEL")
    String type,

    @Schema(description = "숙소 설명")
    String description,

    @Schema(description = "주소")
    String address,

    @Schema(description = "지역", example = "서울")
    String region,

    @Schema(description = "위도", example = "37.4979000")
    BigDecimal latitude,

    @Schema(description = "경도", example = "127.0276000")
    BigDecimal longitude,

    @Schema(description = "체크인 시간", example = "15:00")
    LocalTime checkInTime,

    @Schema(description = "체크아웃 시간", example = "11:00")
    LocalTime checkOutTime,

    @Schema(description = "썸네일 URL")
    String thumbnailUrl,

    @Schema(description = "상태", example = "ACTIVE")
    String status,

    @Schema(description = "숙소 이미지 목록")
    List<ImageResponse> images,

    @Schema(description = "등록일시")
    LocalDateTime createdAt,

    @Schema(description = "수정일시")
    LocalDateTime updatedAt
) {

  @Builder
  @Schema(description = "숙소 이미지")
  public record ImageResponse(
      @Schema(description = "이미지 ID", example = "1")
      Long id,

      @Schema(description = "이미지 URL", example = "https://cdn.example.com/img/1.jpg")
      String imageUrl,

      @Schema(description = "정렬 순서", example = "0")
      Integer sortOrder
  ) {

    public static ImageResponse from(final PropertyImageResult result) {
      return ImageResponse.builder()
          .id(result.id())
          .imageUrl(result.imageUrl())
          .sortOrder(result.sortOrder())
          .build();
    }
  }

  public static PropertyResponse from(final PropertyResult result) {
    return PropertyResponse.builder()
        .id(result.id())
        .name(result.name())
        .type(result.type())
        .description(result.description())
        .address(result.address())
        .region(result.region())
        .latitude(result.latitude())
        .longitude(result.longitude())
        .checkInTime(result.checkInTime())
        .checkOutTime(result.checkOutTime())
        .thumbnailUrl(result.thumbnailUrl())
        .status(result.status())
        .images(result.images().stream().map(ImageResponse::from).toList())
        .createdAt(result.createdAt())
        .updatedAt(result.updatedAt())
        .build();
  }
}
