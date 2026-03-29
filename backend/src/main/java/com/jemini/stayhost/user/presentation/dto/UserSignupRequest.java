package com.jemini.stayhost.user.presentation.dto;

import com.jemini.stayhost.user.application.dto.UserSignupCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 요청")
public record UserSignupRequest(
    @Schema(description = "이메일", example = "user@example.com")
    @NotBlank @Email @Size(max = 200) String email,

    @Schema(description = "비밀번호 (8자 이상)", example = "password1234!")
    @NotBlank @Size(min = 8, max = 50) String password,

    @Schema(description = "이름", example = "이지은")
    @NotBlank @Size(max = 100) String name,

    @Schema(description = "연락처", example = "010-5678-1234")
    @Size(max = 20) String phone
) {

    public UserSignupCommand toCommand() {
        return UserSignupCommand.builder()
            .email(this.email)
            .password(this.password)
            .name(this.name)
            .phone(this.phone)
            .build();
    }
}
