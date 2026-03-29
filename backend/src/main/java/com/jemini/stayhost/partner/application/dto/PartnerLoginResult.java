package com.jemini.stayhost.partner.application.dto;

import lombok.Builder;

@Builder
public record PartnerLoginResult(
    String accessToken,
    long expiresInSeconds,
    Long partnerId,
    String businessName,
    String status
) {

  public static PartnerLoginResult create(
      final String accessToken,
      final long expiresInSeconds,
      final Long partnerId,
      final String businessName,
      final String status
  ) {
    return PartnerLoginResult.builder()
        .accessToken(accessToken)
        .expiresInSeconds(expiresInSeconds)
        .partnerId(partnerId)
        .businessName(businessName)
        .status(status)
        .build();
  }
}
