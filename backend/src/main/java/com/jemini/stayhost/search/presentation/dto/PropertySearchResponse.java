package com.jemini.stayhost.search.presentation.dto;

import com.jemini.stayhost.search.application.dto.PropertySearchResult;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalTime;

@Builder
public record PropertySearchResponse(
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

    public static PropertySearchResponse from(final PropertySearchResult result) {
        return PropertySearchResponse.builder()
            .id(result.id())
            .name(result.name())
            .type(result.type())
            .region(result.region())
            .address(result.address())
            .thumbnailUrl(result.thumbnailUrl())
            .checkInTime(result.checkInTime())
            .checkOutTime(result.checkOutTime())
            .minPrice(result.minPrice())
            .availableRoomTypes(result.availableRoomTypes())
            .build();
    }
}
