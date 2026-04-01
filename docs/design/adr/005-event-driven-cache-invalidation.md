# ADR-005: ApplicationEvent 기반 캐시 무효화

## 상태

Accepted (2026-03-28)

---

## 배경

Extranet(파트너 관리 화면)에서 파트너가 숙소 정보나 요금을 변경하면, 검색 캐시(ADR-003)가 구버전 데이터를 반환하는 정합성 문제가 발생한다.
- (검색 캐시: [003-local-cache-strategy.md](./003-local-cache-strategy.md))

예를 들어 파트너가 3월 30일 요금을 150,000원에서 120,000원으로 변경했는데, 캐시에 여전히 150,000원이 남아 있으면 고객이 잘못된 가격으로 검색 결과를 보게 된다. TTL(5분)이 만료될 때까지 이 상태가 지속된다.

캐시 무효화 전략을 결정할 때 고려해야 할 제약이 있다.

- 캐시 무효화는 데이터 변경 이후에 실행되어야 한다
- 트랜잭션이 롤백된 경우에는 캐시를 무효화해서는 안 된다
- 트랜잭션 중간에 캐시를 지우고 이후 롤백되면, 이미 캐시가 지워진 상태에서 DB에는 변경 이전 값이 남게 되어 불필요한 캐시 미스가 발생한다

---

## 결정

Spring `ApplicationEvent` + `@TransactionalEventListener(phase = AFTER_COMMIT)` 조합을 채택한다.

데이터 변경 서비스에서 도메인 이벤트를 발행하고, 트랜잭션이 커밋된 이후에만 캐시 무효화 리스너가 실행되도록 한다.

### 구현 흐름

```
[Extranet API — 요금 변경 요청]
       ↓
[RatePolicyService.update() — 트랜잭션 시작]
       ↓
[DB UPDATE rate_policy ...]
       ↓
[applicationEventPublisher.publishEvent(RatePolicyChangedEvent)]
       ↓
[트랜잭션 커밋]
       ↓ (커밋 성공 후에만 리스너 실행)
[@TransactionalEventListener(AFTER_COMMIT)]
[CacheInvalidationListener.onRatePolicyChanged()]
       ↓
[cacheManager.evict("ratePolicy", key)]
```

트랜잭션이 롤백되면 `AFTER_COMMIT` 리스너는 실행되지 않으므로, 캐시 무효화도 발생하지 않는다.

### 이벤트 유형

```java
// 요금 변경 시
RatePolicyChangedEvent(roomTypeId, affectedDates)

// 숙소 정보 변경 시
PropertyUpdatedEvent(propertyId)

// 객실 유형 변경 시
RoomTypeUpdatedEvent(roomTypeId)
```

---

## 대안

- Option A: TTL만 의존
  - 장점: 구현이 전혀 없다. 코드 복잡도 제로.
  - 단점:
    - 요금 변경 후 최대 5분간 잘못된 가격이 검색 결과에 노출된다.
    - 할인 행사나 긴급 요금 조정 시 파트너와 고객 모두에게 혼란을 준다.
    - 데이터 정합성을 시간으로 희생하는 방식이다.

## 긍정적 결과

- 트랜잭션 커밋 이후에만 캐시 무효화가 실행되므로, 롤백 시 불필요한 캐시 제거가 발생하지 않는다
- 서비스 계층은 `applicationEventPublisher.publishEvent()` 만 호출하면 되며, 캐시 인프라에 직접 의존하지 않는다
  - 캐시 전략 변경이 서비스 코드에 영향을 주지 않는다
- 이벤트 기반 구조이므로 향후 캐시 외에 다른 사이드 이펙트(예: 채널 매니저 동기화 알림)를 동일한 이벤트로 처리할 수 있다

---
