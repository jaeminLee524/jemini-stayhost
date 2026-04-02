package com.jemini.stayhost.property.application.dto;

import com.jemini.stayhost.property.domain.model.RoomType;
import com.jemini.stayhost.property.domain.model.RoomTypeImage;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    List<RoomTypeImageResult> images,
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
            .images(mapToImages(roomType.getImages()))
            .createdAt(roomType.getCreatedAt())
            .updatedAt(roomType.getUpdatedAt())
            .build();
    }

    private static List<RoomTypeImageResult> mapToImages(final List<RoomTypeImage> images) {
        return images.stream()
            .map(RoomTypeResult::mapToImage)
            .toList();
    }

    private static RoomTypeImageResult mapToImage(final RoomTypeImage img) {
        return RoomTypeImageResult.builder()
            .id(img.getId())
            .imageUrl(img.getImageUrl())
            .sortOrder(img.getSortOrder())
            .build();
    }
}
