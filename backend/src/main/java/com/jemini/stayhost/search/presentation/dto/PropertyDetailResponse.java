package com.jemini.stayhost.search.presentation.dto;

import com.jemini.stayhost.search.application.dto.PropertyDetailResult;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Builder
public record PropertyDetailResponse(
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
    List<ImageEntry> images,
    List<RoomTypeEntry> roomTypes
) {

    @Builder
    public record ImageEntry(
        String imageUrl,
        int sortOrder
    ) {}

    @Builder
    public record RoomTypeEntry(
        Long id,
        String name,
        String description,
        int maxOccupancy,
        BigDecimal basePrice,
        String amenities
    ) {}

    public static PropertyDetailResponse from(final PropertyDetailResult result) {
        return PropertyDetailResponse.builder()
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
            .images(result.images().stream()
                .map(img -> ImageEntry.builder()
                    .imageUrl(img.imageUrl())
                    .sortOrder(img.sortOrder())
                    .build())
                .toList())
            .roomTypes(result.roomTypes().stream()
                .map(rt -> RoomTypeEntry.builder()
                    .id(rt.id())
                    .name(rt.name())
                    .description(rt.description())
                    .maxOccupancy(rt.maxOccupancy())
                    .basePrice(rt.basePrice())
                    .amenities(rt.amenities())
                    .build())
                .toList())
            .build();
    }
}
