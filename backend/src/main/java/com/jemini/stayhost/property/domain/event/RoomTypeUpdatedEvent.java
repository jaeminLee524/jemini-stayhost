package com.jemini.stayhost.property.domain.event;

import lombok.Builder;

@Builder
public record RoomTypeUpdatedEvent(
    Long propertyId
) {

    public static RoomTypeUpdatedEvent create(final Long propertyId) {
        return RoomTypeUpdatedEvent.builder()
            .propertyId(propertyId)
            .build();
    }
}
