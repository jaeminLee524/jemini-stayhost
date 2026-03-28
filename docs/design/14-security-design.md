# 보안 설계: 인증/인가

> 작성일: 2026-03-28

---

## 1. 인증 (Authentication)

### 1.1 JWT 기반 토큰 인증

이 시스템은 세션 기반이 아닌 JWT(JSON Web Token) 기반 Stateless 인증을 채택한다.

#### 세션 대신 JWT를 선택한 이유

세션 방식은 서버가 로그인 상태를 직접 저장해야 한다.

- 수평 확장(Scale-out)을 하려면 모든 인스턴스가 같은 세션 저장소를 공유해야 한다
- 실무에서는 Redis 세션 스토어로 해결하지만, 인프라 복잡도가 높아진다

JWT는 토큰 자체에 인증 정보를 담는다.

- 서버는 서명(Signature)만 검증하면 되므로 별도의 공유 저장소가 불필요하다

| 기준 | 세션 방식 | JWT 방식 |
|------|----------|---------|
| 서버 상태 | Stateful (세션 저장) | Stateless (토큰 자급) |
| 수평 확장 | Redis 등 공유 스토어 필요 | 별도 인프라 불필요 |
| 강제 로그아웃 | 세션 삭제로 즉시 가능 | 토큰 만료까지 대기 (블랙리스트 필요) |
| 구현 비용 | Spring Session + Redis 설정 | jjwt 의존성 + 필터 하나 |

이번 설계에서 JWT를 선택한 근거:

- 구현 비용 대비 효과: Redis 세션 스토어 없이도 API 서버를 여러 대 띄울 수 있다
- Stateless 아키텍처의 명확성: 서버가 상태를 보관하지 않아 수평 확장이 자유롭다
- JWT의 단점(강제 로그아웃 어려움)은 Access Token 유효기간을 짧게(30분) 유지함으로써 완화한다

---

### 1.2 토큰 구조

#### Access Token

```
Header.Payload.Signature
```

Payload Claims:

| Claim | 값 예시 | 설명 |
|-------|--------|------|
| `sub` | `"42"` | 사용자 ID (User.id 또는 Partner.id) |
| `role` | `"GUEST"` | 역할 (GUEST / PARTNER / ADMIN) |
| `context` | `"USER"` | 인증 컨텍스트 (USER / PARTNER) |
| `iat` | Unix timestamp | 발급 시각 |
| `exp` | Unix timestamp | 만료 시각 (발급 후 30분) |

- TTL: 30분
- 서명 알고리즘: HMAC-SHA256 (HS256)
- 시크릿 키: 환경 변수(`JWT_SECRET`)로 주입, 코드에 하드코딩 금지

#### Refresh Token (DESIGN-ONLY)

- TTL: 7일
- 구현 범위에서는 Access Token만 발급한다. Refresh Token은 설계만 정의한다.
- 실제 운영에서는 Refresh Token을 DB(또는 Redis)에 저장하고, Access Token 만료 시 재발급 엔드포인트(`POST /api/auth/refresh`)를 제공하는 구조로 확장한다.

---

### 1.3 인증 흐름

#### 고객 로그인 (User)

```
1. POST /api/public/users/login
   Body: { "email": "user@example.com", "password": "..." }

2. UserService: 이메일로 User 조회 → BCrypt 패스워드 검증

3. 검증 성공 시 JWT 발급
   - sub: user.id
   - role: GUEST
   - context: USER
   - exp: now + 30분

4. 응답: { "accessToken": "eyJ..." }

5. 이후 요청: Authorization: Bearer eyJ...
```

#### 파트너 로그인 (Partner)

```
1. POST /api/public/extranet/auth/login
   Body: { "loginId": "partner001", "password": "..." }

2. PartnerService: loginId로 Partner 조회 → BCrypt 패스워드 검증
   → Partner의 상태가 ACTIVE인지 확인 (PENDING/SUSPENDED이면 거부)

3. 검증 성공 시 JWT 발급
   - sub: partner.id
   - role: PARTNER
   - context: PARTNER
   - partnerId: partner.id   ← 소유권 검증에 사용
   - exp: now + 30분

4. 응답: { "accessToken": "eyJ..." }
```

#### 요청 처리 흐름 (JwtAuthenticationFilter)

```
HTTP 요청
    │
    ▼
JwtAuthenticationFilter (OncePerRequestFilter)
    │
    ├─ Authorization 헤더 없음 → SecurityContext 비어있는 채로 통과
    │   (공개 API는 permitAll()로 허용, 인증 필요 API는 이후 401 반환)
    │
    ├─ Bearer 토큰 파싱
    │
    ├─ 서명 검증 실패 또는 만료 → 401 Unauthorized
    │
    └─ 검증 성공
        → UsernamePasswordAuthenticationToken 생성
           (principal: userId, authorities: [ROLE_GUEST / ROLE_PARTNER / ROLE_ADMIN])
        → SecurityContextHolder에 저장
        → 다음 필터로 전달
```

---

## 2. 인가 (Authorization)

### 2.1 역할 기반 접근 제어 (RBAC)

도메인 모델에서 식별한 역할을 그대로 Spring Security의 Authority로 매핑한다.

| 역할 | 대상 | Spring Authority |
|------|------|-----------------|
| `GUEST` | 일반 고객 (User) | `ROLE_GUEST` |
| `PARTNER` | 숙소 파트너 (Partner) | `ROLE_PARTNER` |
| `ADMIN` | 운영자 | `ROLE_ADMIN` |

`ADMIN`은 `PARTNER`와 `GUEST`의 권한을 포함하지 않는다. 역할은 중첩(hierarchy)이 아닌 독립으로 설계하여 각 역할의 책임이 명확히 분리된다.

---

### 2.2 API별 접근 권한 매트릭스

| API 그룹 | 엔드포인트 패턴 | GUEST | PARTNER | ADMIN | 비인증 |
|---------|--------------|-------|---------|-------|--------|
| 고객 인증 | `POST /api/public/users/signup` | - | - | - | O |
| 고객 인증 | `POST /api/public/users/login` | - | - | - | O |
| 내 정보 | `GET /api/users/me` | O | X | X | X |
| 검색/조회 | `GET /api/public/search/` | O | O | O | O (공개) |
| 숙소 상세 | `GET /api/public/properties/{id}` | O | O | O | O (공개) |
| 예약 생성 | `POST /api/reservations` | O | X | O | X |
| 예약 조회/취소 | `GET,POST /api/reservations/` | O (본인) | X | O | X |
| Extranet 인증 | `POST /api/public/extranet/auth/login` | - | - | - | O |
| Extranet 파트너 등록 | `POST /api/extranet/partners` | - | - | - | O |
| Extranet 전체 | `/api/extranet/` | X | O (본인 숙소만) | O | X |
| Admin 전체 | `/api/admin/` | X | X | O | X |

---

### 2.3 Spring Security 설정

```java
// common/config/SecurityConfig.java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtAuthenticationFilter jwtFilter) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)           // REST API: CSRF 불필요
            .sessionManagement(s ->
                s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 공개 엔드포인트 (/api/public/ 패턴으로 통일)
                .requestMatchers("/api/public/").permitAll()
                // 파트너 등록은 인증 불필요 (공개 엔드포인트가 아닌 예외)
                .requestMatchers(HttpMethod.POST, "/api/extranet/partners").permitAll()
                // 역할별 접근 제어
                .requestMatchers("/api/admin/").hasRole("ADMIN")
                .requestMatchers("/api/extranet/").hasRole("PARTNER")
                .requestMatchers("/api/reservations/").hasAnyRole("GUEST", "ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(e -> e
                .authenticationEntryPoint(customAuthEntryPoint())   // 401 JSON 응답
                .accessDeniedHandler(customAccessDeniedHandler())   // 403 JSON 응답
            )
            .build();
    }
}
```

---

### 2.4 파트너 숙소 소유권 검증

URL 패턴 기반 인가(`hasRole("PARTNER")`)만으로는 충분하지 않다. 파트너 A가 파트너 B의 숙소를 수정하는 요청을 막아야 한다.

이 검증은 서비스 레이어에서 명시적으로 수행한다.

```java
// property/application/PropertyService.java
public PropertyResponse updateProperty(Long propertyId,
                                       UpdatePropertyRequest request,
                                       Long requestingPartnerId) {
    final Property property = propertyRepository.findById(propertyId)
        .orElseThrow(() -> new PropertyNotFoundException(propertyId));

    // Tell Don't Ask: 객체에게 소유권 검증을 위임
    property.validateOwner(requestingPartnerId);

    property.update(request.name(), request.description());
    return PropertyResponse.from(property);
}
```

`requestingPartnerId`는 컨트롤러에서 래퍼 타입(`PartnerId`)으로 주입받는다.

```java
// property/presentation/controller/ExtranetPropertyController.java
@PutMapping("/api/extranet/properties/{id}")
public ApiBaseResponse<PropertyResponse> updateProperty(
        @PathVariable Long id,
        @RequestBody @Valid UpdatePropertyRequest request,
        PartnerId partnerId) {  // Resolver가 자동 주입
    return ApiBaseResponse.success(
        propertyService.updateProperty(id, request, partnerId.value())
    );
}
```

---

### 2.5 인증 정보 주입: 래퍼 타입 + ArgumentResolver

Controller에서 `@AuthenticationPrincipal`을 직접 사용하면 Spring Security 구현 세부사항에 의존하게 된다. 인증 방식이 변경되면 모든 Controller를 수정해야 한다.

래퍼 타입 + `HandlerMethodArgumentResolver`로 분리하면 Controller는 인증 구현을 모른 채 필요한 값만 받는다.

래퍼 타입:

```java
// common/security/UserId.java
public record UserId(Long value) {
    public static UserId of(Long value) {
        return new UserId(value);
    }
}

// common/security/PartnerId.java
public record PartnerId(Long value) {
    public static PartnerId of(Long value) {
        return new PartnerId(value);
    }
}
```

Resolver:

```java
// common/security/UserIdResolver.java
public class UserIdResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return UserId.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                   ModelAndViewContainer mavContainer,
                                   NativeWebRequest webRequest,
                                   WebDataBinderFactory binderFactory) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof JwtPrincipal principal)) {
            return null;  // 비인증 요청 → null
        }
        return UserId.of(principal.getUserId());
    }
}
```

Controller 사용 예시:

```java
// 인증 필요 API
@PostMapping("/api/reservations")
public ApiBaseResponse<ReservationResponse> create(
        @RequestBody CreateReservationRequest request,
        UserId userId) {
    return ApiBaseResponse.success(
        reservationFacade.create(userId.value(), request)
    );
}
```

- 공개 API(`/api/public/`)에서는 `UserId`를 주입받지 않는다. 인증이 필요한 API에서만 사용한다
- 인증 방식이 JWT → OAuth2로 변경되어도 Resolver만 수정하면 Controller는 그대로
- 정적 팩토리 메서드(`UserId.of()`)로 생성하여 코딩 원칙 준수

---

### 2.5 고민: 파트너와 고객의 인증 체계를 통합할 것인가

초기 설계에서 하나의 로그인 API로 고객과 파트너를 모두 처리하는 방안을 검토했다. 이메일/패스워드 기반이라는 공통점이 있기 때문이다.

그러나 실제 OTA에서 Extranet(파트너 포털)과 고객 서비스는 별도의 시스템으로 운영된다. 분리를 결정한 근거는 다음과 같다.

- 엔티티가 다르다. 고객은 `User` 엔티티, 파트너는 `Partner` 엔티티를 사용한다. Partner는 사업자 정보(사업자번호, 대표자, 계좌)와 로그인 계정(login_id, password)을 함께 가진다.
- JWT claims가 다르다. 파트너 토큰에는 `partnerId`가 포함되어야 숙소 소유권 검증이 가능하다. 고객 토큰에는 이 정보가 불필요하다.
- 접근 가능한 URL 패턴이 완전히 다르다. 고객은 `/api/` 하위, 파트너는 `/api/extranet/` 하위를 사용한다. Spring Security의 URL 기반 인가 규칙과도 자연스럽게 대응된다.

통합했을 때의 이점(코드 중복 감소)보다 분리했을 때의 이점(명확한 책임, 독립적 확장, 보안 오류 방지)이 크다고 판단했다.

---

## 3. 비밀번호 보안

### BCryptPasswordEncoder (strength 10)

```java
// common/config/SecurityConfig.java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(10);
}
```

BCrypt를 선택한 이유:

- 단방향 해시: 복호화 불가능. DB 유출 시 평문 노출 없음
- Salt 자동 포함: 레인보우 테이블 공격 방어
- Work Factor 조절 가능: strength 값으로 해싱 비용 조정 (10은 약 100ms)

평문 패스워드는 어디에도 저장하지 않는다. 로그, 에러 메시지, DB 어느 곳에도 노출되어서는 안 된다.

회원가입/파트너 등록 시:

```java
user.setPasswordHash(passwordEncoder.encode(rawPassword));
```

로그인 검증 시:

```java
if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
    throw new InvalidCredentialsException();
}
```

---

## 4. API 보안 고려사항 (DESIGN-ONLY)

이하 항목은 현재 구현 범위에 포함하지 않으나, 운영 환경 전환 시 반드시 적용해야 할 사항이다.

### 4.1 Rate Limiting

검색 API(`GET /api/search/`)는 인증 없이 접근 가능한 공개 엔드포인트다. 단기간 대량 요청에 취약할 수 있다.

향후 적용 방안:

- Bucket4j: Spring 내부에서 적용 가능. 토큰 버킷 알고리즘 기반. IP 또는 사용자 단위로 초당 요청 수 제한
- API Gateway: Kong, AWS API Gateway 등 인프라 레벨에서 제어. 서비스 코드와 무관하게 운용 가능

현재는 DB 조회 결과를 Caffeine 로컬 캐시로 보호하여 반복 요청의 DB 부하를 완화한다.

### 4.2 CORS

프론트엔드 배포 도메인만 허용한다. `*`(와일드카드) 허용은 금지한다.

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("https://stayhost.jemini.com"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    config.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/", config);
    return source;
}
```

로컬 개발 환경에서는 `http://localhost:3000`을 추가 허용한다. 이 설정은 Spring Profile로 분리한다.

### 4.3 SQL Injection 방어

JPA + Spring Data JPA의 Parameterized Query가 기본적으로 SQL Injection을 차단한다. JPQL, Criteria API 모두 PreparedStatement를 사용한다.

주의: `@Query`에서 `:param` 바인딩 대신 String concatenation으로 쿼리를 구성하는 패턴은 금지한다.

```java
// 금지
@Query("SELECT p FROM Property p WHERE p.name = '" + name + "'")

// 허용
@Query("SELECT p FROM Property p WHERE p.name = :name")
List<Property> findByName(@Param("name") String name);
```

### 4.4 XSS 방어

이 시스템은 HTML을 렌더링하지 않는다.

- 모든 응답은 `Content-Type: application/json`이며, 클라이언트가 JSON 데이터를 해석해 렌더링한다
- 서버 레벨의 HTML 이스케이프는 불필요하다
- 입력 검증은 `@Valid` + Bean Validation으로 처리한다
- 허용하지 않는 특수문자가 포함된 입력은 400 Bad Request로 거부한다

```java
public record CreatePropertyRequest(
    @NotBlank @Size(max = 100) String name,
    @NotBlank @Size(max = 2000) String description,
    @NotNull PropertyType type,
    @NotBlank String region
) { }
```

---

## 5. 예약 본인 확인

파트너 소유권 검증(2.4)과 마찬가지로, 고객의 예약 조회/취소 시에도 본인 예약인지 확인해야 한다.

```java
// booking/application/service/ReservationService.java
@Transactional(readOnly = true)
public ReservationResult getReservation(Long reservationId, Long requestingUserId) {
    Reservation reservation = reservationReader.getById(reservationId);

    // Tell Don't Ask: 객체에게 본인 확인을 위임
    reservation.validateOwner(requestingUserId);

    return ReservationResult.from(reservation);
}
```

---

## 6. Actuator 엔드포인트 보안

`/actuator/prometheus`는 내부 메트릭을 노출하므로 운영 환경에서 외부 접근을 차단해야 한다.

- 개발 환경: 전체 노출 허용
- 운영 환경: 내부 네트워크에서만 접근 가능하도록 제한하거나, Spring Security에서 ADMIN 역할 요구

```java
// SecurityFilterChain에 추가
.requestMatchers("/actuator/**").hasRole("ADMIN")  // 또는 IP 기반 제한
```

---

## 7. Swagger UI 접근 제한

Swagger UI는 API 명세가 외부에 노출되므로 운영 환경에서 비활성화한다.

- `@Profile("local")` 또는 `@Profile("dev")`로 Swagger 설정 클래스를 제한
- 운영 프로파일에서는 SpringDoc 자체를 비활성화: `springdoc.api-docs.enabled=false`
- 개발 환경에서는 GroupedOpenApi로 API 그룹별 문서 분리 (Extranet / Customer / Admin)

```java
@Profile("local")
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .components(new Components()
                .addSecuritySchemes("Bearer", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")))
            .security(List.of(new SecurityRequirement().addList("Bearer")))
            .info(new Info().title("StayHost API").version("v1"));
    }

    @Bean
    public GroupedOpenApi extranetApi() {
        return GroupedOpenApi.builder()
            .group("1-extranet")
            .pathsToMatch("/api/extranet/**", "/api/public/extranet/**")
            .build();
    }

    @Bean
    public GroupedOpenApi customerApi() {
        return GroupedOpenApi.builder()
            .group("2-customer")
            .pathsToMatch("/api/public/**", "/api/users/**", "/api/reservations/**")
            .build();
    }
}
```

---

## 8. OAuth2 / 소셜 로그인을 제외한 이유

소셜 로그인(Google, Kakao 등)은 현대 서비스에서 흔히 요구되는 기능이다. 그러나 이번 설계에서는 의도적으로 제외했다.

OAuth2 소셜 로그인을 추가하면 구현 흐름이 복잡해진다. Authorization Code Flow, Redirect URI 처리, 소셜 계정과 내부 계정의 연결(linking), 소셜 로그인 전용 회원과 일반 회원의 통합 관리가 필요하다. 이 모든 것을 제대로 구현하려면 상당한 공수가 필요하다.

JWT를 직접 발급하는 방식은 다음 장점이 있다.

- 인증/인가 설계를 직접 보여줄 수 있다. JwtAuthenticationFilter, SecurityFilterChain 설정, 역할 기반 접근 제어, 파트너 소유권 검증까지 인증 체계의 전반을 코드로 표현한다.
- 외부 의존 없음. 소셜 로그인 제공자의 API 변경에 영향받지 않는다.
- 범위가 명확하다. 이 시스템의 핵심은 숙소 예약 도메인이지, OAuth2 연동이 아니다.

---

## 9. 패키지 구조

보안 관련 코드는 `common/security/` 하위에 위치한다. 각 도메인에 흩어지지 않고 한 곳에 모인다.

```
com.jemini.stayhost
└── common/
    ├── config/
    │   └── SecurityConfig.java          ← SecurityFilterChain, PasswordEncoder, CORS 설정
    └── security/
        ├── JwtAuthenticationFilter.java  ← OncePerRequestFilter, 토큰 파싱 및 검증
        ├── JwtProvider.java              ← 토큰 생성(generate), 파싱(parse), 검증(validate)
        ├── JwtPrincipal.java             ← 인증 후 SecurityContext에 저장되는 사용자 정보
        └── JwtAuthenticationEntryPoint.java  ← 401/403 JSON 응답 처리
```

`JwtProvider`는 순수한 토큰 처리 유틸리티이며 Spring Bean이어도 무방하다. `JwtAuthenticationFilter`는 `OncePerRequestFilter`를 상속하여 요청당 1회만 실행됨을 보장한다.
