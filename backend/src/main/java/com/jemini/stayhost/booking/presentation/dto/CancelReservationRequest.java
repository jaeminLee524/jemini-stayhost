package com.jemini.stayhost.booking.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "예약 취소 요청")
public record CancelReservationRequest(
    @Schema(description = "취소 사유", example = "일정 변경으로 인한 취소")
    @Size(max = 500) String cancelReason
) {
}
