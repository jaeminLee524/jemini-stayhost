package com.jemini.stayhost.common.aop;

import com.jemini.stayhost.common.logging.LogMaskingUtils;
import com.jemini.stayhost.common.logging.MaskField;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Aspect
@Component
public class ApiLoggingAop {

    private static final Logger log = LoggerFactory.getLogger(ApiLoggingAop.class);

    @Around("within(com.jemini.stayhost..presentation.controller..*)")
    public Object logApiCall(final ProceedingJoinPoint joinPoint) throws Throwable {
        final String method = joinPoint.getSignature().toShortString();
        final String maskedArgs = Arrays.stream(joinPoint.getArgs())
            .map(this::toMaskedString)
            .collect(Collectors.joining(", "));

        log.info("[API_REQUEST] {} args=[{}]", method, maskedArgs);

        final Object result = joinPoint.proceed();

        log.info("[API_RESPONSE] {} result={}", method, toMaskedString(result));
        return result;
    }

    private String toMaskedString(final Object obj) {
        if (obj == null) {
            return "null";
        }

        final Field[] fields = obj.getClass().getDeclaredFields();
        final boolean hasMaskField = Arrays.stream(fields)
            .anyMatch(f -> f.isAnnotationPresent(MaskField.class));

        if (!hasMaskField) {
            return obj.toString();
        }

        final Map<String, Object> masked = new LinkedHashMap<>();
        for (final Field field : fields) {
            field.setAccessible(true);
            try {
                final Object value = field.get(obj);
                if (field.isAnnotationPresent(MaskField.class) && value instanceof String strValue) {
                    masked.put(field.getName(), LogMaskingUtils.mask(strValue));
                } else {
                    masked.put(field.getName(), value);
                }
            } catch (final IllegalAccessException e) {
                masked.put(field.getName(), "***");
            }
        }
        return obj.getClass().getSimpleName() + masked;
    }
}
