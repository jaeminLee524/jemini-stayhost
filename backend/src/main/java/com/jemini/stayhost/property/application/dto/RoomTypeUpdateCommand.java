package com.jemini.stayhost.property.application.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;

@Builder
public record RoomTypeUpdateCommand(
    String name,
    String description,
    int maxOccupancy,
    BigDecimal basePrice,
    List<String> amenities
) {
}
