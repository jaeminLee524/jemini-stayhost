package com.jemini.stayhost.search.application.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record RoomTypeRateResult(
    Long propertyId,
    List<RoomTypeRateEntry> roomTypes
) {

    @Builder
    public record RoomTypeRateEntry(
        Long id,
        String name,
        int maxOccupancy,
        List<DailyRate> rates
    ) {}

    @Builder
    public record DailyRate(
        LocalDate date,
        BigDecimal price,
        boolean available
    ) {}
}
