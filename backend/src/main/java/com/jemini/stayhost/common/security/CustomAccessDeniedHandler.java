package com.jemini.stayhost.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jemini.stayhost.common.exception.ErrorCode;
import com.jemini.stayhost.common.response.ApiBaseResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(final HttpServletRequest request,
                       final HttpServletResponse response,
                       final AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        final ApiBaseResponse<?> body = ApiBaseResponse.error(
                ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.getMessage()
        );
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
