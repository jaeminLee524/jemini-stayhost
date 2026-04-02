# 07. 주요 시퀀스 다이어그램

> 이 문서는 OTA 플랫폼의 핵심 플로우 7개를 Mermaid 시퀀스 다이어그램으로 정리한다.
> 각 다이어그램 아래에 설계 판단 근거와 주의사항을 함께 기술한다.

---

## 목차

1. [파트너 숙소 등록 플로우](#1-파트너-숙소-등록-플로우)
2. [고객 숙소 검색 플로우](#2-고객-숙소-검색-플로우)
3. [예약 생성 플로우 (핵심)](#3-예약-생성-플로우-핵심)
4. [예약 취소 플로우](#4-예약-취소-플로우)
5. [채널 매니저 OUTBOUND 플로우](#5-채널-매니저-outbound-플로우)
6. [채널 매니저 INBOUND 플로우](#6-채널-매니저-inbound-플로우)
7. [Supplier 배치 동기화 플로우](#7-supplier-배치-동기화-플로우)

---

## 1. 파트너 숙소 등록 플로우

파트너가 Extranet을 통해 숙소를 등록하는 전체 흐름이다. 숙소 등록 후 이벤트를 발행하여 관련 캐시를 무효화한다.

```mermaid
sequenceDiagram
    actor Partner as 파트너 (Extranet)
    participant ExtranetAPI as Extranet API
    participant AuthFilter as JWT 인증 필터
    participant PropertyService as PropertyService
    participant DB as MySQL
    participant EventPublisher as ApplicationEventPublisher
    participant CacheEvict as PropertyCacheEvictListener

    Partner->>ExtranetAPI: POST /api/extranet/properties\n{ name, type, address, region, ... }
    ExtranetAPI->>AuthFilter: JWT 토큰 검증
    AuthFilter-->>ExtranetAPI: Partner 인증 완료 (partnerId 추출)

    Note over ExtranetAPI: 요청 본문 유효성 검사 (@Valid)

    ExtranetAPI->>PropertyService: createProperty(partnerId, request)

    Note over PropertyService: @Transactional 시작
    PropertyService->>DB: SELECT partner WHERE id=partnerId AND status='ACTIVE'
    DB-->>PropertyService: Partner (활성 파트너 확인)

    alt 파트너가 ACTIVE가 아닌 경우
        PropertyService-->>ExtranetAPI: PartnerNotActiveException
        ExtranetAPI-->>Partner: 403 Forbidden
    end

    PropertyService->>DB: INSERT property\n(partner_id, name, type, address, region, status='DRAFT')
    DB-->>PropertyService: propertyId (생성된 ID)

    PropertyService->>DB: COMMIT
    PropertyService->>EventPublisher: publish(PropertyCreatedEvent(propertyId, partnerId))

    Note over EventPublisher: @TransactionalEventListener(AFTER_COMMIT)<br/>DB 커밋 후에만 이벤트 처리

    EventPublisher->>CacheEvict: onPropertyCreated(event)
    Note over CacheEvict: 신규 등록이므로 무효화할 캐시 없음<br/>목록 캐시가 있다면 이 시점에 무효화

    PropertyService-->>ExtranetAPI: PropertyResponse { id, name, status='DRAFT' }
    ExtranetAPI-->>Partner: 201 Created\n{ propertyId, name, status: "DRAFT" }

    Note over Partner: 이후 파트너는 객실 유형, 요금, 재고를 추가로 등록한다\n숙소 상태를 ACTIVE로 변경해야 고객에게 노출된다
```

설계 포인트:
- 숙소는 등록 시 `DRAFT` 상태로 생성된다. 파트너가 객실/요금/재고까지 설정하고 `ACTIVE`로 상태를 변경해야 검색에 노출된다.
- JWT 인증 필터에서 `partnerId`를 추출하여 서비스 레이어에 전달한다. 파트너는 자신의 숙소만 등록/수정할 수 있다.
- `PropertyCreatedEvent`는 향후 채널 매니저 초기 배포나 알림에 활용할 수 있다.

---

## 2. 고객 숙소 검색 플로우

고객이 지역, 날짜, 인원으로 숙소를 검색하는 흐름이다. 검색 쿼리는 매번 DB를 호출하되, 개별 숙소/객실 데이터는 Caffeine 캐시에서 제공한다.

```mermaid
sequenceDiagram
    actor Customer as 고객
    participant SearchAPI as Search API
    participant SearchService as SearchService
    participant Cache as Caffeine Cache
    participant DB as MySQL (커버링 인덱스)

    Customer->>SearchAPI: GET /api/public/search/properties\n?region=서울&checkIn=2026-04-01&checkOut=2026-04-03&guestCount=2

    SearchAPI->>SearchService: searchProperties(region, checkIn, checkOut, guestCount, page)

    Note over SearchService: 1단계: 커버링 인덱스로 숙소 ID 목록만 빠르게 추출
    SearchService->>DB: SELECT p.id FROM property p\nWHERE p.region=? AND p.status='ACTIVE'\nORDER BY p.id\nLIMIT ? OFFSET ?
    Note over DB: 인덱스: idx_property_region_status(region, status)<br/>커버링 인덱스 → 테이블 풀스캔 없이 ID 목록만 추출

    DB-->>SearchService: [propertyId: 1, 5, 12, 23, ...]

    Note over SearchService: 2단계: 각 숙소 상세 정보 캐시에서 조회
    loop 각 propertyId에 대해
        SearchService->>Cache: get("property:{id}")
        alt Cache Hit
            Cache-->>SearchService: PropertyDetail (캐시 히트)
        else Cache Miss
            SearchService->>DB: SELECT * FROM property WHERE id=?
            SearchService->>DB: SELECT * FROM room_type WHERE property_id=?
            DB-->>SearchService: PropertyDetail
            SearchService->>Cache: put("property:{id}", PropertyDetail, TTL=10분)
            SearchService->>Cache: put("roomTypes:{propertyId}", RoomTypes, TTL=10분)
        end
    end

    Note over SearchService: 3단계: 날짜별 요금 조회 (검색 조건에 해당하는 날짜)
    loop 각 숙소의 각 객실 유형에 대해
        loop 각 날짜에 대해
            SearchService->>Cache: get("rate:{roomTypeId}:{date}")
            alt Cache Hit
                Cache-->>SearchService: Rate (캐시 히트)
            else Cache Miss
                SearchService->>DB: SELECT price FROM rate\nWHERE room_type_id=? AND date=?
                DB-->>SearchService: Rate
                SearchService->>Cache: put("rate:{roomTypeId}:{date}", Rate, TTL=3분)
            end
        end
    end

    Note over SearchService: 4단계: 재고 조회 (캐시 없음 — 항상 실시간)
    SearchService->>DB: SELECT available_count FROM inventory\nWHERE room_type_id IN (...)\nAND date BETWEEN ? AND ?
    Note over DB: 재고는 캐시하지 않는다\n예약 가능 여부는 항상 최신 데이터여야 하기 때문

    DB-->>SearchService: 날짜별 재고 목록

    SearchService-->>SearchAPI: SearchResult { properties: [...], totalCount, page }
    SearchAPI-->>Customer: 200 OK\n{ properties: [숙소 목록, 요금 포함], totalCount, page }
```

설계 포인트:
- `propertySearch` (검색 결과 전체) 캐시를 사용하지 않는 이유: 지역 x 날짜 x 인원 x 페이지 조합이 폭발적으로 늘어나 캐시 히트율이 매우 낮다. 대신 하위 단위(property, roomTypes, rate)로 분해하면 동일 숙소가 다양한 검색에서 재사용되어 히트율이 높아진다.
- 재고만 캐시에서 제외한다. 재고는 예약 발생 시 즉시 변하므로 stale 데이터를 보여주면 고객에게 잘못된 예약 가능 여부를 안내하게 된다.

---

## 3. 예약 생성 플로우 (핵심)

예약 생성은 Caffeine CAS 1차 필터링 → DB 비관적 락 2차 확정의 2단계로 처리된다. Caffeine CAS에서 매진 요청을 JVM 레벨에서 즉시 걸러내어 DB 부하를 최소화하고, 통과한 요청만 DB 비관적 락으로 최종 확정한다.

```mermaid
sequenceDiagram
    actor Customer as 고객
    participant ReservationAPI as Reservation API
    participant AuthFilter as JWT 인증 필터
    participant Facade as ReservationFacade
    participant InvCache as InventoryCache (Caffeine)
    participant Service as ReservationService
    participant DB as MySQL
    participant EventPublisher as ApplicationEventPublisher
    participant ChannelManager as ChannelManagerService (비동기)

    Customer->>ReservationAPI: POST /api/reservations\n{ propertyId, roomTypeId, checkIn, checkOut, guestCount, ... }

    ReservationAPI->>AuthFilter: JWT 토큰 검증
    AuthFilter-->>ReservationAPI: User 인증 완료 (userId 추출)

    ReservationAPI->>Facade: create(userId, request)

    Note over Facade: Facade — @Transactional 없음<br/>읽기/검증/위임만 담당

    Facade->>DB: SELECT * FROM room_type WHERE id=? AND status='ACTIVE'
    DB-->>Facade: RoomType 정보 (최대 인원, 요금 확인)

    alt guestCount > maxOccupancy
        Facade-->>ReservationAPI: InvalidGuestCountException
        ReservationAPI-->>Customer: 400 Bad Request (인원 초과)
    end

    Note over Facade: 1단계: Caffeine CAS 1차 필터링<br/>AtomicInteger CAS로 빠른 매진 판단 (DB 접근 없음)
    loop 체크인 ~ 체크아웃-1 각 날짜
        Facade->>InvCache: tryDecrease(roomTypeId, date)
        InvCache-->>Facade: true/false (CAS 결과)
    end

    alt 하나라도 CAS 실패 (매진)
        Facade->>InvCache: rollback(성공한 날짜들 복원)
        Facade-->>ReservationAPI: InsufficientInventoryException
        ReservationAPI-->>Customer: 409 Conflict\n{ message: "선택하신 날짜에 재고가 없습니다" }
        Note over Customer: DB에 도달하지 않고 즉시 응답<br/>100 동시 요청 시 99건이 여기서 걸러짐
    end

    Note over Facade: CAS 통과 → 2단계: DB 비관적 락으로 최종 확정

    Facade->>Service: createWithInventoryLock(command)
    Note over Service: @Transactional 시작 — 원자적 쓰기

    Service->>DB: SELECT * FROM room_type WHERE id=? AND status='ACTIVE'
    Note over Service: 방어적 재검증 — Facade 검증 이후<br/>room_type이 비활성화됐을 수 있음 (TOCTOU 방지)

    alt room_type이 INACTIVE로 변경된 경우
        Service-->>Facade: RoomTypeInactiveException
        Facade->>InvCache: rollback(CAS 복원)
        Facade-->>ReservationAPI: 409 Conflict
    end

    Service->>DB: SELECT * FROM inventory\nWHERE room_type_id = ?\nAND date BETWEEN checkIn AND checkOut-1\nORDER BY room_type_id, date\nFOR UPDATE
    Note over DB: ORDER BY로 잠금 순서 고정 → 데드락 방지

    DB-->>Service: inventory 행 목록 (잠금 획득)

    alt DB에서도 재고 부족 확인 시
        Service->>DB: ROLLBACK
        Service-->>Facade: InsufficientInventoryException
        Facade->>InvCache: rollback(CAS 복원)
        Facade-->>ReservationAPI: 409 Conflict
    end

    Note over Service: 재고 차감 + 예약 생성 (동일 트랜잭션)
    Service->>DB: UPDATE inventory SET reserved_count = reserved_count + 1

    Service->>DB: SELECT price FROM rate WHERE room_type_id=? AND date BETWEEN ? AND ?
    DB-->>Service: 날짜별 요금 목록

    Service->>DB: INSERT reservation (status='CONFIRMED', confirmed_at=NOW())
    DB-->>Service: reservationId

    Service->>DB: INSERT reservation_daily_rate (날짜별 요금 스냅샷)

    Service->>DB: COMMIT
    Note over DB: 커밋 시점에 비관적 락 해제

    Service->>EventPublisher: publish(ReservationCreatedEvent)
    Service->>EventPublisher: publish(InventoryChangedEvent)

    Note over EventPublisher: @TransactionalEventListener(AFTER_COMMIT)

    EventPublisher->>ChannelManager: onInventoryChanged(event) [비동기]
    Note over ChannelManager: 매핑된 채널에 재고 변경 OUTBOUND 푸시

    Service-->>Facade: ReservationResult
    Facade-->>ReservationAPI: ReservationResponse\n{ reservationNumber, status='CONFIRMED', finalPrice }
    ReservationAPI-->>Customer: 201 Created
```

설계 포인트:
- Caffeine CAS 1차 필터링: `AtomicInteger`의 CAS 연산으로 DB 접근 없이 JVM 레벨에서 매진을 즉시 판단한다. 100 동시 요청 시 99건이 여기서 걸러져 DB에는 1건만 도달한다.
- DB 비관적 락 2차 확정: CAS를 통과한 요청만 `SELECT FOR UPDATE`로 최종 정합성을 보장한다. Caffeine은 pre-filter, DB가 source of truth다.
- Facade 트랜잭션 분리: Facade는 `@Transactional` 없이 읽기/검증/CAS를 처리하고, Service에만 `@Transactional`을 걸어 비관적 락 점유 시간을 최소화한다.
- DB 실패 시 Caffeine 롤백: DB 트랜잭션이 실패하면 Caffeine의 CAS 차감분을 `incrementAndGet()`으로 복원한다.
- ORDER BY 데드락 방지: 멀티 나이트 예약 시 `ORDER BY room_type_id, date`로 잠금 순서를 고정한다.

---

## 4. 예약 취소 플로우

고객이 예약을 취소하면 재고를 복원하고, 채널 매니저를 통해 외부 채널의 재고도 동기화한다.

```mermaid
sequenceDiagram
    actor Customer as 고객
    participant ReservationAPI as Reservation API
    participant AuthFilter as JWT 인증 필터
    participant ReservationService as ReservationService
    participant DB as MySQL
    participant EventPublisher as ApplicationEventPublisher
    participant ChannelManager as ChannelManagerService (비동기)

    Customer->>ReservationAPI: POST /api/reservations/{id}/cancel\n{ cancelReason }

    ReservationAPI->>AuthFilter: JWT 토큰 검증
    AuthFilter-->>ReservationAPI: User 인증 완료

    ReservationAPI->>ReservationService: cancelReservation(userId, reservationId, reason)

    Note over ReservationService: @Transactional 시작
    ReservationService->>DB: SELECT * FROM reservation WHERE id=? AND user_id=?
    DB-->>ReservationService: Reservation

    alt 예약이 존재하지 않거나 본인 예약이 아닌 경우
        ReservationService-->>ReservationAPI: ReservationNotFoundException / AccessDeniedException
        ReservationAPI-->>Customer: 404 / 403
    end

    Note over ReservationService: 취소 가능 여부 확인 (체크인 날짜 기준)

    ReservationService->>DB: UPDATE reservation\nSET status='CANCELLED',\n    cancelled_at=NOW(),\n    cancel_reason=?\nWHERE id=? AND status='CONFIRMED'
    Note over DB: WHERE status='CONFIRMED' 조건으로<br/>동시 취소 요청 시 중복 취소 방지 (affected rows=0이면 이미 취소됨)
    DB-->>ReservationService: affected rows

    alt affected rows = 0 (이미 취소됨)
        ReservationService-->>ReservationAPI: AlreadyCancelledException
        ReservationAPI-->>Customer: 409 Conflict
    end

    Note over ReservationService: 재고 복원 (차감의 역연산)
    ReservationService->>DB: UPDATE inventory\nSET reserved_count = reserved_count - 1\nWHERE room_type_id=?\nAND date BETWEEN checkIn AND checkOut-1

    ReservationService->>DB: COMMIT

    ReservationService->>EventPublisher: publish(ReservationCancelledEvent)
    ReservationService->>EventPublisher: publish(InventoryChangedEvent)

    Note over EventPublisher: @TransactionalEventListener(AFTER_COMMIT)

    EventPublisher->>ChannelManager: onInventoryChanged(event) [비동기]
    Note over ChannelManager: 재고가 복원되었으므로\n매핑된 모든 채널에 재고 증가 OUTBOUND 푸시

    ReservationService-->>ReservationAPI: CancelResponse { reservationId, status='CANCELLED' }
    ReservationAPI-->>Customer: 200 OK\n{ status: "CANCELLED", cancelledAt }
```

설계 포인트:
- 취소 시 재고 복원도 트랜잭션 내에서 처리한다. 예약 상태 변경과 재고 복원이 원자적으로 이루어진다.
- 채널 매니저 동기화는 `AFTER_COMMIT` 이후 비동기로 처리한다. 취소 응답 속도에 영향을 주지 않는다.
- 재고 복원 후 `InventoryChangedEvent`를 발행하면 채널 매니저가 모든 외부 채널에 재고 증가를 알린다. 이를 통해 다른 채널에서도 다시 예약이 가능해진다.

---

## 5. 채널 매니저 OUTBOUND 플로우

자사 재고/요금 변경 시 매핑된 모든 외부 채널에 동기화하는 흐름이다.

```mermaid
sequenceDiagram
    participant EventPublisher as ApplicationEventPublisher
    participant ChannelManager as ChannelManagerService
    participant MappingRepo as channel_property_mapping (DB)
    participant RatePolicy as channel_rate_policy (DB)
    participant AdapterRegistry as ChannelAdapterRegistry
    participant ChannelAdapterA as BookingComAdapter (Mock)
    participant ChannelAdapterB as ExpediaAdapter (Mock)
    participant ChannelAdapterC as AgodaAdapter (Mock) — 실패 시나리오
    participant SyncLog as channel_sync_log (DB)
    participant RetryQueue as RetryQueue (스케줄러)

    EventPublisher->>ChannelManager: onInventoryChanged(InventoryChangedEvent)
    Note over ChannelManager: @TransactionalEventListener(AFTER_COMMIT)<br/>DB 커밋이 완료된 후에만 실행

    ChannelManager->>MappingRepo: SELECT * FROM channel_property_mapping\nWHERE property_id=? AND status='ACTIVE'
    MappingRepo-->>ChannelManager: [mappingA(Booking), mappingB(Expedia), mappingC(Agoda)]

    Note over ChannelManager: 채널별 markup 적용 후 요금 계산
    ChannelManager->>RatePolicy: SELECT * FROM channel_rate_policy\nWHERE channel_property_mapping_id IN (...)
    RatePolicy-->>ChannelManager: 채널별 요금 정책 (PERCENTAGE/FIXED)

    par 병렬 푸시 (각 채널에 동시 전송)
        ChannelManager->>AdapterRegistry: getAdapter("BOOKING")
        AdapterRegistry-->>ChannelManager: BookingComAdapter
        ChannelManager->>ChannelAdapterA: pushInventory(mappingA, updates)
        ChannelAdapterA-->>ChannelManager: ChannelSyncResult(success=true)
        ChannelManager->>SyncLog: INSERT (channelA, INVENTORY, OUTBOUND, SUCCESS)
    and
        ChannelManager->>AdapterRegistry: getAdapter("EXPEDIA")
        AdapterRegistry-->>ChannelManager: ExpediaAdapter
        ChannelManager->>ChannelAdapterB: pushInventory(mappingB, updates)
        ChannelAdapterB-->>ChannelManager: ChannelSyncResult(success=true)
        ChannelManager->>SyncLog: INSERT (channelB, INVENTORY, OUTBOUND, SUCCESS)
    and
        ChannelManager->>AdapterRegistry: getAdapter("AGODA")
        AdapterRegistry-->>ChannelManager: AgodaAdapter
        ChannelManager->>ChannelAdapterC: pushInventory(mappingC, updates)
        Note over ChannelAdapterC: 타임아웃 30초 초과
        ChannelAdapterC-->>ChannelManager: ChannelSyncResult(success=false, "Connection timeout")
        ChannelManager->>SyncLog: INSERT (channelC, INVENTORY, OUTBOUND, FAILED, retryCount=0)
        ChannelManager->>RetryQueue: enqueue(channelC, mappingC, updates, delay=1분)
    end

    Note over ChannelManager: 2개 성공 + 1개 실패 → 전체 상태: PARTIAL
    ChannelManager->>SyncLog: UPDATE summary (status=PARTIAL)

    Note over RetryQueue: 1분 후 1차 재시도
    RetryQueue->>ChannelAdapterC: pushInventory(mappingC, updates)
    alt 재시도 성공
        ChannelAdapterC-->>RetryQueue: success=true
        RetryQueue->>SyncLog: UPDATE (channelC, SUCCESS, retryCount=1)
    else 3회 모두 실패
        RetryQueue->>SyncLog: UPDATE (channelC, FAILED, retryCount=3)
        RetryQueue->>RetryQueue: 운영자 알림 발송
    end
```

설계 포인트:
- 채널별 어댑터를 `ChannelAdapterRegistry`(Map 기반)에서 코드로 조회하여 의존성을 느슨하게 유지한다.
- 채널별 markup을 `channel_rate_policy`에서 조회하여 적용한 후 어댑터에 전달한다. 어댑터는 markup 계산을 모른다.
- 모든 채널 푸시는 병렬로 실행한다. 한 채널의 실패가 다른 채널을 블로킹하지 않는다.

---

## 6. 채널 매니저 INBOUND 플로우

외부 OTA에서 예약이 발생하여 웹훅으로 수신하는 흐름이다. 자사 예약을 생성하고, 나머지 채널에 재고를 연쇄 동기화한다.

```mermaid
sequenceDiagram
    participant OTA as 외부 OTA (Booking.com)
    participant WebhookController as ChannelWebhookController
    participant ChannelAdapter as BookingComAdapter
    participant ChannelManager as ChannelManagerService
    participant ReservationService as ReservationService
    participant DB as MySQL (비관적 락)
    participant EventPublisher as ApplicationEventPublisher
    participant OtherAdapters as 다른 채널 어댑터들 (Expedia, Agoda)
    participant SyncLog as channel_sync_log

    OTA->>WebhookController: POST /api/channels/{channelId}/webhook/reservation\n{ 외부 OTA 예약 payload }

    Note over WebhookController: HMAC 서명 검증\n(위조 요청 차단)

    WebhookController->>ChannelAdapter: convertReservation(externalPayload)
    Note over ChannelAdapter: 외부 OTA 형식 → 자사 InboundReservationRequest 변환<br/>필드명 매핑, 날짜 형식 변환, 금액 단위 처리

    alt 변환 실패
        ChannelAdapter-->>WebhookController: ChannelDataConversionException
        WebhookController->>SyncLog: INSERT (INBOUND, FAILED, request_payload=원본 보존)
        WebhookController-->>OTA: 422 Unprocessable Entity
        Note over WebhookController: 원본 payload를 sync_log에 보존\n운영자가 수동 확인 후 재처리 가능
    end

    ChannelAdapter-->>ChannelManager: InboundReservationRequest

    ChannelManager->>ReservationService: createReservation(request, source="CHANNEL:BOOKING")

    Note over ReservationService: @Transactional 시작
    ReservationService->>DB: SELECT * FROM inventory\nWHERE room_type_id=? AND date BETWEEN ? AND ?\nORDER BY room_type_id, date FOR UPDATE
    Note over DB: 비관적 락 — 외부 OTA 예약도\n자사 직접 예약과 동일한 동시성 제어 적용

    alt 재고 부족
        DB-->>ReservationService: reserved_count >= total_count
        ReservationService->>DB: ROLLBACK
        ReservationService-->>ChannelManager: InsufficientInventoryException
        ChannelManager->>SyncLog: INSERT (INBOUND, FAILED, "재고 부족")
        ChannelManager-->>WebhookController: 재고 부족 응답
        WebhookController-->>OTA: 409 Conflict
        Note over OTA: OTA는 재고 부족 응답을 받으면\n자체 예약을 취소 처리해야 함
    else 재고 충분
        ReservationService->>DB: UPDATE inventory SET reserved_count = reserved_count + 1
        ReservationService->>DB: INSERT reservation\n(status='CONFIRMED', source='CHANNEL:BOOKING')
        ReservationService->>DB: COMMIT

        ReservationService->>EventPublisher: publish(ReservationCreatedEvent)
        ReservationService->>EventPublisher: publish(InventoryChangedEvent)

        Note over EventPublisher: @TransactionalEventListener(AFTER_COMMIT)
        EventPublisher->>ChannelManager: onInventoryChanged(event)

        Note over ChannelManager: 예약 수신 채널(Booking.com)을 제외한\n나머지 채널에 재고 동기화 (연쇄 동기화)
        par 연쇄 동기화
            ChannelManager->>OtherAdapters: pushInventory(Expedia, 감소된 재고)
            ChannelManager->>OtherAdapters: pushInventory(Agoda, 감소된 재고)
        end

        ChannelManager->>SyncLog: INSERT (INBOUND, SUCCESS, direction=INBOUND)
        ChannelManager->>SyncLog: INSERT (OUTBOUND, 연쇄 동기화 결과)

        ChannelManager-->>WebhookController: 예약 생성 완료
        WebhookController-->>OTA: 200 OK\n{ reservationNumber }
    end
```

설계 포인트:
- 외부 OTA 예약도 자사 직접 예약과 완전히 동일한 비관적 락 + 재고 차감 로직을 거친다. 단, `source` 필드에 `CHANNEL:BOOKING` 등 채널 정보를 기록한다.
- 예약 수신 채널(Booking.com)은 연쇄 동기화에서 제외한다. 이미 Booking.com에서 예약이 완료되었으므로 다시 재고를 보낼 필요가 없다.
- 연쇄 동기화 실패는 재시도 큐로 처리한다. 자사 예약은 유지된다.

---

## 7. Supplier 배치 동기화 플로우

외부 공급사의 숙소/요금/재고를 주기적으로 가져와 자사 데이터에 동기화하는 배치 흐름이다.

```mermaid
sequenceDiagram
    participant Scheduler as @Scheduled (매 1시간)
    participant SupplierSyncService as SupplierSyncService
    participant SyncJobRepo as supplier_sync_job (DB)
    participant AdapterRegistry as SupplierAdapterRegistry
    participant SupplierAdapter as MockSupplierAdapter
    participant SupplierPropRepo as supplier_property (DB)
    participant MappingRepo as supplier_property_mapping (DB)
    participant PropertyService as PropertyService
    participant PropertyDB as property / rate / inventory (DB)
    participant EventPublisher as ApplicationEventPublisher
    participant Cache as Caffeine Cache

    Scheduler->>SupplierSyncService: syncAll()
    Note over SupplierSyncService: ACTIVE 상태인 모든 Supplier를 조회

    loop 각 Supplier에 대해
        SupplierSyncService->>SyncJobRepo: INSERT supplier_sync_job\n(status=RUNNING, job_type='FULL_SYNC', started_at=NOW())

        SupplierSyncService->>AdapterRegistry: getAdapter(supplier.code)
        AdapterRegistry-->>SupplierSyncService: SupplierAdapter

        SupplierSyncService->>SupplierAdapter: fetchProperties()
        Note over SupplierAdapter: Mock: 가상 숙소 3건 반환\n실제 구현 시 공급사 REST API 호출

        alt API 호출 실패
            SupplierAdapter-->>SupplierSyncService: SupplierApiException
            SupplierSyncService->>SyncJobRepo: UPDATE (status=FAILED, error_message=?)
            Note over SupplierSyncService: 이 Supplier 건너뜀\n다음 스케줄에서 자동 재시도
        else API 호출 성공
            SupplierAdapter-->>SupplierSyncService: List<SupplierPropertyData>

            loop 각 공급사 숙소에 대해
                Note over SupplierSyncService: 원본 데이터를 raw_data(JSON)에 그대로 저장\n각 공급사마다 응답 구조가 다르기 때문
                SupplierSyncService->>SupplierPropRepo: INSERT OR UPDATE supplier_property\n(raw_data=원본JSON, last_synced_at=NOW())

                SupplierSyncService->>MappingRepo: SELECT * FROM supplier_property_mapping\nWHERE supplier_property_id=?

                alt 매핑 없음 (신규 공급사 숙소)
                    MappingRepo-->>SupplierSyncService: null
                    SupplierSyncService->>MappingRepo: INSERT supplier_property_mapping\n(mapping_status='UNMAPPED')
                    Note over SupplierSyncService: 자동 매핑 안 함\n관리자가 Admin에서 수동으로 자사 숙소와 연결해야 함

                else 매핑 있음 (MAPPED)
                    MappingRepo-->>SupplierSyncService: mapping { property_id }

                    SupplierSyncService->>SupplierAdapter: fetchRates(externalPropertyId, today, today+30일)
                    SupplierAdapter-->>SupplierSyncService: List<SupplierRateData>

                    SupplierSyncService->>SupplierAdapter: fetchInventory(externalPropertyId, today, today+30일)
                    SupplierAdapter-->>SupplierSyncService: List<SupplierInventoryData>

                    SupplierSyncService->>PropertyService: updateFromSupplier(propertyId, rateData, inventoryData)

                    Note over PropertyService: @Transactional
                    PropertyService->>PropertyDB: UPDATE rate (날짜별 요금 갱신)
                    PropertyService->>PropertyDB: UPDATE inventory (날짜별 재고 갱신)
                    PropertyDB-->>PropertyService: 업데이트 완료

                    PropertyService->>EventPublisher: publish(RateUpdatedEvent)
                    Note over EventPublisher: @TransactionalEventListener(AFTER_COMMIT)
                    EventPublisher->>Cache: evict("rate:{roomTypeId}:{date}") [해당 날짜 범위]
                    Note over Cache: 캐시 무효화 → 다음 조회 시 DB에서 최신 요금 로딩

                else 매핑 CONFLICT
                    MappingRepo-->>SupplierSyncService: mapping { mapping_status='CONFLICT' }
                    Note over SupplierSyncService: 충돌 상태는 자동 처리 안 함\n건너뜀 + fail_count 증가
                end
            end

            SupplierSyncService->>SyncJobRepo: UPDATE supplier_sync_job\n(status=COMPLETED,\n total_count=N,\n success_count=M,\n fail_count=K,\n completed_at=NOW())
        end
    end

    Scheduler-->>Scheduler: 다음 스케줄 대기 (1시간 후)
```

설계 포인트:
- `supplier_sync_job`으로 모든 배치 실행 이력을 추적한다. 언제 실행되었는지, 몇 건 성공/실패했는지 관리자가 확인할 수 있다.
- 매핑 확인 → UNMAPPED(대기) → 관리자 수동 연결 → MAPPED → 동기화 활성화 순서로 진행된다. 자동 매핑을 하지 않는 이유는 공급사 숙소와 자사 숙소가 같다고 자동으로 판단할 수 없기 때문이다.
- Supplier 데이터 업데이트 후 `RateUpdatedEvent`를 발행하여 캐시를 무효화한다. 고객이 다음 요금 조회 시 최신 데이터를 받는다.
- CONFLICT 상태는 자동으로 해결하지 않는다. 잘못된 자동 처리가 데이터 오염을 일으킬 수 있으므로 반드시 관리자가 판단한다.

---

## 참고: 시퀀스 다이어그램 범례

| 표기 | 의미 |
|------|------|
| `[비동기]` | 메인 플로우와 독립적으로 실행. 응답 시간에 영향 없음 |
| `@TransactionalEventListener(AFTER_COMMIT)` | DB 커밋 완료 후에만 이벤트 처리 |
| `FOR UPDATE` | 비관적 락. 트랜잭션 커밋까지 해당 행 잠금 |
| `DESIGN-ONLY` | 설계 범위. 실제 구현 없음 |
| `Mock` | 외부 API 대신 가상 데이터 반환하는 테스트/개발용 구현체 |

---

## 연관 문서

- [04-concurrency.md](04-concurrency.md) — 비관적 락 상세 전략, 낙관적 락 vs 비관적 락 비교
- [05-cache-strategy.md](05-cache-strategy.md) — Caffeine 캐시 전략, 캐시 무효화 정책
- [06-event-architecture.md](06-event-architecture.md) — 도메인 이벤트 목록, 발행/구독 구조
