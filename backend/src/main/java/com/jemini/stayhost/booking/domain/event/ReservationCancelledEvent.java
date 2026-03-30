package com.jemini.stayhost.booking.domain.event;

import lombok.Builder;

@Builder
public record ReservationCancelledEvent(
    Long reservationId
) {

    public static ReservationCancelledEvent create(final Long reservationId) {
        return ReservationCancelledEvent.builder()
            .reservationId(reservationId)
            .build();
    }
}
