package com.jemini.stayhost.search.application.dto;

import com.jemini.stayhost.property.domain.model.Property;
import com.jemini.stayhost.property.domain.model.PropertyImage;
import com.jemini.stayhost.property.domain.model.RoomType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Builder
public record PropertyDetailResult(
    Long id,
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
    List<PropertyImageEntryResult> images,
    List<RoomTypeEntryResult> roomTypes
) {

    public static PropertyDetailResult from(
        final Property property,
        final List<RoomType> roomTypes
    ) {
        return PropertyDetailResult.builder()
            .id(property.getId())
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
            .images(mapToImages(property.getImages()))
            .roomTypes(mapToRoomTypes(roomTypes))
            .build();
    }

    private static List<PropertyImageEntryResult> mapToImages(final List<PropertyImage> images) {
        return images.stream()
            .map(PropertyDetailResult::mapToImage)
            .toList();
    }

    private static PropertyImageEntryResult mapToImage(final PropertyImage img) {
        return PropertyImageEntryResult.builder()
            .imageUrl(img.getImageUrl())
            .sortOrder(img.getSortOrder())
            .build();
    }

    private static List<RoomTypeEntryResult> mapToRoomTypes(final List<RoomType> roomTypes) {
        return roomTypes.stream()
            .map(RoomTypeEntryResult::from)
            .toList();
    }
}
