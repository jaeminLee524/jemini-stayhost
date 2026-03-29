## Day 2 - 설계 문서 리뷰 및 보강

### 수행 내용
- 설계 문서 전체 리뷰 및 보강
- member → user 테이블/엔티티 변경
- partner_user 테이블 partner로 통합 (불필요한 분리 제거)
- DIP 패키지 구조 확정: domain/repository 제거, domain/component만 유지
- application/service + application/facade 패키지 분리
- Reader/Manager 도구 레이어 호출 규칙 정립 (Service만 호출, Facade/Controller 직접 호출 금지)
- Facade 트랜잭션 규칙 (Facade: 트랜잭션 없음, Service만 @Transactional)
- Caffeine CAS 2단계 전략 시퀀스 다이어그램 추가
- TOCTOU 방어적 재검증 설계 (예약 생성 시 room_type 재확인, 취소 시 WHERE status='CONFIRMED')
- 래퍼 타입(UserId, PartnerId) + ArgumentResolver 인증 주입 패턴
- 에러/로깅 보강: AOP 요청/응답 로깅, MdcTaskDecorator, 비동기 Appender, 개인정보 마스킹(@MaskField)
- 보안 보강: 예약 본인 확인, Actuator 보안, Swagger 접근 제한(@Profile)
- 코딩 원칙 추가: Tell Don't Ask, SLA, 정적 팩토리 메서드(내부 builder), DTO/Component Naming
- reservation 가격 구조 변경 (total_price → base_price + discount_amount + final_price)
- DDL 보강: 전체 COMMENT 추가, 보조 인덱스 제거(개발 후 추가), CHECK 제약 제거(성능 고려)
- /api/public/ 비인증 엔드포인트 접두사 통일
- 채널 매니저: CompletableFuture 병렬 푸시, 백오프 조정(5초→15초→30초)
- ERD 중복 제거, ASCII→Mermaid 변환, bold 제거
- 테스트 전략 문서 분리 (15-test-strategy.md)
- 모니터링 문서 간소화
- GitHub 초기 push 완료

### 의사결정
- [member → user]: user가 더 범용적이고 직관적
- [partner_user 통합]: 7일 프로젝트에서 멀티 계정은 불필요. partner 테이블에 login_id/password 포함
- [domain/repository 제거]: Reader/Manager 인터페이스가 이미 데이터 접근을 추상화하므로 repository 인터페이스 중복
- [application/service + facade 분리]: 역할이 다른 두 계층을 패키지로 명확히 분리. 단순 context는 service만, 복잡한 context에만 facade 추가
- [Facade 트랜잭션 없음]: 읽기/검증 구간까지 트랜잭션에 포함하면 비관적 락 점유 시간 증가. Service에만 @Transactional
- [Reader/Manager에 @Transactional 없음]: 트랜잭션은 Service가 관리하는 유일한 계층. Reader/Manager는 순수 데이터 접근 도구
- [래퍼 타입 인증 주입]: @AuthenticationPrincipal 대신 UserId/PartnerId 래퍼 타입 + ArgumentResolver. Spring Security 구현 세부사항으로부터 Controller 격리
- [CHECK 제약 제거]: write마다 CHECK 조건 평가 시 성능 이슈. 애플리케이션 레벨 검증으로 충분
- [HTTP 상태 코드 활용]: 항상 200이 아닌, 400/404/409/500 등 적절한 상태 코드 반환. 모니터링(Micrometer)이 상태 코드 기반 에러율 집계
- [정적 팩토리 메서드 + 내부 builder]: 비즈니스 로직에서 new/builder 직접 사용 금지. 정적 팩토리 메서드 내부에서 builder 사용

### 참고 자료
- [지속 성장 가능한 소프트웨어를 만들어가는 방법](https://geminikims.medium.com/%EC%A7%80%EC%86%8D-%EC%84%B1%EC%9E%A5-%EA%B0%80%EB%8A%A5%ED%95%9C-%EC%86%8C%ED%94%84%ED%8A%B8%EC%9B%A8%EC%96%B4%EB%A5%BC-%EB%A7%8C%EB%93%A4%EC%96%B4%EA%B0%80%EB%8A%94-%EB%B0%A9%EB%B2%95-97844c5dab63)
- [당근페이 백엔드 아키텍처가 걸어온 여정](https://medium.com/daangn/%EB%8B%B9%EA%B7%BC%ED%8E%98%EC%9D%B4-%EB%B0%B1%EC%97%94%EB%93%9C-%EC%95%84%ED%82%A4%ED%85%8D%EC%B2%98%EA%B0%80-%EA%B1%B8%EC%96%B4%EC%98%A8-%EC%97%AC%EC%A0%95-98615d5a6b06)
- [Booking.com Extranet](https://account.booking.com/sign-in)
- 기존 프로젝트: MDCLoggingFilter, MdcTaskDecorator, LoggingAop, DevOpenApiConfig 패턴 참고

---

## Day 2 (cont.) - 구현 (3/29~30)

### 수행 내용

Extranet API 구현:
- 파트너 등록/로그인 API (JWT 발급)
- 숙소 CRUD + 상태 변경 API
- 객실 유형 CRUD API
- 요금 날짜 범위 bulk 설정/조회 API (daysOfWeek 필터링, UPSERT)
- 재고 날짜 범위 bulk 설정/조회 API (totalCount < reservedCount 거부)

Customer API 구현:
- 회원가입/로그인/내정보 조회 API

코드 품질 개선:
- 전체 Java 파일 들여쓰기 2칸 -> 4칸 통일 (intellij-java-google-style.xml 기준)
- Step-Down Rule 적용 (private 메서드 호출 순서대로 배치)
- Command DTO @Builder 패턴 통일, Request toCommand()에서 builder 직접 사용 + this. 키워드
- Response/Result DTO @Builder 패턴 통일
- Rate/Inventory Service Composed Method Pattern 적용
- Inventory.updateTotalCount() 도메인 내부 검증 (Tell Don't Ask)
- Property.create()에 latitude/longitude/thumbnailUrl 추가 (API 스펙 충족)
- Partner.create()에 bankName/bankAccount 추가
- Property.update()/Partner.update() partial update 지원 (null 필드 무시)
- RoomType.update()에 amenities 반영
- 가격 유효성 검증 (@Positive) 추가
- 날짜 범위 검증 통일 (MAX_DATE_RANGE_DAYS 30일 제한)
- 하드코딩 상수 추출 (ROLE_PARTNER, SECONDS_DIVISOR 등)
- 메서드 파라미터 개행 원칙 적용

테스트 (Phase 1-2, 70개):
- Domain Model Unit Tests 29개 (Partner, Property, RoomType, Rate, Inventory)
- JwtProvider Unit Tests 6개
- ControllerTestBase + smoke test 2개 (PartnerId ArgumentResolver 체인 검증)
- Service Unit Tests 31개 (PartnerService, PropertyService, RoomTypeService, RateService, InventoryService)

문서:
- 코드 컨벤션 docs에 Step-Down Rule, 상수 추출 원칙, 메서드 파라미터 개행 원칙 추가
- 들여쓰기 4스페이스로 문서 정정
- timeline.md 순서 변경 (Customer API -> Extranet 예약 조회)

### 의사결정
- [Bottom-Up 테스트 전략]: Domain Unit -> Service Unit -> Controller Slice -> Integration 순. 테스트가 없는 상태에서 빠른 단위 테스트 기반 구축 우선
- [ControllerTestBase에 setPartnerAuthentication()]: @WithMockUser 사용 금지. PartnerIdResolver가 instanceof JwtPrincipal로 검사하므로 SecurityContextHolder에 JwtPrincipal 직접 세팅
- [Request DTO에서 early validation]: toCommand() 시점에 날짜 범위/가격 검증. Service 레이어에도 방어적 검증 유지 (프로그래밍적 호출 대비)
- [Customer API 먼저 구현]: 예약 생성(Customer)이 없으면 예약 조회(Extranet)를 테스트할 데이터가 없으므로 순서 변경
