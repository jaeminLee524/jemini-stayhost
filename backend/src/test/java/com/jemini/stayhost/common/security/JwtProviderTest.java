package com.jemini.stayhost.common.security;

import com.jemini.stayhost.common.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        final JwtProperties properties = new JwtProperties(
            "test-secret-key-for-jwt-provider-must-be-at-least-32-bytes-long",
            1800000L
        );
        jwtProvider = new JwtProvider(properties);
    }

    @Test
    @DisplayName("토큰 생성 성공")
    void 토큰_생성_성공() {
        final String token = jwtProvider.generateToken(1L, "PARTNER", "EXTRANET");

        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("토큰 파싱 성공 - subject, role, context")
    void 토큰_파싱_성공_subject_role_context() {
        final String token = jwtProvider.generateToken(42L, "PARTNER", "EXTRANET");

        final JwtPrincipal principal = jwtProvider.parseToken(token);

        assertThat(principal.subjectId()).isEqualTo(42L);
        assertThat(principal.role()).isEqualTo("PARTNER");
        assertThat(principal.context()).isEqualTo("EXTRANET");
    }

    @Test
    @DisplayName("유효한 토큰 검증 - true")
    void 유효한_토큰_검증_true() {
        final String token = jwtProvider.generateToken(1L, "PARTNER", "EXTRANET");

        assertThat(jwtProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("만료된 토큰 검증 - false")
    void 만료된_토큰_검증_false() {
        final JwtProperties expiredProps = new JwtProperties(
            "test-secret-key-for-jwt-provider-must-be-at-least-32-bytes-long",
            -1000L
        );
        final JwtProvider expiredProvider = new JwtProvider(expiredProps);
        final String token = expiredProvider.generateToken(1L, "PARTNER", "EXTRANET");

        assertThat(expiredProvider.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("변조된 토큰 검증 - false")
    void 변조된_토큰_검증_false() {
        final String token = jwtProvider.generateToken(1L, "PARTNER", "EXTRANET");

        assertThat(jwtProvider.validateToken(token + "tampered")).isFalse();
    }

    @Test
    @DisplayName("빈문자열 토큰 검증 - false")
    void 빈문자열_토큰_검증_false() {
        assertThat(jwtProvider.validateToken("")).isFalse();
    }
}
