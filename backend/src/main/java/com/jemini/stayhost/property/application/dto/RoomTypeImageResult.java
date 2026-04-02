package com.jemini.stayhost.property.application.dto;

import lombok.Builder;

@Builder
public record RoomTypeImageResult(
    Long id,
    String imageUrl,
    Integer sortOrder
) {

}
