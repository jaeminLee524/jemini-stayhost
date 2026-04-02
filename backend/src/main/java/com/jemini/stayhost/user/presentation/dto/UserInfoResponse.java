package com.jemini.stayhost.user.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "회원 기본 정보")
public record UserInfoResponse(
    @Schema(description = "회원 ID", example = "1")
    Long id,

    @Schema(description = "이메일", example = "user@example.com")
    String email,

    @Schema(description = "이름", example = "홍길동")
    String name
) {

}
