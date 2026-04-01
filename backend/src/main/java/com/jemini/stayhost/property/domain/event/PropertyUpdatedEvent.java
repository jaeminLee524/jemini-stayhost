package com.jemini.stayhost.property.domain.event;

import lombok.Builder;

@Builder
public record PropertyUpdatedEvent(
    Long propertyId
) {

    public static PropertyUpdatedEvent create(
        final Long propertyId
    ) {
        return PropertyUpdatedEvent.builder()
            .propertyId(propertyId)
            .build();
    }
}
