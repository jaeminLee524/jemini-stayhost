package com.jemini.stayhost.partner.presentation.dto;

import com.jemini.stayhost.common.logging.MaskField;
import com.jemini.stayhost.partner.application.dto.PartnerRegisterCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "파트너 등록 요청")
public record PartnerRegisterRequest(
    @Schema(description = "사업자명", example = "㈜스테이호스트")
    @NotBlank @Size(max = 200) String businessName,

    @Schema(description = "사업자등록번호", example = "123-45-67890")
    @NotBlank @Size(max = 20) String businessNumber,

    @Schema(description = "대표자명", example = "홍길동")
    @NotBlank @Size(max = 100) @MaskField String representative,

    @Schema(description = "연락처", example = "02-1234-5678")
    @Size(max = 20) @MaskField String phone,

    @Schema(description = "이메일", example = "contact@stayhost.com")
    @Size(max = 200) @MaskField String email,

    @Schema(description = "정산 은행명", example = "국민은행")
    @Size(max = 50) String bankName,

    @Schema(description = "정산 계좌번호", example = "123456-78-901234")
    @Size(max = 50) @MaskField String bankAccount,

    @Schema(description = "Extranet 로그인 아이디", example = "partner_admin")
    @NotBlank @Size(max = 100) String loginId,

    @Schema(description = "비밀번호 (8자 이상)", example = "password1234!")
    @NotBlank @Size(min = 8, max = 50) @MaskField String password
) {

    public PartnerRegisterCommand toCommand() {
        return PartnerRegisterCommand.builder()
            .businessName(this.businessName)
            .businessNumber(this.businessNumber)
            .representative(this.representative)
            .phone(this.phone)
            .email(this.email)
            .bankName(this.bankName)
            .bankAccount(this.bankAccount)
            .loginId(this.loginId)
            .password(this.password)
            .build();
    }
}
