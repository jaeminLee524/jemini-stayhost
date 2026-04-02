package com.jemini.stayhost.partner.presentation.dto;

import com.jemini.stayhost.common.logging.MaskField;
import com.jemini.stayhost.partner.application.dto.PartnerUpdateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "파트너 정보 수정 요청")
public record PartnerUpdateRequest(
    @Schema(description = "연락처", example = "02-9999-8888")
    @Size(max = 20) @MaskField String phone,

    @Schema(description = "이메일", example = "new@stayhost.com")
    @Size(max = 200) @MaskField String email,

    @Schema(description = "정산 은행명", example = "신한은행")
    @Size(max = 50) String bankName,

    @Schema(description = "정산 계좌번호", example = "987654-32-109876")
    @Size(max = 50) @MaskField String bankAccount
) {

    public PartnerUpdateCommand toCommand() {
        return PartnerUpdateCommand.builder()
            .phone(this.phone)
            .email(this.email)
            .bankName(this.bankName)
            .bankAccount(this.bankAccount)
            .build();
    }
}
