package com.jemini.stayhost.common.exception;

import static java.util.stream.Collectors.joining;

import com.jemini.stayhost.common.response.ApiBaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class ApiControllerAdvice {

    private static final Logger log = LoggerFactory.getLogger(ApiControllerAdvice.class);

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiBaseResponse<?>> handleNotFoundException(final NotFoundException e) {
        log.info("[{}] {}", e.getErrorCode().name(), e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiBaseResponse.error(e.getErrorCode(), e.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiBaseResponse<?>> handleBusinessException(final BusinessException e) {
        log.warn("[{}] {}", e.getErrorCode().name(), e.getMessage());
        final HttpStatus status = resolveBusinessStatus(e.getErrorCode());
        return ResponseEntity.status(status)
            .body(ApiBaseResponse.error(e.getErrorCode(), e.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiBaseResponse<?>> handleAuthenticationException(final AuthenticationException e) {
        log.info("[AUTHENTICATION_FAILED] {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiBaseResponse.error(ErrorCode.UNAUTHORIZED, e.getMessage()));
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ApiBaseResponse<?>> handleAuthorizationException(final AuthorizationException e) {
        log.info("[AUTHORIZATION_FAILED] {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiBaseResponse.error(ErrorCode.FORBIDDEN, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiBaseResponse<?>> handleValidation(final MethodArgumentNotValidException e) {
        final String message = e.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .collect(joining(", "));
        log.info("[VALIDATION_ERROR] {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiBaseResponse.error(ErrorCode.VALIDATION_ERROR, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiBaseResponse<?>> handleException(final Exception e) {
        log.error("[INTERNAL_SERVER_ERROR] {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiBaseResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."));
    }

    private HttpStatus resolveBusinessStatus(final ErrorCode errorCode) {
        return switch (errorCode) {
            case DUPLICATE_RESERVATION, DUPLICATE_LOGIN_ID, DUPLICATE_BUSINESS_NUMBER, DUPLICATE_EMAIL -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
