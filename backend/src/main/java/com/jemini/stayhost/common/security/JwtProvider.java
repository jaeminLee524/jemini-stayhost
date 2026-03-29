package com.jemini.stayhost.common.security;

import com.jemini.stayhost.common.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {

  private final SecretKey secretKey;
  @Getter
  private final long expiration;

  public JwtProvider(final JwtProperties jwtProperties) {
    this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    this.expiration = jwtProperties.expiration();
  }

  public String generateToken(final Long subject, final String role, final String context) {
    final Date now = new Date();
    final Date expiryDate = new Date(now.getTime() + expiration);

    return Jwts.builder()
        .subject(String.valueOf(subject))
        .claim("role", role)
        .claim("context", context)
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(secretKey)
        .compact();
  }

  public JwtPrincipal parseToken(final String token) {
    final Claims claims = parseClaims(token);
    return new JwtPrincipal(
        Long.parseLong(claims.getSubject()),
        claims.get("role", String.class),
        claims.get("context", String.class)
    );
  }

  public boolean validateToken(final String token) {
    try {
      parseClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  private Claims parseClaims(final String token) {
    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
