package com.jemini.stayhost.property.application.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;

@Builder
public record RoomTypeCreateCommand(
    String name,
    String description,
    int maxOccupancy,
    BigDecimal basePrice,
    List<String> amenities,
    int totalRoomCount,
    List<String> imageUrls
) {

}
