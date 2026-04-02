# 12. 테스트 전략

---

## 1. 테스트 환경

| 도구 | 용도 |
|------|------|
| JUnit 5 + Mockito | 단위 테스트 (JVM, 외부 의존성 격리) |
| SpringBootTest + Testcontainers MySQL | 통합/동시성 테스트 (실제 InnoDB 환경) |
| k6 | 부하 테스트 (HTTP 레벨) |

H2는 `SELECT FOR UPDATE` 락 동작이 MySQL과 다르므로 사용하지 않는다. 동시성이 핵심인 프로젝트이므로 실제 DB에서 검증한다.

---

## 2. 단위 테스트

| 대상 | 테스트 내용 | 파일 |
|------|-----------|------|
| 예약 엔티티 | create(), cancel() 정적 팩토리, 상태 전이 | `ReservationTest` |
| 재고 엔티티 | decrease(), restore(), 음수 재고 예외 | `InventoryTest` |
| 요금 엔티티 | Rate.create(), 기간 검증 | `RateTest` |
| 숙소/객실 엔티티 | Property, RoomType 생성 및 상태 변경 | `PropertyTest`, `RoomTypeTest` |
| 파트너 엔티티 | Partner 생성, 활성화 | `PartnerTest` |
| 예약 서비스 | 투숙인원 초과, 날짜 검증, 요금 계산 | `ReservationServiceTest` |
| 숙소/객실/요금/재고 서비스 | CRUD 비즈니스 로직 | `PropertyServiceTest`, `RoomTypeServiceTest`, `RateServiceTest`, `InventoryServiceTest` |
| 검색 서비스 | Bulk IN 쿼리, basePrice 폴백 | `SearchServiceTest` |
| 캐시 무효화 | 이벤트별 캐시 evict 검증 | `CacheEvictListenerTest` |
| JWT 인증 | 토큰 생성/검증, 필터 동작 | `JwtProviderTest`, `JwtAuthenticationFilterTest` |
| 채널 매니저 | 병렬 푸시, 이벤트 리스너 | `ChannelManagerServiceTest` |

---

## 3. 동시성 테스트 (Testcontainers + MySQL)

`ExecutorService` + `CountDownLatch`로 동시 요청을 시뮬레이션한다.

### 시나리오

| 시나리오 | 스레드 | 재고 | 기대 결과 | 검증 포인트 |
|---------|--------|------|----------|-----------|
| 재고 1개 동시 예약 (1박) | 50 | 1 | 1건 성공, 49건 실패 | 비관적 락 정합성 |
| 재고 10개 동시 예약 (1박) | 50 | 10 | 10건 성공, 40건 실패 | 다수 재고 경합 |
| 멀티 나이트 동시 예약 (3박) | 30 | 5 | 5건 성공, 25건 실패, 데드락 0건 | ORDER BY 기반 락 순서 보장 |
| 동일 예약 동시 취소 | 20 | - | 1건 성공, 19건 실패 | 취소 멱등성 |

멀티 나이트 테스트는 3박(3개 inventory 행)에 대해 동시 락 획득이 일관된 순서(`ORDER BY roomTypeId, date`)로 이루어져 데드락이 발생하지 않음을 검증한다.

---

## 4. 통합 테스트 (E2E)

### Extranet 플로우 (`PartnerPropertyFlowIntegrationTest`)

파트너 등록 → 숙소 등록 → 활성화 → 객실 추가 → 요금 설정 → 재고 설정 → 검색 노출 확인

### Customer 플로우 (`UserReservationFlowIntegrationTest`)

회원가입 → 검색 → 요금 조회 → 예약 생성 → 내 예약 조회 → 예약 취소 → 재고 복원 확인

### 예약 엣지케이스 (`ReservationEdgeCaseIntegrationTest`)

| 시나리오 | 기대 결과 |
|---------|----------|
| 재고 소진 후 추가 예약 | 400 INVENTORY_INSUFFICIENT |
| 이중 취소 | 400 RESERVATION_ALREADY_CANCELLED |
| 타인 예약 접근 | 403 |
| 타 파트너 숙소 수정 | 403 |
| 투숙인원 초과 | 400 |
| 비인증 요청 | 401 |
| 유효하지 않은 날짜 | 400 |
| 비활성 숙소 예약 | 404 |
| 존재하지 않는 예약 | 404 |

### 취소 → 재예약 (`ReservationRebookIntegrationTest`)

예약 생성 → 재고 소진 → 취소 → 재고 복원 → 재예약 성공

### 검색 노출 (`PropertySearchIntegrationTest`)

비활성 숙소 미노출 → 활성화 후 노출 → 객실 포함 확인 → 키워드 검색

---

## 5. 부하 테스트 (k6)

### 시나리오

| 시나리오 | 최대 VUs | 시간 | 파일 |
|---------|---------|------|------|
| 검색 전용 (검색 → 상세 → 요금) | 100 | 30s | `search-load.js` |
| 예약 전용 (쓰기 경합) | 50 | 10s | `reservation-load.js` |
| 혼합 워크로드 (검색 150 + 상세 80 + 예약 50) | 280 | 80s | `mixed-load.js` |

### Threshold 기준

| 메트릭 | 목표 |
|--------|------|
| 검색 p(95) / p(99) | < 300ms / < 500ms |
| 요금 p(95) / p(99) | < 500ms / < 1000ms |
| 예약 p(95) / p(99) | < 1000ms / < 2000ms |
| 에러율 (재고 부족 제외) | > 99% 성공 |

### 실행 방법

```bash
bash k6/scripts/seed.sh           # 테스트 데이터 시딩
k6 run k6/scripts/mixed-load.js   # 혼합 부하 테스트
```

최신 결과는 [부하 테스트 결과 보고서](../test/k6-load-test-report.md) 참조.

---

## 6. 테스트 네이밍 규칙

- 한글 메서드명: `재고_1개에_동시_예약시_1명만_성공()`
- Given-When-Then 구조
- `@DisplayName`으로 시나리오 설명

| 접미사 | 의미 | 환경 |
|--------|------|------|
| `Test` | 단위 테스트 | JVM (Mockito) |
| `IntegrationTest` | 통합 테스트 | Testcontainers + MySQL |
| `ConcurrencyTest` | 동시성 테스트 | Testcontainers + MySQL |
