package com.jemini.stayhost.support;

import com.jemini.stayhost.common.config.SecurityConfig;
import com.jemini.stayhost.common.config.WebMvcConfig;
import com.jemini.stayhost.common.exception.ApiControllerAdvice;
import com.jemini.stayhost.common.security.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

/**
 * @WebMvcTest 컨트롤러 슬라이스 테스트의 공통 Base 클래스.
 *
 * 주의: @WithMockUser 사용 금지.
 * PartnerIdResolver가 instanceof JwtPrincipal로 검사하므로,
 * Spring의 기본 User 객체를 넣으면 PartnerId가 null로 반환된다.
 * 반드시 setPartnerAuthentication()을 사용할 것.
 */
@Import({SecurityConfig.class, JwtAuthenticationFilter.class,
    JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class,
    WebMvcConfig.class, PartnerIdResolver.class, UserIdResolver.class,
    ApiControllerAdvice.class})
public abstract class ControllerTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected JwtProvider jwtProvider;

    @BeforeEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    protected void setPartnerAuthentication(final Long partnerId) {
        final JwtPrincipal principal = new JwtPrincipal(partnerId, "PARTNER", "EXTRANET");
        final UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            principal, null, List.of(new SimpleGrantedAuthority("ROLE_PARTNER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
