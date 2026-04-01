package com.jemini.stayhost.common.presentation;

import com.jemini.stayhost.common.config.SecurityConfig;
import com.jemini.stayhost.common.security.CustomAccessDeniedHandler;
import com.jemini.stayhost.common.security.JwtAuthenticationEntryPoint;
import com.jemini.stayhost.common.security.JwtAuthenticationFilter;
import com.jemini.stayhost.common.security.JwtProvider;
import com.jemini.stayhost.health.presentation.HealthCheckController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthCheckController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class HealthCheckControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private JwtProvider jwtProvider;

  @Test
  void healthCheck_returnsSuccess() throws Exception {
    mockMvc.perform(get("/api/public/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").value("SUCCESS"))
        .andExpect(jsonPath("$.data").value("OK"))
        .andExpect(jsonPath("$.error").isEmpty());
  }

  @Test
  void protectedEndpoint_withoutToken_returns401() throws Exception {
    mockMvc.perform(get("/api/reservations"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.result").value("ERROR"))
        .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
  }
}
