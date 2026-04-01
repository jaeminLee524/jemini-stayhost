package com.jemini.stayhost.partner.presentation.dto;

import com.jemini.stayhost.partner.application.dto.PartnerLoginResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "로그인한 파트너 정보")
public record PartnerInfoResponse(
    @Schema(description = "파트너 ID", example = "10")
    Long id,

    @Schema(description = "사업자명", example = "㈜스테이호스트")
    String businessName,

    @Schema(description = "상태", example = "ACTIVE")
    String status
) {

    public static PartnerInfoResponse from(final PartnerLoginResult result) {
        return PartnerInfoResponse.builder()
            .id(result.partnerId())
            .businessName(result.businessName())
            .status(result.status())
            .build();
    }
}
