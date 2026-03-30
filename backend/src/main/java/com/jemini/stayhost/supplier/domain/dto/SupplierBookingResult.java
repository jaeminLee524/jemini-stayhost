package com.jemini.stayhost.supplier.domain.dto;

public record SupplierBookingResult(
        boolean success,
        String externalBookingId,
        String errorMessage
) {
}
