# k6 부하 테스트 결과

## 테스트 환경

| 항목 | 값 |
|------|-----|
| 일시 | 2026-04-02 |
| 앱 서버 | Docker (Spring Boot 3.4.4, JDK 21) |
| DB | MySQL 8.0 (Docker) |
| 캐시 | Caffeine (in-process) |
| 테스트 도구 | k6 v0.57.0 |
| 실행 환경 | macOS (로컬, 단일 머신) |

## 테스트 데이터

| 항목 | 수량 |
|------|------|
| 숙소 | 5개 (ACTIVE) |
| 객실 유형 | 25개 (숙소당 5개) |
| 요금 기간 | 30일 |
| 재고 | 객실당 20개/일 |
| 테스트 사용자 | 1명 |

## 시나리오 구성

mixed-load.js: 3개 시나리오를 동시에 실행하여 실제 트래픽 패턴을 시뮬레이션한다.

| 시나리오 | 역할 | 최대 VUs | 실행 시간 | Executor |
|---------|------|---------|----------|----------|
| search_browse | 숙소 검색 (캐시 히트/미스 혼합) | 150 | 80s | ramping-vus |
| detail_view | 숙소 상세 + 요금 조회 | 80 | 80s | ramping-vus |
| reservation_create | 예약 생성 (쓰기 경합) | 50 | 80s | ramping-vus |

총 최대 동시 사용자: 280 VUs

### Ramp-up 프로파일

```
search_browse:      0 → 50 → 100 → 150 → 150 → 0  (10s/30s/10s/20s/10s)
detail_view:        0 → 20 → 50 → 80 → 80 → 0      (10s/30s/10s/20s/10s)
reservation_create: 0 → 10 → 30 → 50 → 0            (15s/30s/20s/15s)
```

## 테스트 결과

### Threshold 판정

| Threshold | 기준 | 실측값 | 판정 |
|-----------|------|--------|------|
| search_duration p(95) | < 300ms | 3.37ms | PASS |
| search_duration p(99) | < 500ms | 9.97ms | PASS |
| detail_duration p(95) | < 300ms | 3.64ms | PASS |
| detail_duration p(99) | < 500ms | 10.42ms | PASS |
| rate_duration p(95) | < 500ms | 9.61ms | PASS |
| rate_duration p(99) | < 1000ms | 26.03ms | PASS |
| reservation_duration p(95) | < 1000ms | 7.55ms | PASS |
| reservation_duration p(99) | < 2000ms | 26.81ms | PASS |
| search_success rate | > 99% | 100% | PASS |
| detail_success rate | > 99% | 100% | PASS |
| rate_success rate | > 99% | 100% | PASS |

### 응답 시간 상세 (ms)

| 시나리오 | avg | min | p(50) | p(90) | p(95) | p(99) | max |
|---------|-----|-----|-------|-------|-------|-------|-----|
| 검색 (search) | 1.39 | 0.25 | 0.66 | 2.31 | 3.37 | 9.97 | 201.98 |
| 상세 (detail) | 1.55 | 0.30 | 0.71 | 2.45 | 3.64 | 10.42 | 201.59 |
| 요금 (rate) | 4.60 | 1.68 | 2.87 | 7.55 | 9.61 | 26.03 | 204.36 |
| 예약 (reservation) | 3.94 | 1.00 | 2.15 | 6.04 | 7.55 | 26.81 | 1000.00 |

### 처리량

| 지표 | 값 |
|------|-----|
| 총 요청 수 | 34,981건 |
| 처리량 (RPS) | 434.36 req/s |
| 총 반복 수 | 27,308회 |
| 테스트 시간 | 80.5s |
| 수신 데이터 | 51 MB (636 KB/s) |

### 예약 시나리오 상세

| 지표 | 값 |
|------|-----|
| 예약 성공 | 500건 |
| 재고 부족 (기대된 실패) | 3,333건 |
| 체크 성공률 | 100% |

재고 20개 x 25개 객실 = 총 500건의 예약이 정확히 성공하고, 이후 요청은 모두 재고 부족(INVENTORY_INSUFFICIENT)으로 처리되었다.
이는 비관적 잠금 기반의 동시성 제어가 정상 동작함을 의미한다.

## 요금 조회 최적화: Bulk IN 쿼리 전환 효과

요금 조회 시 per-roomType 루프(N+1)를 IN절 Bulk 쿼리 1회로 전환한 결과를 비교한다.

### 변경 내용

| 항목 | 변경 전 | 변경 후 |
|------|--------|--------|
| rate DB 쿼리 | roomType별 개별 쿼리 (N회) | IN절 Bulk 쿼리 (1회) |
| 캐시 키 | range-key (roomTypeId:startDate:endDate) | per-date (roomTypeId:date) |
| 캐시 eviction | prefix 스캔 O(n) | affectedDates 기반 정밀 삭제 O(k) |

### Bulk 전환 전후 비교 (숙소당 객실 5개 기준)

| 시나리오 | 지표 | 전환 전 (N+1) | 전환 후 (Bulk) | 개선율 |
|---------|------|-------------|--------------|--------|
| 검색 | p(95) | 4.80ms | 3.37ms | -30% |
| 검색 | p(99) | 109.77ms | 9.97ms | -91% |
| 상세 | p(95) | 4.62ms | 3.64ms | -21% |
| 상세 | p(99) | 119.83ms | 10.42ms | -91% |
| 요금 | p(95) | 8.20ms | 9.61ms | +17% |
| 요금 | p(99) | 172.17ms | 26.03ms | -85% |
| 예약 | p(95) | 22.17ms | 7.55ms | -66% |
| 예약 | p(99) | 188.21ms | 26.81ms | -86% |

p(99) tail latency가 전 시나리오에서 85~91% 감소했다. 객실 유형이 많을수록 N+1 → 1 전환 효과가 크다.

## 캐시 전략 요약

| 캐시 이름 | TTL | 대상 | Eviction 방식 |
|----------|-----|------|--------------|
| search | 1분 | 검색 결과 (region, keyword, page) | 숙소/객실 변경 시 전체 무효화 |
| property | 30분 | 숙소 상세 정보 | 해당 propertyId 키 evict |
| roomTypes | 10분 | 숙소별 객실 유형 목록 | 해당 propertyId 키 evict |
| rate | 3분 | roomType별 날짜별 요금 | affectedDates 기반 per-date evict |
| inventory | 미캐싱 | 재고 | 정합성 우선, 매번 DB 조회 |

### 캐시 효과 분석

- 검색 p(50) 0.66ms: 캐시 히트 시 sub-millisecond 응답
- 상세 조회 p(50) 0.71ms: property + roomTypes 캐시 활용
- 요금 조회 p(50) 2.87ms: Bulk IN 쿼리 1회 + warm-up 캐시 적재
- 예약 p(50) 2.15ms: 캐시 미사용(쓰기 트랜잭션), 재고 비관적 잠금 포함

## 한계 및 주의사항

- 로컬 단일 머신 테스트이므로 네트워크 지연이 없다. 실제 환경에서는 응답 시간이 증가한다.
- 테스트 데이터 규모가 작다 (숙소 5개). 실제 서비스 규모에서는 DB 쿼리 시간이 증가한다.
- Caffeine은 in-process 캐시이므로 다중 인스턴스 배포 시 인스턴스 간 캐시 불일치가 발생한다.
- JWT 토큰 1개로 모든 예약 VU가 공유하므로 사용자별 인증 부하가 반영되지 않는다.
