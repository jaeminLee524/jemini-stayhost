package com.jemini.stayhost.partner.presentation.dto;

import com.jemini.stayhost.partner.application.dto.PartnerResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "파트너 등록 응답")
public record PartnerRegisterResponse(
    @Schema(description = "파트너 ID", example = "10")
    Long partnerId,

    @Schema(description = "사업자명", example = "㈜스테이호스트")
    String businessName,

    @Schema(description = "상태", example = "PENDING")
    String status
) {

    public static PartnerRegisterResponse from(final PartnerResult result) {
        return PartnerRegisterResponse.builder()
            .partnerId(result.id())
            .businessName(result.businessName())
            .status(result.status())
            .build();
    }
}
