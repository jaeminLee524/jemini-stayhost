package com.jemini.stayhost.property.application.dto;

import com.jemini.stayhost.property.domain.model.RoomType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record RoomTypeResult(
    Long id,
    Long propertyId,
    String name,
    String description,
    int maxOccupancy,
    BigDecimal basePrice,
    String amenities,
    int totalRoomCount,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static RoomTypeResult from(final RoomType roomType) {
        return RoomTypeResult.builder()
            .id(roomType.getId())
            .propertyId(roomType.getPropertyId())
            .name(roomType.getName())
            .description(roomType.getDescription())
            .maxOccupancy(roomType.getMaxOccupancy())
            .basePrice(roomType.getBasePrice())
            .amenities(roomType.getAmenities())
            .totalRoomCount(roomType.getTotalRoomCount())
            .status(roomType.getStatus().name())
            .createdAt(roomType.getCreatedAt())
            .updatedAt(roomType.getUpdatedAt())
            .build();
    }
}
