# 보안 설계: 인증/인가

> 작성일: 2026-03-28

---

## 1. 인증 (Authentication)

### 1.1 JWT 기반 토큰 인증

세션 대신 JWT(JSON Web Token) 기반 Stateless 인증을 채택한다.

| 기준 | 세션 방식 | JWT 방식 |
|------|----------|---------|
| 서버 상태 | Stateful (세션 저장) | Stateless (토큰 자급) |
| 수평 확장 | Redis 등 공유 스토어 필요 | 별도 인프라 불필요 |
| 강제 로그아웃 | 세션 삭제로 즉시 가능 | 토큰 만료까지 대기 (블랙리스트 필요) |
| 구현 비용 | Spring Session + Redis 설정 | jjwt 의존성 + 필터 하나 |

JWT 선택 근거:

- Redis 세션 스토어 없이도 API 서버를 여러 대 띄울 수 있다
- 서버가 상태를 보관하지 않아 수평 확장이 자유롭다
- Access Token 유효기간을 짧게(30분) 유지하여 강제 로그아웃 단점을 완화한다

---

### 1.2 토큰 구조

#### Access Token Payload Claims

| Claim | 값 예시 | 설명 |
|-------|--------|------|
| `sub` | `"42"` | 사용자 ID (User.id 또는 Partner.id) |
| `role` | `"GUEST"` | 역할 (GUEST / PARTNER / ADMIN) |
| `context` | `"CUSTOMER"` | 인증 컨텍스트 (CUSTOMER / PARTNER) |
| `iat` | Unix timestamp | 발급 시각 |
| `exp` | Unix timestamp | 만료 시각 (발급 후 30분) |

- TTL: 30분
- 서명 알고리즘: HMAC-SHA256 (HS256)
- 시크릿 키: 환경 변수로 주입, 코드에 하드코딩 금지
- 파트너의 경우 `sub`에 Partner.id가 들어가며, 별도의 `partnerId` claim은 사용하지 않는다

> 토큰 생성/파싱/검증: [JwtProvider.java](../../backend/src/main/java/com/jemini/stayhost/common/security/JwtProvider.java)

#### Refresh Token (DESIGN-ONLY)

- TTL: 7일
- 현재 구현 범위에서는 Access Token만 발급한다
- 운영 환경에서는 Refresh Token을 DB(또는 Redis)에 저장하고, `POST /api/auth/refresh` 재발급 엔드포인트로 확장한다

---

### 1.3 인증 흐름

#### 고객 로그인

1. `POST /api/public/users/login` (email, password)
2. `UserService`: 이메일로 User 조회 → BCrypt 패스워드 검증
3. 검증 성공 시 JWT 발급 (sub: user.id, role: GUEST, context: CUSTOMER)
4. 응답: `{ "accessToken": "eyJ..." }`

#### 파트너 로그인

1. `POST /api/public/extranet/auth/login` (loginId, password)
2. `PartnerService`: loginId로 Partner 조회 → BCrypt 패스워드 검증 → 상태가 ACTIVE인지 확인
3. 검증 성공 시 JWT 발급 (sub: partner.id, role: PARTNER, context: PARTNER)
4. 응답: `{ "accessToken": "eyJ..." }`

#### 요청 처리 흐름

[JwtAuthenticationFilter.java](../../backend/src/main/java/com/jemini/stayhost/common/security/JwtAuthenticationFilter.java)는 `OncePerRequestFilter`를 상속하여 요청당 1회 실행된다.

1. Authorization 헤더에서 Bearer 토큰 추출
2. 헤더 없음 → SecurityContext 비어있는 채로 통과 (공개 API는 permitAll, 인증 필요 API는 401)
3. 서명 검증 실패 또는 만료 → 401 Unauthorized
4. 검증 성공 → [JwtPrincipal](../../backend/src/main/java/com/jemini/stayhost/common/security/JwtPrincipal.java)(subjectId, role, context)을 SecurityContext에 저장

---

## 2. 인가 (Authorization)

### 2.1 역할 기반 접근 제어 (RBAC)

| 역할 | 대상 | Spring Authority |
|------|------|-----------------|
| `GUEST` | 일반 고객 (User) | `ROLE_GUEST` |
| `PARTNER` | 숙소 파트너 (Partner) | `ROLE_PARTNER` |
| `ADMIN` | 운영자 | `ROLE_ADMIN` |

역할은 중첩(hierarchy)이 아닌 독립으로 설계한다. `ADMIN`은 `PARTNER`와 `GUEST`의 권한을 포함하지 않는다.

---

### 2.2 API별 접근 권한 매트릭스

| API 그룹 | 엔드포인트 패턴 | GUEST | PARTNER | ADMIN | 비인증 |
|---------|--------------|-------|---------|-------|--------|
| 고객 인증 | `POST /api/public/users/signup` | - | - | - | O |
| 고객 인증 | `POST /api/public/users/login` | - | - | - | O |
| 내 정보 | `GET /api/users/me` | O | X | X | X |
| 검색/조회 | `GET /api/public/search/**` | O | O | O | O (공개) |
| 숙소 상세 | `GET /api/public/properties/{id}` | O | O | O | O (공개) |
| 예약 생성 | `POST /api/reservations` | O | X | O | X |
| 예약 조회/취소 | `GET,POST /api/reservations/**` | O (본인) | X | O | X |
| Extranet 인증 | `POST /api/public/extranet/auth/login` | - | - | - | O |
| Extranet 파트너 등록 | `POST /api/extranet/partners` | - | - | - | O |
| Extranet 전체 | `/api/extranet/**` | X | O (본인 숙소만) | O | X |
| Admin 전체 | `/api/admin/**` | X | X | O | X |

> SecurityFilterChain 설정: [SecurityConfig.java](../../backend/src/main/java/com/jemini/stayhost/common/config/SecurityConfig.java)

---

### 2.3 파트너 숙소 소유권 검증

URL 패턴 기반 인가(`hasRole("PARTNER")`)만으로는 파트너 A가 파트너 B의 숙소를 수정하는 것을 막을 수 없다. 서비스 레이어에서 Tell Don't Ask 원칙으로 검증한다.

- `property.validateOwner(requestingPartnerId)`: Property 엔티티에 소유권 검증 위임
- `requestingPartnerId`는 컨트롤러에서 `PartnerId` 래퍼 타입으로 주입받는다

---

### 2.4 인증 정보 주입: 래퍼 타입 + ArgumentResolver

`@AuthenticationPrincipal`을 직접 사용하면 Spring Security 구현 세부사항에 의존하게 된다. 래퍼 타입 + `HandlerMethodArgumentResolver`로 분리하여 Controller는 인증 구현을 모른 채 필요한 값만 받는다.

래퍼 타입:

| 타입 | 용도 | 팩토리 메서드 |
|------|------|-------------|
| [UserId](../../backend/src/main/java/com/jemini/stayhost/common/security/UserId.java) | 고객 식별 | `UserId.create(value)` |
| [PartnerId](../../backend/src/main/java/com/jemini/stayhost/common/security/PartnerId.java) | 파트너 식별 | `PartnerId.create(value)` |

Resolver:

| Resolver | 동작 |
|----------|------|
| [UserIdResolver](../../backend/src/main/java/com/jemini/stayhost/common/security/UserIdResolver.java) | SecurityContext → `JwtPrincipal.subjectId()` → `UserId.create()` |
| [PartnerIdResolver](../../backend/src/main/java/com/jemini/stayhost/common/security/PartnerIdResolver.java) | SecurityContext → `JwtPrincipal.subjectId()` → `PartnerId.create()` |

- 공개 API(`/api/public/`)에서는 래퍼 타입을 주입받지 않는다
- 인증 방식이 JWT → OAuth2로 변경되어도 Resolver만 수정하면 Controller는 그대로

---

### 2.5 파트너/고객 인증 체계 분리 근거

초기에 하나의 로그인 API로 고객과 파트너를 모두 처리하는 방안을 검토했으나 분리를 결정했다.

- 엔티티가 다르다: 고객은 `User`, 파트너는 `Partner` (사업자 정보 + 로그인 계정)
- JWT claims가 다르다: 파트너 토큰의 `sub`은 Partner.id이며 숙소 소유권 검증에 사용된다
- URL 패턴이 완전히 다르다: 고객은 `/api/**`, 파트너는 `/api/extranet/**`

통합의 이점(코드 중복 감소)보다 분리의 이점(명확한 책임, 독립적 확장, 보안 오류 방지)이 크다.

---

## 3. 비밀번호 보안

BCryptPasswordEncoder (strength 10)를 사용한다.

- 단방향 해시: 복호화 불가능. DB 유출 시 평문 노출 없음
- Salt 자동 포함: 레인보우 테이블 공격 방어
- Work Factor 조절 가능: strength 10은 약 100ms

평문 패스워드는 어디에도 저장하지 않는다. 로그, 에러 메시지, DB 어느 곳에도 노출되어서는 안 된다.

---

## 4. 예약 본인 확인

고객의 예약 조회/취소 시 본인 예약인지 확인한다. 파트너 소유권 검증(2.3)과 동일하게 Tell Don't Ask 원칙으로 엔티티에 위임한다.

- `reservation.validateOwner(requestingUserId)`: Reservation 엔티티에 본인 확인 위임

---

## 5. API 보안 고려사항 (DESIGN-ONLY)

이하 항목은 현재 구현 범위에 포함하지 않으나, 운영 환경 전환 시 반드시 적용해야 할 사항이다.

### 5.1 Rate Limiting

검색 API(`GET /api/public/search/**`)는 인증 없이 접근 가능한 공개 엔드포인트다. 단기간 대량 요청에 취약할 수 있다.

- Bucket4j: 토큰 버킷 알고리즘 기반. IP 또는 사용자 단위 제한
- API Gateway: Kong, AWS API Gateway 등 인프라 레벨 제어

현재는 DB 조회 결과를 Caffeine 로컬 캐시로 보호하여 반복 요청의 DB 부하를 완화한다.

### 5.2 CORS

프론트엔드 배포 도메인만 허용한다. `*`(와일드카드) 허용은 금지한다. 로컬 개발 환경에서는 `http://localhost:3000`을 추가 허용하며, Spring Profile로 분리한다.

### 5.3 SQL Injection 방어

JPA + Spring Data JPA의 Parameterized Query가 기본적으로 SQL Injection을 차단한다. `@Query`에서 `:param` 바인딩 대신 String concatenation으로 쿼리를 구성하는 패턴은 금지한다.

### 5.4 XSS 방어

모든 응답은 `Content-Type: application/json`이며, 서버 레벨의 HTML 이스케이프는 불필요하다. 입력 검증은 `@Valid` + Bean Validation으로 처리한다.

---

## 6. Actuator/Swagger 접근 정책

| 엔드포인트 | 현재 설정 | 운영 환경 권장 |
|-----------|----------|--------------|
| `/actuator/**` | `permitAll()` | 내부 네트워크 제한 또는 `hasRole("ADMIN")` |
| `/swagger-ui/**`, `/v3/api-docs/**` | `permitAll()` | `springdoc.api-docs.enabled=false`로 비활성화 |

> Swagger 설정: [SwaggerConfig.java](../../backend/src/main/java/com/jemini/stayhost/common/config/SwaggerConfig.java) - GroupedOpenApi로 Extranet / Customer / Supplier / Common 그룹 분리

---

## 7. OAuth2 / 소셜 로그인을 제외한 이유

OAuth2 소셜 로그인은 Authorization Code Flow, Redirect URI, 소셜/내부 계정 연결 등 상당한 구현 공수가 필요하다. 이번 설계에서는 의도적으로 제외했다.

- JWT를 직접 발급하여 인증/인가 설계의 전반을 코드로 표현할 수 있다
- 외부 의존 없음. 소셜 로그인 제공자의 API 변경에 영향받지 않는다
- 이 시스템의 핵심은 숙소 예약 도메인이지, OAuth2 연동이 아니다

---

## 8. 패키지 구조

보안 관련 코드는 `common/` 하위에 위치한다.

```
com.jemini.stayhost.common
├── config/
│   └── SecurityConfig.java          ← SecurityFilterChain, PasswordEncoder
└── security/
    ├── JwtAuthenticationFilter.java  ← 토큰 파싱 및 검증 필터
    ├── JwtProvider.java              ← 토큰 생성/파싱/검증
    ├── JwtPrincipal.java             ← 인증 사용자 정보 (subjectId, role, context)
    ├── JwtAuthenticationEntryPoint.java  ← 401 JSON 응답
    ├── CustomAccessDeniedHandler.java    ← 403 JSON 응답
    ├── UserId.java / PartnerId.java      ← 래퍼 타입
    └── UserIdResolver.java / PartnerIdResolver.java  ← ArgumentResolver
```
