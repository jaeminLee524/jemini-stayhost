# ADR-005: ApplicationEvent 기반 캐시 무효화

## 상태

Accepted (2026-03-28)

---

## 배경

Extranet(파트너 관리 화면)에서 파트너가 숙소 정보나 요금을 변경하면, 검색 캐시(ADR-003)가 구버전 데이터를 반환하는 정합성 문제가 발생한다.

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

### Option A: TTL만 의존

별도 무효화 로직 없이 캐시 TTL(5분)이 만료될 때까지 기다리는 방식이다.

- 장점: 구현이 전혀 없다. 코드 복잡도 제로.
- 단점:
  - 요금 변경 후 최대 5분간 잘못된 가격이 검색 결과에 노출된다
  - 할인 행사나 긴급 요금 조정 시 파트너와 고객 모두에게 혼란을 준다
  - 데이터 정합성을 시간으로 희생하는 방식이다
- 판정: 기능적으로는 동작하지만 허용 불가한 정합성 수준. 불채택. TTL은 무효화 이벤트가 누락될 때의 안전망으로만 유지.

### Option B: 수동 캐시 제거 (서비스 계층에서 직접 호출)

데이터 변경 서비스에서 DB 업데이트 직후 `cacheManager.evict()` 를 직접 호출하는 방식이다.

```java
ratePolicyRepository.save(updatedPolicy);
cacheManager.evict("ratePolicy", key);  // 트랜잭션 내부에서 직접 호출
```

- 장점: 코드가 직관적이다. 이벤트 메커니즘 없이 한 곳에서 처리된다.
- 단점:
  - 트랜잭션 롤백 시 캐시는 이미 제거된 상태가 된다
  - DB에는 롤백 전 값이 남아 있는데 캐시는 비워져 있으므로, 다음 조회 시 DB에서 구버전 데이터를 다시 읽어 캐시에 저장한다
  - 더 중요한 것은 트랜잭션 내에서 캐시를 건드리는 것이 관심사 혼합이라는 점이다
  - 서비스 계층이 캐시 인프라에 직접 의존하게 된다
- 판정: 롤백 안전성 부재 및 관심사 혼합 문제로 불채택.

---

## 결과

### 긍정적 결과

- 트랜잭션 커밋 이후에만 캐시 무효화가 실행되므로, 롤백 시 불필요한 캐시 제거가 발생하지 않는다
- 서비스 계층은 `applicationEventPublisher.publishEvent()` 만 호출하면 되며, 캐시 인프라에 직접 의존하지 않는다
  - 캐시 전략 변경이 서비스 코드에 영향을 주지 않는다
- 이벤트 기반 구조이므로 향후 캐시 외에 다른 사이드 이펙트(예: 채널 매니저 동기화 알림)를 동일한 이벤트로 처리할 수 있다

### 부정적 결과 및 관리 방안

- 이벤트 누락 시 TTL까지 stale 데이터 유지: 리스너에서 예외가 발생하거나 이벤트 발행을 코드에서 빠뜨리면 캐시가 무효화되지 않는다.
  - 관리 방안:
    - TTL(5분)을 안전망으로 유지한다
    - 이벤트 누락이 있어도 최대 5분 후에는 캐시가 자동 갱신된다
    - 리스너에 예외 처리와 로깅을 추가하여 누락을 감지한다
- 동기 이벤트의 성능 영향: `@TransactionalEventListener(AFTER_COMMIT)`은 기본적으로 동기 실행된다. 캐시 무효화 자체는 빠르지만, 이벤트 처리가 느려지면 응답이 지연될 수 있다.
  - 관리 방안:
    - 캐시 무효화는 매우 빠른 작업(로컬 메모리 접근)이므로 현재는 동기 처리로 충분하다
    - Redis 전환 시에는 `@Async`와 함께 비동기 처리로 변경한다
- 멀티 인스턴스 시 캐시 불일치: ApplicationEvent는 동일 JVM 내에서만 전파된다. 인스턴스 A에서 이벤트가 발행되어 A의 캐시는 무효화되지만, 인스턴스 B의 캐시는 그대로다.
  - 관리 방안:
    - 단일 인스턴스 환경에서는 이 문제가 없다
    - 멀티 인스턴스 운영 시 Redis Pub/Sub으로 전환하여 모든 인스턴스에 이벤트를 브로드캐스트한다

---

## 구현 참고

`@TransactionalEventListener`의 기본 phase는 `AFTER_COMMIT`이지만 명시적으로 선언하는 것이 의도를 명확히 한다.

```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void onRatePolicyChanged(RatePolicyChangedEvent event) {
    event.getAffectedDates().forEach(date ->
        cacheManager.evict("ratePolicy",
            event.getRoomTypeId() + ":" + date)
    );
}
```

트랜잭션 밖에서도 이벤트가 실행될 수 있도록 `fallbackExecution = true` 설정을 고려할 수 있으나, 이 경우 롤백 안전성 보장이 무의미해지므로 명시적으로 `false`로 유지한다.

---

## 후속 조치

- [ ] 멀티 인스턴스 환경 대응: ApplicationEvent → Redis Pub/Sub 전환 설계
  - `RedisMessageListenerContainer` + `MessageListenerAdapter` 구성
  - 이벤트 페이로드 직렬화 전략 (JSON) 결정
- [ ] 이벤트 누락 감지를 위한 캐시 히트율 모니터링 (Caffeine `recordStats()` + Actuator 노출)
- [ ] 리스너 예외 발생 시 알림 구성 (Slack 또는 로그 알람)
- [ ] 대량 요금 변경(일괄 업데이트) 시 이벤트 배치 처리 고려 (이벤트 폭풍 방지)
