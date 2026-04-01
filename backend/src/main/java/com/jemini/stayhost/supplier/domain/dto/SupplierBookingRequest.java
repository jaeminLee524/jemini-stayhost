package com.jemini.stayhost.supplier.domain.dto;

import java.time.LocalDate;

import lombok.Builder;

@Builder
public record SupplierBookingRequest(
    String externalPropertyId,
    String externalRoomId,
    LocalDate checkInDate,
    LocalDate checkOutDate,
    String guestName,
    int guestCount
) {

}
