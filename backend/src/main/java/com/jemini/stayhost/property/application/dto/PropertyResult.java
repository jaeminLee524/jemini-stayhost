package com.jemini.stayhost.property.application.dto;

import com.jemini.stayhost.property.domain.model.Property;
import com.jemini.stayhost.property.domain.model.PropertyImage;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.jspecify.annotations.NonNull;

@Builder
public record PropertyResult(
    Long id,
    Long partnerId,
    String name,
    String type,
    String description,
    String address,
    String region,
    BigDecimal latitude,
    BigDecimal longitude,
    LocalTime checkInTime,
    LocalTime checkOutTime,
    String thumbnailUrl,
    String status,
    List<ImageResult> images,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    @Builder
    public record ImageResult(
        Long id,
        String imageUrl,
        Integer sortOrder
    ) {

    }

    public static PropertyResult from(final Property property) {
        return PropertyResult.builder()
            .id(property.getId())
            .partnerId(property.getPartnerId())
            .name(property.getName())
            .type(property.getType().name())
            .description(property.getDescription())
            .address(property.getAddress())
            .region(property.getRegion())
            .latitude(property.getLatitude())
            .longitude(property.getLongitude())
            .checkInTime(property.getCheckInTime())
            .checkOutTime(property.getCheckOutTime())
            .thumbnailUrl(property.getThumbnailUrl())
            .status(property.getStatus().name())
            .images(toPropertyImages(property.getImages()))
            .createdAt(property.getCreatedAt())
            .updatedAt(property.getUpdatedAt())
            .build();
    }

    private static List<ImageResult> toPropertyImages(List<PropertyImage> images) {
        return images.stream()
            .map(PropertyResult::toPropertyImage)
            .toList();
    }

    private static ImageResult toPropertyImage(PropertyImage img) {
        return ImageResult.builder()
            .id(img.getId())
            .imageUrl(img.getImageUrl())
            .sortOrder(img.getSortOrder())
            .build();
    }
}
