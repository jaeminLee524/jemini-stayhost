package com.jemini.stayhost.partner.presentation.dto;

import com.jemini.stayhost.partner.application.dto.PartnerResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "파트너 상세 응답")
public record PartnerResponse(
    @Schema(description = "파트너 ID", example = "10")
    Long id,

    @Schema(description = "사업자명", example = "㈜스테이호스트")
    String businessName,

    @Schema(description = "사업자등록번호", example = "123-45-67890")
    String businessNumber,

    @Schema(description = "대표자명", example = "홍길동")
    String representative,

    @Schema(description = "연락처", example = "02-1234-5678")
    String phone,

    @Schema(description = "이메일", example = "contact@stayhost.com")
    String email,

    @Schema(description = "정산 은행명", example = "국민은행")
    String bankName,

    @Schema(description = "정산 계좌번호", example = "123456-78-901234")
    String bankAccount,

    @Schema(description = "상태", example = "ACTIVE")
    String status,

    @Schema(description = "등록일시")
    LocalDateTime createdAt
) {

  public static PartnerResponse from(final PartnerResult result) {
    return PartnerResponse.builder()
        .id(result.id())
        .businessName(result.businessName())
        .businessNumber(result.businessNumber())
        .representative(result.representative())
        .phone(result.phone())
        .email(result.email())
        .bankName(result.bankName())
        .bankAccount(result.bankAccount())
        .status(result.status())
        .createdAt(result.createdAt())
        .build();
  }
}
