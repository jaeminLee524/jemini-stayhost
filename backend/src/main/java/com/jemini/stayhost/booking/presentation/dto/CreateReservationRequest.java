package com.jemini.stayhost.booking.presentation.dto;

import com.jemini.stayhost.booking.application.dto.CreateReservationCommand;
import com.jemini.stayhost.common.logging.MaskField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "예약 생성 요청")
public record CreateReservationRequest(
    @Schema(description = "숙소 ID", example = "100")
    @NotNull Long propertyId,

    @Schema(description = "객실 유형 ID", example = "200")
    @NotNull Long roomTypeId,

    @Schema(description = "체크인 날짜", example = "2026-04-10")
    @NotNull LocalDate checkInDate,

    @Schema(description = "체크아웃 날짜", example = "2026-04-12")
    @NotNull LocalDate checkOutDate,

    @Schema(description = "투숙객 이름", example = "김민준")
    @NotBlank @Size(max = 100) @MaskField String guestName,

    @Schema(description = "투숙객 연락처", example = "010-1234-5678")
    @Size(max = 20) @MaskField String guestPhone,

    @Schema(description = "투숙 인원", example = "2")
    @NotNull @Min(1) Integer guestCount
) {

    public CreateReservationCommand toCommand() {
        return CreateReservationCommand.builder()
            .propertyId(this.propertyId)
            .roomTypeId(this.roomTypeId)
            .checkInDate(this.checkInDate)
            .checkOutDate(this.checkOutDate)
            .guestName(this.guestName)
            .guestPhone(this.guestPhone)
            .guestCount(this.guestCount)
            .build();
    }
}
