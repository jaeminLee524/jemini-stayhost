package com.jemini.stayhost.property.presentation.dto;

import com.jemini.stayhost.property.application.dto.RoomTypeResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Schema(description = "객실 유형 응답")
public record RoomTypeResponse(
    @Schema(description = "객실 유형 ID", example = "200")
    Long id,

    @Schema(description = "숙소 ID", example = "100")
    Long propertyId,

    @Schema(description = "객실 유형명", example = "스탠다드 더블")
    String name,

    @Schema(description = "객실 설명")
    String description,

    @Schema(description = "최대 수용 인원", example = "2")
    int maxOccupancy,

    @Schema(description = "기본 가격", example = "120000")
    BigDecimal basePrice,

    @Schema(description = "어메니티 (JSON)", example = "[\"WiFi\", \"TV\"]")
    String amenities,

    @Schema(description = "총 객실 수", example = "10")
    int totalRoomCount,

    @Schema(description = "상태", example = "ACTIVE")
    String status,

    @Schema(description = "등록일시")
    LocalDateTime createdAt,

    @Schema(description = "수정일시")
    LocalDateTime updatedAt
) {

    public static List<RoomTypeResponse> mapToResponse(List<RoomTypeResult> roomTypes) {
        return roomTypes.stream()
            .map(RoomTypeResponse::from)
            .toList();
    }

    public static RoomTypeResponse from(final RoomTypeResult result) {
        return RoomTypeResponse.builder()
            .id(result.id())
            .propertyId(result.propertyId())
            .name(result.name())
            .description(result.description())
            .maxOccupancy(result.maxOccupancy())
            .basePrice(result.basePrice())
            .amenities(result.amenities())
            .totalRoomCount(result.totalRoomCount())
            .status(result.status())
            .createdAt(result.createdAt())
            .updatedAt(result.updatedAt())
            .build();
    }
}
