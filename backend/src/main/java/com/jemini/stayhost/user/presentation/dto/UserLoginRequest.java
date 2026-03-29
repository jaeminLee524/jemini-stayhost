package com.jemini.stayhost.user.presentation.dto;

import com.jemini.stayhost.user.application.dto.UserLoginCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "회원 로그인 요청")
public record UserLoginRequest(
    @Schema(description = "이메일", example = "user@example.com")
    @NotBlank String email,

    @Schema(description = "비밀번호", example = "password1234!")
    @NotBlank String password
) {

    public UserLoginCommand toCommand() {
        return UserLoginCommand.builder()
            .email(this.email)
            .password(this.password)
            .build();
    }
}
