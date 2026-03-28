# 모니터링 설계

> 작성일: 2026-03-28
> 상태: DESIGN-ONLY (구현 불필요)

---

## 1. 구성

```
Application (Micrometer) → Prometheus (수집) → Grafana (시각화 + 알림)
```

- Spring Boot Actuator + Micrometer로 `/actuator/prometheus` 엔드포인트 노출
- Prometheus가 주기적으로 scrape하여 시계열 데이터 저장
- Grafana 대시보드로 시각화 + 임계값 알림

---

## 2. 수집 메트릭

### 비즈니스

| 메트릭 | 타입 | 설명 |
|--------|------|------|
| `reservation.created` | Counter | 예약 생성 수 |
| `reservation.cancelled` | Counter | 예약 취소 수 |
| `reservation.concurrent.conflict` | Counter | 동시 예약 충돌 수 |
| `search.request` | Counter | 검색 요청 수 |
| `supplier.sync.success/fail` | Counter | Supplier 동기화 성공/실패 |
| `channel.sync.success/fail` | Counter | Channel 동기화 성공/실패 |

### 성능

| 메트릭 | 타입 | 설명 |
|--------|------|------|
| `reservation.create.latency` | Timer | 예약 생성 응답 시간 |
| `search.latency` | Timer | 검색 응답 시간 |
| `cache.hit.ratio` | Gauge | Caffeine 캐시 히트율 |
| `pessimistic.lock.wait` | Timer | 비관적 락 대기 시간 |

### 인프라 (Actuator 자동 수집)

| 메트릭 | 설명 |
|--------|------|
| `jvm.memory.used` | JVM 메모리 사용량 |
| `hikari.connections.active` | DB 커넥션 풀 활성 수 |
| `hikari.connections.pending` | DB 커넥션 대기 수 |
| `http.server.requests` | HTTP 요청 응답 시간 |

---

## 3. 알림 정책

| 심각도 | 조건 | 대응 |
|--------|------|------|
| Critical | 5XX 에러율 > 5%, 커넥션 풀 고갈, JVM 힙 > 95% | 즉시 대응 |
| Warning | P99 응답 시간 초과, 캐시 히트율 < 50%, 동시 충돌 빈발 | 30분 내 확인 |
| Info | Supplier 배치 완료, 일일 예약 통계 | 참고용 |

---

## 4. 추가 모니터링

| 대상 | 방법 | 비고 |
|------|------|------|
| Caffeine 캐시 히트/미스 | `Caffeine.newBuilder().recordStats()` + Micrometer 자동 연동 | 설정 한 줄로 `cache.gets{result=hit/miss}` 메트릭 노출 |
| Slow Query | MySQL `slow_query_log` 또는 Hibernate `hibernate.generate_statistics` | 비관적 락 쿼리 병목 감지 |
| GC 빈도/시간 | Actuator 자동 수집 (`jvm.gc.pause`) | Full GC 빈발 시 알림 |
| 스레드 풀 상태 | `executor.active`, `executor.queue.remaining` | 채널 매니저 전용 풀 포화 감지 |

---

## 5. 선택 근거

- Prometheus + Grafana: 오픈소스, Spring Boot Actuator와 자연스럽게 결합
- Micrometer: Spring Boot 3.x 기본 메트릭 파사드, 벤더 중립적 (향후 Datadog 등으로 교체 가능)
- 분산 트레이싱(Zipkin/Jaeger)은 서비스 안정화 이후 도입 검토
