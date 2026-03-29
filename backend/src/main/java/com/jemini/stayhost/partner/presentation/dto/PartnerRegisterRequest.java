package com.jemini.stayhost.partner.presentation.dto;

import com.jemini.stayhost.partner.application.dto.PartnerRegisterCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PartnerRegisterRequest(
        @NotBlank @Size(max = 200) String businessName,
        @NotBlank @Size(max = 20) String businessNumber,
        @NotBlank @Size(max = 100) String representative,
        @Size(max = 20) String phone,
        @Size(max = 200) String email,
        @Size(max = 50) String bankName,
        @Size(max = 50) String bankAccount,
        @NotBlank @Size(max = 100) String loginId,
        @NotBlank @Size(min = 8, max = 50) String password
) {

    public PartnerRegisterCommand toCommand() {
        return PartnerRegisterCommand.create(
                businessName, businessNumber, representative,
                phone, email, bankName, bankAccount, loginId, password
        );
    }
}
