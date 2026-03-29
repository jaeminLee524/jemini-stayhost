package com.jemini.stayhost.booking.presentation.dto;

import com.jemini.stayhost.booking.application.dto.ReservationResult;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record CreateReservationResponse(
    Long id,
    String reservationNumber,
    String propertyName,
    String roomTypeName,
    LocalDate checkInDate,
    LocalDate checkOutDate,
    int guestCount,
    BigDecimal basePrice,
    BigDecimal discountAmount,
    BigDecimal finalPrice,
    String status,
    LocalDateTime confirmedAt,
    List<DailyRateEntry> dailyRates
) {

    @Builder
    public record DailyRateEntry(
        LocalDate date,
        BigDecimal price
    ) {}

    public static CreateReservationResponse from(final ReservationResult result) {
        return CreateReservationResponse.builder()
            .id(result.id())
            .reservationNumber(result.reservationNumber())
            .propertyName(result.propertyName())
            .roomTypeName(result.roomTypeName())
            .checkInDate(result.checkInDate())
            .checkOutDate(result.checkOutDate())
            .guestCount(result.guestCount())
            .basePrice(result.basePrice())
            .discountAmount(result.discountAmount())
            .finalPrice(result.finalPrice())
            .status(result.status())
            .confirmedAt(result.confirmedAt())
            .dailyRates(result.dailyRates().stream()
                .map(dr -> DailyRateEntry.builder()
                    .date(dr.date())
                    .price(dr.price())
                    .build())
                .toList())
            .build();
    }
}
