# jemini-stayhost

OTA(Online Travel Agency) 숙박 플랫폼의 백엔드 시스템 설계 및 구현 프로젝트.

숙소 파트너(Extranet), 고객 서비스, 채널 매니저, 외부 공급자(Supplier) 연동 등 OTA 도메인 전반을 다룬다.

## 기술 스택

| 항목 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.4 |
| Build | Gradle (Kotlin DSL) |
| ORM | Spring Data JPA (Hibernate) |
| Database | MySQL 8.0 |
| Cache | Caffeine (로컬 캐시) |
| API Docs | SpringDoc OpenAPI (Swagger) |
| Test | JUnit 5 + Testcontainers (MySQL) |
| Load Test | k6 |

## 설계 문서 안내

설계 문서는 `docs/design/` 디렉토리에서 확인할 수 있다.

### 핵심 설계

| 문서 | 설명 |
|------|------|
| [도메인 모델](docs/design/02-domain-model.md) | 7개 Bounded Context, 엔티티 설계 |
| [ERD](docs/design/03-erd.md) | 전체 테이블 DDL, 인덱스 전략 |
| [API 명세](docs/design/04-api-spec.md) | Extranet / Customer / Admin / Channel / Supplier API |
| [동시성 처리](docs/design/05-concurrency.md) | 비관적 락 예약, 대규모 요금 조회 전략 |

### 도메인 상세

| 문서 | 설명 |
|------|------|
| [채널 매니저](docs/design/06-channel-manager.md) | 타 OTA 재고/요금 동기화 설계 |
| [Supplier 연동](docs/design/07-supplier-integration.md) | 외부 공급자 배치 동기화 설계 |
| [캐시 전략](docs/design/08-cache-strategy.md) | Caffeine 하위 단위 캐시, 정합성 전략 |
| [이벤트 아키텍처](docs/design/09-event-architecture.md) | ApplicationEvent 기반 도메인 이벤트 설계 |
| [시퀀스 다이어그램](docs/design/10-sequence-diagrams.md) | 7개 핵심 플로우 시퀀스 (Mermaid) |

### 개발 가이드

| 문서 | 설명 |
|------|------|
| [코드 컨벤션](docs/design/11-code-conventions.md) | 패키지 구조(DIP), 네이밍, 응답 형식, Git 규칙 |
| [에러 처리 및 로깅](docs/design/12-error-logging-strategy.md) | 예외 계층, ErrorCode, 구조화 로깅 전략 |
| [모니터링 설계](docs/design/13-monitoring-design.md) | Micrometer + Prometheus + Grafana, 메트릭 정의 |
| [보안 설계](docs/design/14-security-design.md) | JWT 인증, RBAC 인가, API 보안 정책 |
| [테스트 전략](docs/design/15-test-strategy.md) | 동시성/통합/E2E/k6 부하 테스트 계획 |

### ADR (Architecture Decision Records)

| ADR | 결정 |
|-----|------|
| [ADR-001](docs/design/adr/001-monolith-package-separation.md) | 모놀리식 + 패키지 분리 아키텍처 |
| [ADR-002](docs/design/adr/002-pessimistic-locking.md) | 비관적 락 기반 예약 동시성 |
| [ADR-003](docs/design/adr/003-local-cache-strategy.md) | Caffeine 로컬 캐시 + 하위 단위 키 |
| [ADR-004](docs/design/adr/004-auto-confirmation-flow.md) | 자동 확정 기반 예약 플로우 |
| [ADR-005](docs/design/adr/005-event-driven-cache-invalidation.md) | 이벤트 기반 캐시 무효화 |

## 과정 기록서 (Progress Journal)

- [과정 기록서](docs/journal/progress-journal.md)
- [핵심 기능 기술적 의사결정](docs/journal/feature.md)
- [타사 OTA 플랫폼 리서치](docs/research/)
