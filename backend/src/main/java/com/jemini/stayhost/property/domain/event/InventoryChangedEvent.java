package com.jemini.stayhost.property.domain.event;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record InventoryChangedEvent(
    Long roomTypeId,
    List<LocalDate> affectedDates
) {

    public static InventoryChangedEvent create(
        final Long roomTypeId,
        final List<LocalDate> affectedDates
    ) {
        return InventoryChangedEvent.builder()
            .roomTypeId(roomTypeId)
            .affectedDates(affectedDates)
            .build();
    }
}
