package com.jemini.stayhost.partner.presentation.dto;

import com.jemini.stayhost.partner.application.dto.PartnerUpdateCommand;
import jakarta.validation.constraints.Size;

public record PartnerUpdateRequest(
        @Size(max = 20) String phone,
        @Size(max = 200) String email,
        @Size(max = 50) String bankName,
        @Size(max = 50) String bankAccount
) {

    public PartnerUpdateCommand toCommand() {
        return PartnerUpdateCommand.create(phone, email, bankName, bankAccount);
    }
}
