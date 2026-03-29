package com.jemini.stayhost.booking.application.dto;

import com.jemini.stayhost.booking.domain.model.Reservation;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CancelReservationResult(
    Long id,
    String reservationNumber,
    String status,
    LocalDateTime cancelledAt,
    String cancelReason
) {

    public static CancelReservationResult from(final Reservation reservation) {
        return CancelReservationResult.builder()
            .id(reservation.getId())
            .reservationNumber(reservation.getReservationNumber())
            .status(reservation.getStatus().name())
            .cancelledAt(reservation.getCancelledAt())
            .cancelReason(reservation.getCancelReason())
            .build();
    }
}
