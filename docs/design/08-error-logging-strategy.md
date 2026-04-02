# 09. 에러 처리 및 로깅 전략

---

## 1. 에러 처리

### 1-1 예외 계층 구조

| 예외 타입 | HTTP 상태 | 용도 |
|----------|----------|------|
| `NotFoundException` (extends BusinessException) | 404 | 리소스 미존재 |
| `BusinessException` | 400 | 비즈니스 규칙 위반 |
| `BusinessException` (DUPLICATE 계열) | 409 | 중복/충돌 |
| `AuthenticationException` | 401 | 인증 실패 |
| `AuthorizationException` | 403 | 인가 실패 |

`BusinessException`은 `ErrorCode` enum을 필수로 받는다. 에러 코드 없는 예외가 생성되는 것을 컴파일 타임에 방지한다.

HTTP 상태 코드 결정은 presentation 계층의 관심사이므로, `ErrorCode`에 상태 코드를 넣지 않고 `ApiControllerAdvice`의 `resolveBusinessStatus()` 메서드에서 결정한다.

### 1-2 ErrorCode

`{DOMAIN}_{PROBLEM}` 형식으로 정의한다.

```java
// NotFoundException 계열 (404)
throw new NotFoundException(ErrorCode.PROPERTY_NOT_FOUND);
throw new NotFoundException(ErrorCode.RESERVATION_NOT_FOUND);

// BusinessException 계열 (400)
throw new BusinessException(ErrorCode.INVENTORY_INSUFFICIENT);
throw new BusinessException(ErrorCode.RESERVATION_ALREADY_CANCELLED);
throw new BusinessException(ErrorCode.INVALID_DATE_RANGE);

// BusinessException 계열 (409)
throw new BusinessException(ErrorCode.DUPLICATE_RESERVATION);
```

### 1-3 [ApiControllerAdvice](../../backend/src/main/java/com/jemini/stayhost/common/exception/ApiControllerAdvice.java)

모든 예외를 한 곳에서 처리한다. 예외 타입에 따라 HTTP 상태 코드와 로그 레벨이 결정된다.

| 예외 타입 | HTTP 상태 | 로그 레벨 |
|----------|----------|---------|
| `NotFoundException` | 404 | INFO |
| `BusinessException` (일반) | 400 | WARN |
| `BusinessException` (중복) | 409 | WARN |
| `AuthenticationException` | 401 | INFO |
| `AuthorizationException` | 403 | INFO |
| `MethodArgumentNotValidException` | 400 | INFO |
| `Exception` (fallback) | 500 | ERROR |

404, 401, 403은 클라이언트 실수이므로 INFO로 충분하다. ERROR로 남기면 모니터링 알림이 오발령된다.

### 1-4 ApiBaseResponse 구조

성공 응답:

```json
{
  "result": "SUCCESS",
  "data": { "id": 1, "name": "한강뷰 호텔", "region": "서울" },
  "error": null
}
```

에러 응답:

```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "INVENTORY_INSUFFICIENT",
    "message": "선택한 날짜에 객실이 매진되었습니다."
  }
}
```

`result`는 `ResultType` enum(`SUCCESS`, `ERROR`)을 사용한다.

---

## 2. 로깅 전략

### 2-1 로그 레벨 정책

| 레벨 | 사용 기준 | 예시 |
|------|----------|------|
| ERROR | 시스템 장애, 예상치 못한 예외 (500) | DB 연결 실패, NPE, 외부 API 타임아웃 |
| WARN | 주의가 필요한 비즈니스 예외 | 재고 부족 빈발, 정지 계정 접근 시도 |
| INFO | 정상 비즈니스 플로우, 예상된 클라이언트 오류 | 예약 생성/취소/확정, 404/401/403 응답 |
| DEBUG | 상세 디버깅 정보 | 쿼리 파라미터, 캐시 히트/미스, 락 획득 대기 |

### 2-2 [MDC (Mapped Diagnostic Context)](../../backend/src/main/java/com/jemini/stayhost/common/filter/MdcLoggingFilter.java)

요청별로 `traceId`와 `userId`를 MDC에 주입한다. 하나의 예약 요청이 여러 서비스 메서드를 거칠 때 모든 로그가 동일한 `traceId`를 공유하여 흐름 단위로 묶어볼 수 있다.

### 2-3 [JSON 포맷 로그](../../backend/src/main/resources/logback-spring.xml)

운영 환경에서 출력되는 JSON 로그 예시:

```json
{
  "timestamp": "2026-03-28T14:23:45.123+09:00",
  "level": "WARN",
  "logger": "c.j.s.common.exception.ApiControllerAdvice",
  "message": "[INVENTORY_INSUFFICIENT] 선택한 날짜에 객실이 매진되었습니다.",
  "traceId": "a3f9b1c2",
  "userId": "42",
  "thread": "http-nio-8080-exec-3"
}
```

JSON 구조를 갖춰두면 향후 ELK 스택이나 CloudWatch Logs 연동이 logback 설정 변경만으로 가능하다.

### 2-4 로그 포인트

| 로그 포인트 | 레벨 | 위치 |
|-----------|------|------|
| API 요청/응답 (메서드, URI, 상태코드) | INFO | `MdcLoggingFilter` |
| API 요청 바디/파라미터 | DEBUG | `MdcLoggingFilter` |
| 예약 생성/취소/확정 | INFO | `BookingService` |
| 재고 부족 충돌 | WARN | `InventoryService` |
| 비즈니스 예외 처리 | WARN | `ApiControllerAdvice` |
| 인증/인가 실패, 404 | INFO | `ApiControllerAdvice` |
| 캐시 무효화 | DEBUG | `CacheEvictListener` |
| 예상치 못한 서버 오류 | ERROR | `ApiControllerAdvice` |

### 2-5 [AOP 요청/응답 로깅](../../backend/src/main/java/com/jemini/stayhost/common/aop/ApiLoggingAop.java)

모든 Controller 메서드(`presentation.controller` 패키지)에 자동 적용된다. 요청/응답 바디를 JSON으로 직렬화하여 로깅하며, 민감 필드는 마스킹 처리한다.

### 2-6 개인정보 마스킹

[`@MaskField`](../../backend/src/main/java/com/jemini/stayhost/common/logging/MaskField.java) 커스텀 어노테이션이 붙은 필드를 [`LogMaskingUtils`](../../backend/src/main/java/com/jemini/stayhost/common/logging/LogMaskingUtils.java)가 감지하여 마스킹된 값으로 교체한다. 원본 객체는 변경하지 않는다.

- 로그에는 식별자(userId, reservationId)만 남긴다
- 이름, 전화번호, 이메일 등은 마스킹한다

### 2-7 [MdcTaskDecorator](../../backend/src/main/java/com/jemini/stayhost/common/config/MdcTaskDecorator.java) (비동기 MDC 전파)

`CompletableFuture` 병렬 처리, `@Async` 이벤트 리스너 등 별도 스레드에서 실행되는 작업에 부모 스레드의 MDC를 전파한다. 이 설정이 없으면 비동기 스레드에서 `traceId`가 비어있어 로그 추적이 불가능하다.
