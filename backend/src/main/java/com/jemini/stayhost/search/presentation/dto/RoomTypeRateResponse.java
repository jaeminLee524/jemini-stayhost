package com.jemini.stayhost.search.presentation.dto;

import com.jemini.stayhost.search.application.dto.RoomTypeRateResult;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record RoomTypeRateResponse(
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

    public static RoomTypeRateResponse from(final RoomTypeRateResult result) {
        return RoomTypeRateResponse.builder()
            .propertyId(result.propertyId())
            .roomTypes(result.roomTypes().stream()
                .map(rt -> RoomTypeRateEntry.builder()
                    .id(rt.id())
                    .name(rt.name())
                    .maxOccupancy(rt.maxOccupancy())
                    .rates(rt.rates().stream()
                        .map(r -> DailyRate.builder()
                            .date(r.date())
                            .price(r.price())
                            .available(r.available())
                            .build())
                        .toList())
                    .build())
                .toList())
            .build();
    }
}
