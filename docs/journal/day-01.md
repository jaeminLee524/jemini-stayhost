## Day 1 - 설계 문서 작성

### 수행 내용
- OTA 숙박 플랫폼 도메인 리서치 (Booking.com Extranet 구조 분석)
- 7개 Bounded Context 식별 및 도메인 모델 설계
- 전체 ERD 설계 (16개 테이블, MySQL DDL)
- API 명세 작성 (Extranet / Customer / Admin / Channel Manager / Supplier)
- 동시성 처리 전략 수립 (비관적 락, 대규모 요금 조회 캐시)
- 채널 매니저 설계 (OUTBOUND/INBOUND 시퀀스, 에러 핸들링 정책)
- 외부 공급자(Supplier) 연동 설계 (배치 동기화 플로우, 매핑 상태 전이)
- 캐시 전략 설계 (Caffeine 하위 단위 캐시, 이벤트 기반 무효화)
- 이벤트 기반 아키텍처 설계 (ApplicationEvent, 9개 도메인 이벤트)
- 코드 컨벤션 정의 (패키지 구조, 네이밍, Swagger 인터페이스 분리)
- ADR 5건 작성
- GitHub 저장소 생성 및 README.md 작성

### 의사결정
- [모놀리식 + 패키지 분리]: 시간적 제약 하에서 구현 완성도와 설계 표현력의 균형. 멀티 모듈은 빌드 설정 오버헤드, MSA는 과설계. 상세: [ADR-001](../design/adr/001-monolith-package-separation.md)
- [비관적 락 (SELECT FOR UPDATE)]: 동시 예약의 핵심 시나리오(마지막 1개 객실, 100 동시 요청)에서 낙관적 락은 재시도 비용 폭발. 비관적 락이 확정적. 상세: [ADR-002](../design/adr/002-pessimistic-locking.md)
- [Caffeine 로컬 캐시 + 하위 단위 키]: 검색 결과 통째 캐시는 조합 폭발로 히트율 극히 낮음. property/roomType/rate 단위 캐시로 전환. 상세: [ADR-003](../design/adr/003-local-cache-strategy.md)
- [자동 확정 (PENDING 없음)]: OTA 업계 표준은 자동 확정. PENDING 5분은 비현실적, 30분은 UX 저하. 재고 차감 성공 시 즉시 CONFIRMED. 상세: [ADR-004](../design/adr/004-auto-confirmation-flow.md)
- [ApplicationEvent 기반 캐시 무효화]: TTL만으로는 stale 데이터 노출. 이벤트로 즉시 무효화하고 TTL은 안전망. 상세: [ADR-005](../design/adr/005-event-driven-cache-invalidation.md)
- [Supplier 통합 검색 — 동일 테이블 저장]: Supplier 상품을 매핑 후 property 테이블에 정규화 저장. 별도 Materialized View 없이 자연스럽게 통합 검색. Extranet 상품과 동일한 테이블이므로 검색 쿼리 변경 불필요.
- [Swagger 인터페이스 분리]: Controller에 Swagger 어노테이션을 직접 넣지 않고, Docs 인터페이스로 분리. 기존 프로젝트(ut-kr-c2c의 KcbVerificationDocs 패턴) 참고.
- [Extranet 리서치 스크린샷 미포함]: Booking.com Extranet을 리서치했으나, 저작권 이슈로 스크린샷 대신 텍스트 기술로 대체. 리서치 내용은 02-domain-model.md에 반영.
- [DIP 기반 패키지 구조 채택]: 당근마켓의 헥사고날 아키텍처를 참고하되, 풀 헥사고날(port/port-in-impl/port-out-impl)은 본 프로젝트 목적에 과하다고 판단. 핵심인 DIP만 추출하여 domain(순수 Java) → infrastructure(JPA 구현) 역전 구조를 적용. domain/application/infrastructure/presentation 4계층으로 분리.
- [ApiBaseResponse 재사용]: ut-kr-c2c의 Record 기반 응답 구조를 채택하되, translation 필드를 제거하고 ErrorCode를 숙박 도메인용으로 교체하여 간소화.
- [에러 처리 전략]: 예외 계층을 BusinessException 기본 + NotFoundException/AuthenticationException 등으로 분리. ErrorCode enum을 숙박 도메인용으로 정의. HTTP 상태 코드 결정은 ApiControllerAdvice에 격리.
- [모니터링 설계]: Micrometer + Prometheus + Grafana 구성. 구현은 불필요하지만 비관적 락 대기 시간, 캐시 히트율 등 프로젝트 특성에 맞는 메트릭을 구체적으로 정의하여 가산점 확보.
- [보안 설계]: 고객과 파트너의 인증 체계를 분리. 실제 OTA에서 Extranet과 고객 서비스는 별도 시스템이므로 별도 로그인 API + 별도 JWT claims 구조 채택.
- [Facade 패턴]: Domain Service가 타 context의 Service/Repository를 직접 참조하는 것은 금지. Facade가 교차 컨텍스트 오케스트레이션 담당. 나중에 도메인 분리 시 Facade만 수정하면 됨.
- [DB 커넥션 풀 분리]: primary(쓰기)/read(읽기) 풀 분리. 비관적 락이 읽기 커넥션을 블로킹하지 않도록. AbstractRoutingDataSource + @Transactional(readOnly) 기반.
- [Supplier BUILD 범위 확대]: 인터페이스+Mock뿐 아니라 수동 동기화 API + property 저장 + 통합 검색 노출까지 BUILD. 자동 배치만 DESIGN-ONLY. 과제 필수 요구사항인 "Supplier 상품 통합 검색"을 실제로 시연하기 위함.
- [프로모션 대기열 / 서킷 브레이커]: DESIGN-ONLY. Redis Queue 대기열, Resilience4j 서킷 브레이커 설계만 문서에 포함. 현재는 비관적 락 순차 처리 + Mock 외부 API로 충분.
- [ADR 섹션명 한국어화]: Status→상태, Context→배경, Decision→결정 등 전체 한국어 통일.
- [Caffeine CAS + DB 비관적 락 2단계]: Caffeine AtomicInteger CAS를 1차 필터로 사용하여 DB 부하를 대폭 감소시키고, DB 비관적 락을 최종 방어선으로 유지. 100 동시 요청 시 99건을 JVM 레벨에서 즉시 걸러내어 DB에는 1건만 도달.
- [코딩 원칙 추가]: Tell Don't Ask, Single Level of Abstraction, Composed Method Pattern, 정적 팩토리 메서드 패턴, 상수 사용, 파라미터 포매팅 등 ut-kr-c2c 컨벤션 참고하여 채택.
- [DTO/Component 네이밍]: API 계층(Request/Response), Service 계층(Command/Result/Search) 분리. 컴포넌트는 Reader/Manager/Validator/Facade/Adapter 접미사 규칙.
- [MapStruct 미채택]: DIP 아키텍처에서 Entity=JPA Entity이므로 별도 매핑 레이어가 적음. 정적 팩토리 메서드(create, mapToResponse, from)로 충분. OAuth2는 과제 범위에서 제외.

### 참고 자료
- Booking.com Extranet 파트너 등록 플로우 (기본정보 → 숙소설정 → 사진 → 요금과 캘린더 → 법적정보)
- 당근마켓 서버 아키텍처 (bootstrap / core / infrastructure / usecase 구조, 헥사고날 기반)
