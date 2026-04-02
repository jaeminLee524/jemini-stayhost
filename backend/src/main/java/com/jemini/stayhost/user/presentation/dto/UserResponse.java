package com.jemini.stayhost.user.presentation.dto;

import com.jemini.stayhost.user.application.dto.UserResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "회원 응답")
public record UserResponse(
    @Schema(description = "회원 ID", example = "1")
    Long id,

    @Schema(description = "이메일", example = "user@example.com")
    String email,

    @Schema(description = "이름", example = "홍길동")
    String name,

    @Schema(description = "연락처", example = "010-1234-5678")
    String phone,

    @Schema(description = "회원 상태", example = "ACTIVE")
    String status,

    @Schema(description = "가입 일시")
    LocalDateTime createdAt
) {

    public static UserResponse from(final UserResult result) {
        return UserResponse.builder()
            .id(result.id())
            .email(result.email())
            .name(result.name())
            .phone(result.phone())
            .status(result.status())
            .createdAt(result.createdAt())
            .build();
    }
}
