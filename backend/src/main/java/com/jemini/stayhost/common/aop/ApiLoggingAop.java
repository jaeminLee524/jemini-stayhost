package com.jemini.stayhost.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ApiLoggingAop {

  private static final Logger log = LoggerFactory.getLogger(ApiLoggingAop.class);

  @Around("within(com.jemini.stayhost..presentation.controller..*)")
  public Object logApiCall(final ProceedingJoinPoint joinPoint) throws Throwable {
    final String method = joinPoint.getSignature().toShortString();

    log.info("[API_REQUEST] {} args={}", method, joinPoint.getArgs());

    final Object result = joinPoint.proceed();

    log.info("[API_RESPONSE] {} result={}", method, result);
    return result;
  }
}
