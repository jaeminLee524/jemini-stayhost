# CLAUDE.md — 프로젝트 컨텍스트

## 기술 스택
- Java 21, Spring Boot 3.4, Gradle (Kotlin DSL), MySQL 8.0
- Spring Data JPA (Hibernate), Caffeine (로컬 캐시), Spring Security + JWT, SpringDoc OpenAPI
- Testcontainers (MySQL), k6 (부하 테스트)

## 아키텍처
- 모놀리식 + DIP(Dependency Inversion Principle) 적용
- 당근마켓 헥사고날 아키텍처 참고, 풀 헥사고날은 아님
- 패키지 구조: `{bounded-context}/domain/application/infrastructure/presentation`
- domain은 JPA 어노테이션(@Entity, @Id 등)만 허용, Spring 의존 금지
- Reader/Manager 인터페이스는 domain/component/, 구현체는 infrastructure/component/
- Reader/Manager에 @Transactional 없음. Service가 트랜잭션을 관리하는 유일한 계층
- Facade/Controller에서 Reader/Manager 직접 호출 금지. 반드시 Service 경유
- Cross-context 참조는 Facade 패턴 (복잡한 비즈니스에서만 사용, 단순 CRUD는 Controller → Service 직접)
- DB 커넥션 풀 분리: primary(쓰기) / read(읽기) — AbstractRoutingDataSource + @Transactional(readOnly)
- 인증 정보 주입: 래퍼 타입(UserId, PartnerId) + HandlerMethodArgumentResolver. @AuthenticationPrincipal 사용 금지

## 패키지 구조
```
com.jemini.stayhost
├── partner/         # 파트너 관리
├── property/        # 숙소, 객실, 요금, 재고
├── booking/         # 예약, 취소
├── user/            # 회원 (고객)
├── search/          # 검색 + 캐시 (읽기 전용)
├── channel/         # 채널 매니저 (DESIGN-ONLY)
├── supplier/        # 외부 공급자 (DESIGN-ONLY)
└── common/          # 공통 (config, exception, response, security)
```
각 bounded context 내부:
```
domain/model/              # Entity, VO
domain/component/          # Reader/Manager 인터페이스 (도구 계약)
domain/event/              # 도메인 이벤트
application/service/       # Service (@Transactional 관리)
application/facade/        # Facade (cross-context 조합 시에만, 트랜잭션 없음)
infrastructure/persistence/ # JPA Repository 구현
infrastructure/component/   # Reader/Manager 구현체 (@Component)
infrastructure/adapter/     # 외부 API 어댑터
presentation/controller/    # REST Controller
presentation/docs/          # Swagger Docs 인터페이스
presentation/dto/           # Request/Response DTO
```

## 호출 규칙
```
단순:     Controller → Service → Reader/Manager → Repository
복잡:     Controller → Facade(트랜잭션 없음) → Service(@Transactional) → Reader/Manager → Repository
```

## 핵심 설계 결정
- Caffeine CAS + DB 비관적 락 2단계 (1차: JVM CAS 필터링, 2차: SELECT FOR UPDATE)
- 자동 확정 (재고 차감 = 즉시 CONFIRMED, PENDING 없음)
- Caffeine 하위 단위 캐시 (property/roomType/rate 단위, 검색 결과 통째 캐시 폐기)
- ApplicationEvent 기반 캐시 무효화 (@TransactionalEventListener AFTER_COMMIT)
- Supplier 통합 검색 (매핑 후 동일 property 테이블 저장, MV 불필요)
- Facade 트랜잭션 분리 (Facade 트랜잭션 없음, Service에만 @Transactional)
- TOCTOU 방지: Service 내 방어적 재검증, 취소 시 WHERE status='CONFIRMED' + affected rows 체크
- 비인증 엔드포인트: /api/public/ 접두사
- HTTP 상태 코드 활용 (항상 200이 아닌, 400/404/409/500 등 적절한 상태 코드 반환)

## 코딩 원칙
- Tell Don't Ask: 객체에게 판단을 위임 (obj.validateOwner(id))
- Single Level of Abstraction + Composed Method Pattern
- 정적 팩토리 메서드: builder는 내부에서만, 비즈니스 로직에서는 Reservation.create(cmd, ...) 호출
- DTO Naming: API(Request/Response), Service(Command/Result/Search)
- Component Naming: Reader(읽기), Manager(쓰기), Validator(검증), Facade(조합), Adapter(외부)
- 래퍼 타입 인증 주입: UserId.of(), PartnerId.of() + ArgumentResolver

## 참고 프로젝트 패턴 (ut-kr-c2c)
- ApiBaseResponse: Record 기반 응답, translation 필드 제거
- ApiControllerAdvice: 예외 타입별 핸들러 + 로그 레벨 분리
- KcbVerificationDocs: Swagger Docs 인터페이스 분리
- EventPublisher: ApplicationEventPublisher 래핑
- MdcTaskDecorator: 비동기 스레드 MDC 전파
- DevOpenApiConfig: @Profile 기반 Swagger 접근 제한 + GroupedOpenApi

## 프로젝트 구조
```
jemini-stayhost/
├── backend/                # Spring Boot (구현 시 생성)
├── k6/scripts/             # k6 부하 테스트 스크립트
├── docs/
│   ├── design/             # 설계 문서 15개 + ADR 5개 = 20개
│   ├── journal/            # 과정 기록서 (Day별)
│   ├── ai-usage/           # AI 활용 기록
│   └── test/k6-results/    # 테스트 결과
├── docker-compose.yml      # MySQL + backend + (optional) k6
└── README.md
```

## 현재 진행 상태
- [x] Day 1-2: 설계 문서 20개 작성 + 리뷰/보강 완료
- [x] GitHub 저장소 생성 (jaeminLee524/jemini-stayhost)
- [x] 과정 기록서 / AI 활용 기록 작성
- [x] 초기 커밋 + push
- [ ] Day 3: Spring Boot 프로젝트 셋업 + Entity + Docker Compose
- [ ] Day 4: Extranet API
- [ ] Day 5: Customer API + 검색 + 캐시
- [ ] Day 6: 예약/취소 + 동시성 + Testcontainers 테스트
- [ ] Day 7: Channel/Supplier 인터페이스 + k6 + 마무리

## 주의사항
- 코드/문서에 "여기어때", "채용 과제" 포함 금지
- frontend 디렉토리는 최저 우선순위, 삭제해도 나머지에 영향 없도록 설계
- docker-compose에서 frontend는 profile로만 존재
- 문서 추가/변경 시 README.md, journal, ai-usage도 함께 갱신할 것
- bold(**) 사용 금지 — IntelliJ 마크다운에서 한글 앞뒤 렌더링 안 됨

## 커밋 규칙
Conventional Commits: `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`
