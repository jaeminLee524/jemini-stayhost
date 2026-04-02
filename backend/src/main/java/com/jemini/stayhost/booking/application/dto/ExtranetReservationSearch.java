package com.jemini.stayhost.booking.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "파트너 예약 목록 검색 조건")
public record ExtranetReservationSearch(
    @Schema(description = "숙소 ID 필터", example = "100")
    Long propertyId,

    @Schema(description = "예약 상태 필터", example = "CONFIRMED")
    String status,

    @Schema(description = "체크인 시작일 필터", example = "2024-07-01")
    LocalDate checkInFrom,

    @Schema(description = "체크인 종료일 필터", example = "2024-07-31")
    LocalDate checkInTo
) {

}
