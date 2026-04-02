package com.jemini.stayhost.user.presentation.dto;

import com.jemini.stayhost.user.application.dto.UserResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "회원가입 응답")
public record UserSignupResponse(
    @Schema(description = "회원 ID", example = "1")
    Long id,

    @Schema(description = "이메일", example = "user@example.com")
    String email,

    @Schema(description = "이름", example = "홍길동")
    String name
) {

    public static UserSignupResponse from(final UserResult result) {
        return UserSignupResponse.builder()
            .id(result.id())
            .email(result.email())
            .name(result.name())
            .build();
    }
}
