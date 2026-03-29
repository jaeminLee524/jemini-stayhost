package com.jemini.stayhost.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jemini.stayhost.common.exception.ErrorCode;
import com.jemini.stayhost.common.response.ApiBaseResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final AuthenticationException authException
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        final ApiBaseResponse<?> body = ApiBaseResponse.error(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.getMessage());
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
