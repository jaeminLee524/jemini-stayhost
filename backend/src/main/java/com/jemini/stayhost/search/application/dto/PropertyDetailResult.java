package com.jemini.stayhost.search.application.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
}
