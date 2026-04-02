package com.jemini.stayhost.booking.application.dto;

import com.jemini.stayhost.booking.domain.model.Reservation;
import com.jemini.stayhost.booking.domain.model.ReservationDailyRate;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Builder
public record ReservationResult(
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
    String source,
    String thumbnailUrl,
    List<DailyRateEntryResult> dailyRates,
    LocalDateTime confirmedAt,
    LocalDateTime cancelledAt,
    String cancelReason,
    LocalDateTime createdAt
) {

    @Builder
    public record DailyRateEntryResult(
        LocalDate date,
        BigDecimal price
    ) {

    }

    public static ReservationResult from(
        final Reservation reservation,
        final String propertyName,
        final String propertyAddress,
        final String roomTypeName,
        final String thumbnailUrl,
        final LocalTime checkInTime,
        final LocalTime checkOutTime
    ) {
        return ReservationResult.builder()
            .id(reservation.getId())
            .reservationNumber(reservation.getReservationNumber())
            .propertyId(reservation.getPropertyId())
            .propertyName(propertyName)
            .propertyAddress(propertyAddress)
            .roomTypeId(reservation.getRoomTypeId())
            .roomTypeName(roomTypeName)
            .checkInDate(reservation.getCheckInDate())
            .checkOutDate(reservation.getCheckOutDate())
            .checkInTime(checkInTime)
            .checkOutTime(checkOutTime)
            .guestName(reservation.getGuestName())
            .guestPhone(reservation.getGuestPhone())
            .guestCount(reservation.getGuestCount())
            .basePrice(reservation.getBasePrice())
            .discountAmount(reservation.getDiscountAmount())
            .finalPrice(reservation.getFinalPrice())
            .status(reservation.getStatus().name())
            .source(reservation.getSource().name())
            .thumbnailUrl(thumbnailUrl)
            .dailyRates(mapToDailyRates(reservation.getDailyRates()))
            .confirmedAt(reservation.getConfirmedAt())
            .cancelledAt(reservation.getCancelledAt())
            .cancelReason(reservation.getCancelReason())
            .createdAt(reservation.getCreatedAt())
            .build();
    }

    private static List<DailyRateEntryResult> mapToDailyRates(List<ReservationDailyRate> reservationDailyRates) {
        return reservationDailyRates.stream()
            .map(ReservationResult::mapToDailyRate)
            .toList();
    }

    private static DailyRateEntryResult mapToDailyRate(ReservationDailyRate dr) {
        return DailyRateEntryResult.builder()
            .date(dr.getDate())
            .price(dr.getPrice())
            .build();
    }
}
