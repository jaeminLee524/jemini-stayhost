package com.jemini.stayhost.booking.presentation.dto;

import com.jemini.stayhost.booking.application.dto.CancelReservationResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "예약 취소 응답")
public record CancelReservationResponse(
    @Schema(description = "예약 ID", example = "1")
    Long id,

    @Schema(description = "예약 번호", example = "RES-20240101-000001")
    String reservationNumber,

    @Schema(description = "예약 상태", example = "CANCELLED")
    String status,

    @Schema(description = "취소 일시")
    LocalDateTime cancelledAt,

    @Schema(description = "취소 사유", example = "일정 변경으로 인한 취소")
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
