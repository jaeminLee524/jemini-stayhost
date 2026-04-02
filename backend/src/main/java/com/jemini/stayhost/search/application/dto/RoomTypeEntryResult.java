package com.jemini.stayhost.search.application.dto;

import com.jemini.stayhost.property.domain.model.RoomType;
import com.jemini.stayhost.property.domain.model.RoomTypeImage;
import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;

@Builder
public record RoomTypeEntryResult(
    Long id,
    String name,
    String description,
    int maxOccupancy,
    BigDecimal basePrice,
    String amenities,
    List<RoomTypeImageEntryResult> images
) {

    public static RoomTypeEntryResult from(final RoomType roomType) {
        return RoomTypeEntryResult.builder()
            .id(roomType.getId())
            .name(roomType.getName())
            .description(roomType.getDescription())
            .maxOccupancy(roomType.getMaxOccupancy())
            .basePrice(roomType.getBasePrice())
            .amenities(roomType.getAmenities())
            .images(mapToImages(roomType.getImages()))
            .build();
    }

    private static List<RoomTypeImageEntryResult> mapToImages(final List<RoomTypeImage> images) {
        return images.stream()
            .map(RoomTypeEntryResult::mapToImage)
            .toList();
    }

    private static RoomTypeImageEntryResult mapToImage(RoomTypeImage image) {
        return RoomTypeImageEntryResult.builder()
            .imageUrl(image.getImageUrl())
            .sortOrder(image.getSortOrder())
            .build();
    }
}
