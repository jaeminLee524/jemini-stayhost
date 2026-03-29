package com.jemini.stayhost.common.filter;

import com.jemini.stayhost.common.security.JwtPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcLoggingFilter extends OncePerRequestFilter {

  private static final String TRACE_ID = "traceId";
  private static final String USER_ID = "userId";

  @Override
  protected void doFilterInternal(final HttpServletRequest request,
                  final HttpServletResponse response,
                  final FilterChain filterChain) throws ServletException, IOException {
    try {
      MDC.put(TRACE_ID, UUID.randomUUID().toString().substring(0, 8));
      putUserIdIfAuthenticated();
      filterChain.doFilter(request, response);
    } finally {
      MDC.clear();
    }
  }

  private void putUserIdIfAuthenticated() {
    final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof JwtPrincipal principal) {
      MDC.put(USER_ID, String.valueOf(principal.subjectId()));
    }
  }
}
