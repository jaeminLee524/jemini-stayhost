package com.jemini.stayhost.booking.application.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record CreateReservationCommand(
    Long propertyId,
    Long roomTypeId,
    LocalDate checkInDate,
    LocalDate checkOutDate,
    String guestName,
    String guestPhone,
    int guestCount
) {
}
