package com.jemini.stayhost.booking.presentation.dto;

import com.jemini.stayhost.booking.application.dto.ReservationResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Schema(description = "예약 목록 항목 응답")
public record ReservationListResponse(
    @Schema(description = "예약 ID", example = "1")
    Long id,

    @Schema(description = "예약 번호", example = "RES-20240101-000001")
    String reservationNumber,

    @Schema(description = "숙소명", example = "스테이호스트 서울 강남점")
    String propertyName,

    @Schema(description = "객실 유형명", example = "스탠다드 더블")
    String roomTypeName,

    @Schema(description = "체크인 날짜", example = "2024-07-01")
    LocalDate checkInDate,

    @Schema(description = "체크아웃 날짜", example = "2024-07-03")
    LocalDate checkOutDate,

    @Schema(description = "기본 가격", example = "200000")
    BigDecimal basePrice,

    @Schema(description = "할인 금액", example = "10000")
    BigDecimal discountAmount,

    @Schema(description = "최종 결제 금액", example = "190000")
    BigDecimal finalPrice,

    @Schema(description = "예약 상태", example = "CONFIRMED")
    String status,

    @Schema(description = "숙소 썸네일 URL")
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
