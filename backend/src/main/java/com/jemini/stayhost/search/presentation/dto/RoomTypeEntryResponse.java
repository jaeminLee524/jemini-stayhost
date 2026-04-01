package com.jemini.stayhost.search.presentation.dto;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record RoomTypeEntryResponse(
    Long id,
    String name,
    String description,
    int maxOccupancy,
    BigDecimal basePrice,
    String amenities
) {

}
