package com.jemini.stayhost.booking.presentation.dto;

import com.jemini.stayhost.booking.application.dto.ReservationResult;
import com.jemini.stayhost.common.logging.MaskField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Schema(description = "파트너 예약 상세 응답")
public record ExtranetReservationDetailResponse(
    @Schema(description = "예약 ID", example = "1")
    Long id,

    @Schema(description = "예약 번호", example = "RES-20240101-000001")
    String reservationNumber,

    @Schema(description = "숙소 ID", example = "100")
    Long propertyId,

    @Schema(description = "숙소명", example = "스테이호스트 서울 강남점")
    String propertyName,

    @Schema(description = "객실 유형 ID", example = "10")
    Long roomTypeId,

    @Schema(description = "객실 유형명", example = "스탠다드 더블")
    String roomTypeName,

    @Schema(description = "투숙객 이름", example = "김민준")
    @MaskField String guestName,

    @Schema(description = "투숙객 연락처", example = "010-1234-5678")
    @MaskField String guestPhone,

    @Schema(description = "체크인 날짜", example = "2024-07-01")
    LocalDate checkInDate,

    @Schema(description = "체크아웃 날짜", example = "2024-07-03")
    LocalDate checkOutDate,

    @Schema(description = "투숙 인원", example = "2")
    int guestCount,

    @Schema(description = "기본 가격", example = "200000")
    BigDecimal basePrice,

    @Schema(description = "할인 금액", example = "10000")
    BigDecimal discountAmount,

    @Schema(description = "최종 결제 금액", example = "190000")
    BigDecimal finalPrice,

    @Schema(description = "예약 상태", example = "CONFIRMED")
    String status,

    @Schema(description = "예약 출처", example = "DIRECT")
    String source,

    @Schema(description = "날짜별 요금 목록")
    List<DailyRateEntry> dailyRateResults,

    @Schema(description = "예약 확정 일시")
    LocalDateTime confirmedAt,

    @Schema(description = "예약 생성 일시")
    LocalDateTime createdAt
) {

    @Builder
    @Schema(description = "날짜별 요금 항목")
    public record DailyRateEntry(
        @Schema(description = "날짜", example = "2024-07-01")
        LocalDate date,

        @Schema(description = "해당 날짜 요금", example = "100000")
        BigDecimal price
    ) {}

    public static ExtranetReservationDetailResponse from(final ReservationResult result) {
        return ExtranetReservationDetailResponse.builder()
            .id(result.id())
            .reservationNumber(result.reservationNumber())
            .propertyId(result.propertyId())
            .propertyName(result.propertyName())
            .roomTypeId(result.roomTypeId())
            .roomTypeName(result.roomTypeName())
            .guestName(result.guestName())
            .guestPhone(result.guestPhone())
            .checkInDate(result.checkInDate())
            .checkOutDate(result.checkOutDate())
            .guestCount(result.guestCount())
            .basePrice(result.basePrice())
            .discountAmount(result.discountAmount())
            .finalPrice(result.finalPrice())
            .status(result.status())
            .source(result.source())
            .dailyRateResults(mapToDailyRates(result.dailyRates()))
            .confirmedAt(result.confirmedAt())
            .createdAt(result.createdAt())
            .build();
    }

    private static List<DailyRateEntry> mapToDailyRates(final List<ReservationResult.DailyRateEntryResult> dailyRates) {
        return dailyRates.stream()
            .map(ExtranetReservationDetailResponse::mapToDailyRate)
            .toList();
    }

    private static DailyRateEntry mapToDailyRate(final ReservationResult.DailyRateEntryResult dr) {
        return DailyRateEntry.builder()
            .date(dr.date())
            .price(dr.price())
            .build();
    }
}
