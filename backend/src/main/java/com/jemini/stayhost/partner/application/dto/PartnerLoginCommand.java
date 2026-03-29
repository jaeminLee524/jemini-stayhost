package com.jemini.stayhost.partner.application.dto;

import lombok.Builder;

@Builder
public record PartnerLoginCommand(
    String loginId,
    String password
) {

}
