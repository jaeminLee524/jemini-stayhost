# 테스트 전략

> 작성일: 2026-03-28

---

## 1. 테스트 환경

- Testcontainers + MySQL: 실제 MySQL InnoDB 환경에서 통합 테스트 실행
  - H2는 `SELECT FOR UPDATE` 락 동작이 MySQL과 다르므로 사용하지 않는다
- JUnit 5 + Spring Boot Test: 테스트 프레임워크
- Mockito: 단위 테스트에서 외부 의존성 격리

---

## 2. 단위 테스트

| 대상 | 테스트 내용 |
|------|-----------|
| Entity 생성/상태 변경 | Reservation.create(), Reservation.cancel() 정적 팩토리 메서드 |
| 요금 계산 | 날짜별 요금 합산, 할인 적용 |
| 재고 차감/복원 | Inventory.decrease(), Inventory.restore() — 음수 재고/초과 예약 시 예외 (애플리케이션 레벨 검증) |
| 도메인 검증 | guest_count > max_occupancy 시 예외, 과거 날짜 예약 방지 |

---

## 3. 동시성 테스트 (핵심)

### 3.1 테스트 인프라

- Testcontainers + MySQL로 실제 InnoDB 비관적 락 동작 검증
- `ExecutorService` + `CountDownLatch`로 동시 요청 시뮬레이션
- `CompletableFuture`로 결과 수집 후 성공/실패 건수와 최종 재고 상태 단언

### 3.2 시나리오 A: 극단적 경합 (100:1)

```java
@Test
@DisplayName("100개 동시 예약 요청 시 재고 1개이면 정확히 1건만 성공해야 한다")
void concurrentReservation_100requests_1inventory() throws InterruptedException {
    // Given: 재고 1개인 객실 (Testcontainers MySQL)
    int threadCount = 100;
    int initialInventory = 1;
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(threadCount);
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failCount = new AtomicInteger(0);

    // When: 100개 스레드가 동시에 예약 요청 (startLatch.await()로 동시 출발)
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    for (int i = 0; i < threadCount; i++) {
        executor.submit(() -> {
            try {
                startLatch.await();
                reservationService.createReservation(request);
                successCount.incrementAndGet();
            } catch (InsufficientInventoryException e) {
                failCount.incrementAndGet();
            } finally {
                doneLatch.countDown();
            }
        });
    }
    startLatch.countDown(); // 전 스레드 동시 출발
    doneLatch.await();

    // Then
    assertThat(successCount.get()).isEqualTo(1);
    assertThat(failCount.get()).isEqualTo(99);

    Inventory inv = inventoryRepository.findByRoomTypeIdAndDate(roomTypeId, checkInDate);
    assertThat(inv.getReservedCount()).isEqualTo(1);
    assertThat(inv.getAvailableCount()).isEqualTo(0); // total - reserved
}
```

### 3.3 시나리오 B: 일반 경합 (50:10)

```java
@Test
@DisplayName("50개 동시 예약 요청 시 재고 10개이면 정확히 10건만 성공해야 한다")
void concurrentReservation_50requests_10inventory() throws InterruptedException {
    // Given: 재고 10개인 객실
    // When: 50개 스레드 동시 요청
    // Then: 정확히 10건 CONFIRMED, 40건 FAILED
    //       inventory.reserved_count == 10
}
```

### 3.4 시나리오 C: 멀티 나이트 데드락 검증

```java
@Test
@DisplayName("멀티 나이트 동시 예약 시 데드락이 발생하지 않아야 한다")
void multiNightReservation_noDeadlock() throws InterruptedException {
    // Given: 객실 A, B 각 재고 5개 / 3박 예약
    // When: 20개 스레드 동시 요청 (서로 다른 날짜 범위로 겹침)
    // Then: 데드락 없이 모든 요청이 성공 또는 실패로 완료
    //       타임아웃(DeadlockLoserDataAccessException) 발생 건수 == 0
}
```

### 3.5 시나리오 D: 읽기-쓰기 혼합

```java
@Test
@DisplayName("대규모 요금 조회와 동시 예약이 함께 발생해도 정합성이 유지되어야 한다")
void mixedReadWrite_rateQueryAndReservation() throws InterruptedException {
    // Given: 요금 조회 스레드 80개 + 예약 스레드 20개 동시 실행
    // When: 혼합 실행
    // Then: 예약 결과의 합계 == 재고 차감량
    //       요금 조회는 모두 정상 응답 (캐시 또는 DB)
}
```

---

## 4. 통합 테스트

| 시나리오 | 검증 내용 |
|---------|----------|
| 예약 생성 → 재고 차감 → 이벤트 발행 | 전체 흐름이 하나의 트랜잭션으로 동작하는지 |
| 예약 취소 → 재고 복원 | 취소 후 재고가 정확히 복원되는지 |
| 캐시 무효화 | 숙소 수정 → PropertyUpdatedEvent → Caffeine 캐시 제거 확인 |
| Supplier 동기화 | 수동 동기화 → property 테이블 저장 → 검색에 노출 |

---

## 5. API 테스트 (E2E)

| 시나리오 | 흐름 |
|---------|------|
| Extranet 전체 | 파트너 등록 → 숙소 등록 → 객실 추가 → 요금 설정 → 재고 설정 |
| Customer 전체 | 검색 → 요금 조회 → 예약 → 내 예약 조회 → 취소 |
| 전체 흐름 | 파트너 등록 → 숙소 등록 → 고객 예약 → 파트너 예약 확인 |

---

## 6. 부하 테스트 (k6)

### 6.1 테스트 시나리오

| 시나리오 | VU | Duration | 목적 |
|---------|-----|----------|------|
| 대규모 요금 조회 | 100 | 30s | Caffeine 캐시 히트율, P99 응답 시간 |
| 예약 동시성 | 50 | 10s | 비관적 락 처리량, 재고 정합성 |
| 혼합 워크로드 | 200 | 60s | 검색 70% + 예약 30% 실제 트래픽 패턴 |

### 6.2 성공 기준

| 메트릭 | 목표 |
|--------|------|
| 검색 P99 응답 시간 | < 500ms |
| 예약 P99 응답 시간 | < 2s |
| 에러율 | < 1% (재고 부족 제외) |
| 재고 정합성 | 최종 reserved_count == 성공 예약 건수 |

### 6.3 결과 기록

- k6 JSON output → `docs/test/k6-results/`에 저장
- 주요 메트릭(P50, P95, P99, 에러율)을 표로 정리하여 문서화

---

## 7. 테스트 네이밍 규칙

- 한글 메서드명 사용: `동시_예약_요청_시_재고_초과_방지()`
- Given-When-Then 구조를 주석으로 명시
- `@DisplayName`으로 시나리오 설명
