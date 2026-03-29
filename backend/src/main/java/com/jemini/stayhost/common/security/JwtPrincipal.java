package com.jemini.stayhost.common.security;

public record JwtPrincipal(
  Long subjectId,
  String role,
  String context
) {
}
