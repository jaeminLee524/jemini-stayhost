package com.jemini.stayhost.booking.presentation.dto;

import com.jemini.stayhost.booking.application.dto.ReservationResult;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Builder
public record ReservationDetailResponse(
    Long id,
    String reservationNumber,
    Long propertyId,
    String propertyName,
    String propertyAddress,
    Long roomTypeId,
    String roomTypeName,
    LocalDate checkInDate,
    LocalDate checkOutDate,
    LocalTime checkInTime,
    LocalTime checkOutTime,
    String guestName,
    String guestPhone,
    int guestCount,
    BigDecimal basePrice,
    BigDecimal discountAmount,
    BigDecimal finalPrice,
    String status,
    List<DailyRateEntry> dailyRates,
    LocalDateTime confirmedAt,
    LocalDateTime createdAt
) {

    @Builder
    public record DailyRateEntry(
        LocalDate date,
        BigDecimal price
    ) {}

    public static ReservationDetailResponse from(final ReservationResult result) {
        return ReservationDetailResponse.builder()
            .id(result.id())
            .reservationNumber(result.reservationNumber())
            .propertyId(result.propertyId())
            .propertyName(result.propertyName())
            .propertyAddress(result.propertyAddress())
            .roomTypeId(result.roomTypeId())
            .roomTypeName(result.roomTypeName())
            .checkInDate(result.checkInDate())
            .checkOutDate(result.checkOutDate())
            .checkInTime(result.checkInTime())
            .checkOutTime(result.checkOutTime())
            .guestName(result.guestName())
            .guestPhone(result.guestPhone())
            .guestCount(result.guestCount())
            .basePrice(result.basePrice())
            .discountAmount(result.discountAmount())
            .finalPrice(result.finalPrice())
            .status(result.status())
            .dailyRates(result.dailyRates().stream()
                .map(dr -> DailyRateEntry.builder()
                    .date(dr.date())
                    .price(dr.price())
                    .build())
                .toList())
            .confirmedAt(result.confirmedAt())
            .createdAt(result.createdAt())
            .build();
    }
}
