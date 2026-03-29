package com.jemini.stayhost.booking.presentation.dto;

import com.jemini.stayhost.booking.application.dto.CancelReservationResult;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CancelReservationResponse(
    Long id,
    String reservationNumber,
    String status,
    LocalDateTime cancelledAt,
    String cancelReason
) {

    public static CancelReservationResponse from(final CancelReservationResult result) {
        return CancelReservationResponse.builder()
            .id(result.id())
            .reservationNumber(result.reservationNumber())
            .status(result.status())
            .cancelledAt(result.cancelledAt())
            .cancelReason(result.cancelReason())
            .build();
    }
}
