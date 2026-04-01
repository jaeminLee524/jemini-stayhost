package com.jemini.stayhost.search.application.dto;

import com.jemini.stayhost.property.domain.model.Property;
import com.jemini.stayhost.property.domain.model.RoomType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Builder
public record PropertySearchResult(
    Long id,
    String name,
    String type,
    String region,
    String address,
    String thumbnailUrl,
    LocalTime checkInTime,
    LocalTime checkOutTime,
    BigDecimal minPrice,
    int availableRoomTypes
) {

    public static PropertySearchResult from(
        final Property property,
        final List<RoomType> roomTypes
    ) {
        final BigDecimal minPrice = roomTypes.stream()
            .map(RoomType::getBasePrice)
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);

        return PropertySearchResult.builder()
            .id(property.getId())
            .name(property.getName())
            .type(property.getType().name())
            .region(property.getRegion())
            .address(property.getAddress())
            .thumbnailUrl(property.getThumbnailUrl())
            .checkInTime(property.getCheckInTime())
            .checkOutTime(property.getCheckOutTime())
            .minPrice(minPrice)
            .availableRoomTypes(roomTypes.size())
            .build();
    }
}
