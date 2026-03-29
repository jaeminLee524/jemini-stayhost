package com.jemini.stayhost.partner.presentation.dto;

import com.jemini.stayhost.partner.application.dto.PartnerLoginCommand;
import jakarta.validation.constraints.NotBlank;

public record PartnerLoginRequest(
        @NotBlank String loginId,
        @NotBlank String password
) {

    public PartnerLoginCommand toCommand() {
        return PartnerLoginCommand.create(loginId, password);
    }
}
