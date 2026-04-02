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

## 빠른 시작 가이드

### 사전 준비

| 도구 | 버전 | 설치 (macOS) |
|------|------|-------------|
| Docker | 27+ | [Docker Desktop](https://www.docker.com/products/docker-desktop/) |
| k6 | 0.57+ | `brew install k6` |

JDK, Gradle은 Docker 빌드 내에서 처리되므로 서버 기동에는 별도 설치가 필요 없다. 로컬 테스트 실행 시에는 JDK 21이 필요하다 (3단계 참고).

### 1단계: 서버 기동

```bash
# 프로젝트 클론
git clone <repository-url>
cd jemini-stayhost

# MySQL + Spring Boot 앱 기동 (초기 빌드 시 2~3분 소요)
docker compose up -d

# 기동 확인
curl -s http://localhost:8080/api/public/health
```

MySQL 스키마는 Flyway가 앱 시작 시 자동 마이그레이션한다.

앱 로그 확인이 필요하면:

```bash
docker logs -f stayhost-backend
```

### 2단계: Swagger UI 접속

브라우저에서 아래 주소로 접속한다.

```
http://localhost:8080/swagger-ui.html
```

API는 역할별로 구분되어 있다:

| 접두사 | 대상 | 인증 |
|--------|------|------|
| `/api/public/` | 비인증 공개 API (회원가입, 로그인, 검색) | 불필요 |
| `/api/extranet/` | 파트너 (숙소/객실/요금/재고 관리) | Bearer Token (파트너) |
| `/api/` | 고객 (예약 생성/조회/취소) | Bearer Token (고객) |

Swagger에서 인증이 필요한 API를 테스트하려면:

```bash
# 1. 회원가입
curl -X POST http://localhost:8080/api/public/users/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"password1234","name":"테스트","phone":"010-1234-5678"}'

# 2. 로그인 → accessToken 획득
curl -X POST http://localhost:8080/api/public/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"password1234"}'
```

응답의 `data.accessToken` 값을 Swagger UI 상단 Authorize 버튼에 `Bearer {토큰}` 형식으로 입력한다.

### 3단계: 테스트 실행

로컬에서 테스트를 실행하려면 JDK 21이 필요하다.

| 도구 | 버전 | 설치 (macOS) |
|------|------|-------------|
| JDK | 21+ | `brew install openjdk@21` |

```bash
cd backend

# 단위 테스트 + 통합 테스트 실행 (Testcontainers가 MySQL 컨테이너를 자동 생성)
./gradlew test
```

통합 테스트는 Testcontainers를 사용하므로 Docker가 실행 중이어야 한다.

### 4단계: k6 부하 테스트

```bash
# 1. k6 테스트 데이터 시딩 (숙소 5개, 객실 25개, 요금/재고 30일)
bash k6/scripts/seed.sh

# 2. 혼합 부하 테스트 실행 (검색 150 + 상세 80 + 예약 50 = 최대 280 VUs, 80초)
k6 run k6/scripts/mixed-load.js
```

개별 시나리오만 실행하려면:

```bash
k6 run k6/scripts/search-load.js      # 검색 전용 (100 VUs, 30s)
k6 run k6/scripts/reservation-load.js # 예약 전용 (50 VUs, 10s)
```

테스트 결과 분석은 [부하 테스트 결과 보고서](docs/test/k6-load-test-report.md)를 참고한다.

### 서버 종료 및 초기화

```bash
# 서버 종료
docker compose down

# DB 데이터까지 완전 초기화
docker compose down -v
```

---

## 설계 문서 안내

설계 문서는 `docs/design/` 디렉토리에서 확인할 수 있다.

### 핵심 설계

| 문서 | 설명 |
|------|------|
| [도메인 모델](docs/design/01-domain-model.md) | 7개 Bounded Context, 엔티티 설계 |
| [ERD](docs/design/02-erd.md) | 전체 테이블 DDL, 인덱스 전략 |
| [API 명세](docs/design/03-api-spec.md) | Extranet / Customer / Admin / Channel / Supplier API |
| [동시성 처리](docs/design/04-concurrency.md) | 비관적 락 예약, 대규모 요금 조회 전략 |

### 도메인 상세

| 문서 | 설명 |
|------|------|
| [캐시 전략](docs/design/05-cache-strategy.md) | Caffeine 하위 단위 캐시, 정합성 전략 |
| [이벤트 아키텍처](docs/design/06-event-architecture.md) | ApplicationEvent 기반 도메인 이벤트 설계 |

### 개발 가이드

| 문서 | 설명 |
|------|------|
| [코드 컨벤션](docs/design/08-code-conventions.md) | 패키지 구조(DIP), 네이밍, 응답 형식, Git 규칙 |
| [에러 처리 및 로깅](docs/design/09-error-logging-strategy.md) | 예외 계층, ErrorCode, 구조화 로깅 전략 |
| [모니터링 설계](docs/design/10-monitoring-design.md) | Micrometer + Prometheus + Grafana, 메트릭 정의 |
| [보안 설계](docs/design/11-security-design.md) | JWT 인증, RBAC 인가, API 보안 정책 |
| [테스트 전략](docs/design/12-test-strategy.md) | 동시성/통합/E2E/k6 부하 테스트 계획 |

### ADR (Architecture Decision Records)

| ADR | 결정 |
|-----|------|
| [ADR-001](docs/design/adr/001-monolith-package-separation.md) | 모놀리식 + 패키지 분리 아키텍처 |
| [ADR-002](docs/design/adr/002-pessimistic-locking.md) | 비관적 락 기반 예약 동시성 |
| [ADR-003](docs/design/adr/003-local-cache-strategy.md) | Caffeine 로컬 캐시 + 하위 단위 키 |
| [ADR-004](docs/design/adr/004-auto-confirmation-flow.md) | 자동 확정 기반 예약 플로우 |
| [ADR-005](docs/design/adr/005-event-driven-cache-invalidation.md) | 이벤트 기반 캐시 무효화 |

## 부하 테스트

| 문서 | 설명 |
|------|------|
| [k6 실행 가이드](k6/README.md) | 설치, 시딩, 실행 방법 |
| [부하 테스트 결과](docs/test/k6-load-test-report.md) | 280 VUs 혼합 부하 테스트 결과 및 캐시 효과 분석 |

## 과정 기록서 (Progress Journal)

- [과정 기록서](docs/journal/progress-journal.md)
- [핵심 기능 기술적 의사결정](docs/journal/feature.md)
- [타사 OTA 플랫폼 리서치](docs/research/)
