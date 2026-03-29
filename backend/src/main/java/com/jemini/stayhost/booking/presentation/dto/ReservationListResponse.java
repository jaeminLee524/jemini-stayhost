package com.jemini.stayhost.booking.presentation.dto;

import com.jemini.stayhost.booking.application.dto.ReservationResult;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record ReservationListResponse(
    Long id,
    String reservationNumber,
    String propertyName,
    String roomTypeName,
    LocalDate checkInDate,
    LocalDate checkOutDate,
    BigDecimal basePrice,
    BigDecimal discountAmount,
    BigDecimal finalPrice,
    String status,
    String thumbnailUrl
) {

    public static ReservationListResponse from(final ReservationResult result) {
        return ReservationListResponse.builder()
            .id(result.id())
            .reservationNumber(result.reservationNumber())
            .propertyName(result.propertyName())
            .roomTypeName(result.roomTypeName())
            .checkInDate(result.checkInDate())
            .checkOutDate(result.checkOutDate())
            .basePrice(result.basePrice())
            .discountAmount(result.discountAmount())
            .finalPrice(result.finalPrice())
            .status(result.status())
            .thumbnailUrl(result.thumbnailUrl())
            .build();
    }
}
