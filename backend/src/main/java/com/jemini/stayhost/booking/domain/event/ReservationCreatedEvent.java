package com.jemini.stayhost.booking.domain.event;

import lombok.Builder;

@Builder
public record ReservationCreatedEvent(
    Long reservationId
) {

    public static ReservationCreatedEvent create(final Long reservationId) {
        return ReservationCreatedEvent.builder()
            .reservationId(reservationId)
            .build();
    }
}
