package com.jemini.stayhost.partner.presentation.dto;

import com.jemini.stayhost.partner.application.dto.PartnerLoginCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "파트너 로그인 요청")
public record PartnerLoginRequest(
    @Schema(description = "로그인 아이디", example = "partner_admin")
    @NotBlank String loginId,

    @Schema(description = "비밀번호", example = "password1234!")
    @NotBlank String password
) {

  public PartnerLoginCommand toCommand() {
    return PartnerLoginCommand.builder()
        .loginId(this.loginId)
        .password(this.password)
        .build();
  }
}
