package com.jemini.stayhost.property.domain.event;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record RateUpdatedEvent(
    Long roomTypeId,
    List<LocalDate> affectedDates
) {

    public static RateUpdatedEvent create(
        final Long roomTypeId,
        final List<LocalDate> affectedDates
    ) {
        return RateUpdatedEvent.builder()
            .roomTypeId(roomTypeId)
            .affectedDates(affectedDates)
            .build();
    }
}
