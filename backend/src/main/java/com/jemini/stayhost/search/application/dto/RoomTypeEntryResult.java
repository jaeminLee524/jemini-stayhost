package com.jemini.stayhost.search.application.dto;

import com.jemini.stayhost.property.domain.model.RoomType;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record RoomTypeEntryResult(
    Long id,
    String name,
    String description,
    int maxOccupancy,
    BigDecimal basePrice,
    String amenities
) {

    public static RoomTypeEntryResult from(final RoomType roomType) {
        return RoomTypeEntryResult.builder()
            .id(roomType.getId())
            .name(roomType.getName())
            .description(roomType.getDescription())
            .maxOccupancy(roomType.getMaxOccupancy())
            .basePrice(roomType.getBasePrice())
            .amenities(roomType.getAmenities())
            .build();
    }
}
