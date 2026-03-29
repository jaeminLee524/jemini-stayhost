package com.jemini.stayhost.property.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "숙소 상태 변경 요청")
public record PropertyStatusRequest(
        @Schema(description = "변경할 상태", example = "ACTIVE")
        @NotBlank String status
) {
}
