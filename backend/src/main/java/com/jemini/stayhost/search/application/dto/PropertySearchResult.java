package com.jemini.stayhost.search.application.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalTime;

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
}
