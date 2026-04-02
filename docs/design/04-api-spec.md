# API 명세

> 작성일: 2026-03-28
> 기술 스택: Java 21, Spring Boot 3.4, Gradle (Kotlin DSL), MySQL
> 인증 방식: JWT (Bearer Token)

---

## 1. 공통 규약

### 1.1 공통 응답 구조 (ApiResponse)

모든 API는 다음 구조로 응답한다. 성공/실패 여부와 무관하게 HTTP 상태 코드와 `ApiResponse` 바디를 함께 반환한다.

```json
{
  "success": true,
  "data": { },
  "error": null
}
```

실패 시:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "INVENTORY_EXHAUSTED",
    "message": "선택한 날짜의 재고가 부족합니다."
  }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| success | boolean | 요청 처리 성공 여부 |
| data | Object / Array / null | 성공 시 응답 데이터 |
| error.code | String | 오류 코드 (비즈니스 레벨) |
| error.message | String | 사람이 읽을 수 있는 오류 메시지 |

### 1.2 HTTP 상태 코드 규약

| 코드 | 사용 상황 |
|------|----------|
| 200 OK | 조회, 수정 성공 |
| 201 Created | 리소스 생성 성공 |
| 400 Bad Request | 입력값 검증 실패 |
| 401 Unauthorized | 인증 토큰 없음 또는 만료 |
| 403 Forbidden | 권한 없음 (타인 리소스 접근 등) |
| 404 Not Found | 리소스 없음 |
| 409 Conflict | 중복 데이터 (예: 이미 등록된 사업자번호) |
| 500 Internal Server Error | 서버 내부 오류 |

### 1.3 인증 방식

#### JWT Bearer Token

모든 인증이 필요한 API는 HTTP 헤더에 JWT 토큰을 포함해야 한다.

```
Authorization: Bearer <token>
```

- 파트너(Extranet) 토큰과 회원(Customer) 토큰은 별도로 발급된다.
- 토큰에는 subject(사용자 ID), role, context(PARTNER/USER) 클레임이 포함된다.
- 만료 시간: 액세스 토큰 1시간 (refresh token은 DESIGN-ONLY)

#### 인증이 필요 없는 엔드포인트

- `POST /api/public/extranet/auth/login`
- `POST /api/extranet/partners` (파트너 등록)
- `POST /api/public/users/signup`
- `POST /api/public/users/login`
- `GET /api/public/search/properties`
- `GET /api/public/search/properties/{id}/rates`
- `GET /api/public/properties/{id}`

### 1.4 페이지네이션

목록 조회 API는 다음 쿼리 파라미터를 공통으로 사용한다.

| 파라미터 | 기본값 | 설명 |
|---------|--------|------|
| page | 0 | 페이지 번호 (0-based) |
| size | 20 | 페이지 크기 |

페이지네이션 응답:

```json
{
  "success": true,
  "data": {
    "content": [ ],
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8,
    "last": false
  },
  "error": null
}
```

### 1.5 BUILD vs DESIGN-ONLY 구분

| 구분 | 설명 |
|------|------|
| [BUILD] | 실제 구현하는 API. 동작하는 코드와 테스트가 존재한다. |
| [DESIGN-ONLY] | 설계만 완료한 API. 인터페이스 정의와 명세만 제공하며, 구현은 Mock 또는 스텁 수준이다. |

---

## 2. Extranet API (파트너용)

파트너가 숙소, 객실, 요금, 재고를 관리하는 API다. 모든 요청은 파트너 JWT 토큰이 필요하다.

Base Path: `/api/extranet`

---

### 2.1 인증

#### POST /api/public/extranet/auth/login — [BUILD]

파트너 사용자 로그인.

Request
```json
{
  "loginId": "partner_admin",
  "password": "password1234!"
}
```

Response `200 OK`
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "partnerUser": {
      "id": 1,
      "partnerId": 10,
      "name": "홍길동",
      "role": "OWNER"
    }
  },
  "error": null
}
```

---

### 2.2 파트너 관리

#### POST /api/extranet/partners — [BUILD]

파트너 등록. 인증 불필요(신규 가입).

Request
```json
{
  "businessName": "㈜스테이호스트",
  "businessNumber": "123-45-67890",
  "representative": "홍길동",
  "phone": "02-1234-5678",
  "email": "contact@stayhost.com",
  "bankName": "국민은행",
  "bankAccount": "123456-78-901234",
  "loginId": "partner_admin",
  "password": "password1234!",
  "adminName": "홍길동"
}
```

Response `201 Created`
```json
{
  "success": true,
  "data": {
    "partnerId": 10,
    "businessName": "㈜스테이호스트",
    "status": "PENDING"
  },
  "error": null
}
```

> 등록 직후 status는 PENDING이다. 관리자 승인 후 ACTIVE로 전환된다.

---

#### GET /api/extranet/partners/me — [BUILD]

내 파트너 정보 조회.

Response `200 OK`
```json
{
  "success": true,
  "data": {
    "id": 10,
    "businessName": "㈜스테이호스트",
    "businessNumber": "123-45-67890",
    "representative": "홍길동",
    "phone": "02-1234-5678",
    "email": "contact@stayhost.com",
    "bankName": "국민은행",
    "bankAccount": "123456-78-901234",
    "status": "ACTIVE",
    "createdAt": "2026-03-01T09:00:00"
  },
  "error": null
}
```

---

#### PUT /api/extranet/partners/me — [BUILD]

파트너 정보 수정. `businessNumber`는 수정 불가.

Request
```json
{
  "phone": "02-9999-8888",
  "email": "new@stayhost.com",
  "bankName": "신한은행",
  "bankAccount": "987654-32-109876"
}
```

Response `200 OK`
```json
{
  "success": true,
  "data": {
    "id": 10,
    "phone": "02-9999-8888",
    "email": "new@stayhost.com",
    "updatedAt": "2026-03-28T10:30:00"
  },
  "error": null
}
```

---

### 2.3 숙소 관리

#### POST /api/extranet/properties — [BUILD]

숙소 등록.

Request
```json
{
  "name": "스테이호스트 서울 강남점",
  "type": "HOTEL",
  "description": "강남역 도보 5분, 비즈니스 여행에 최적화된 숙소입니다.",
  "address": "서울특별시 강남구 테헤란로 123",
  "region": "서울",
  "latitude": 37.4979,
  "longitude": 127.0276,
  "checkInTime": "15:00",
  "checkOutTime": "11:00",
  "thumbnailUrl": "https://cdn.example.com/img/property_10_thumb.jpg"
}
```

Response `201 Created`
```json
{
  "success": true,
  "data": {
    "id": 100,
    "name": "스테이호스트 서울 강남점",
    "status": "DRAFT"
  },
  "error": null
}
```

---

#### GET /api/extranet/properties — [BUILD]

내 숙소 목록 조회 (페이지네이션).

Query Parameters

| 파라미터 | 필수 | 설명 |
|---------|------|------|
| page | N | 페이지 번호 (기본 0) |
| size | N | 페이지 크기 (기본 20) |
| status | N | 상태 필터 (DRAFT/ACTIVE/INACTIVE) |

Response `200 OK`
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 100,
        "name": "스테이호스트 서울 강남점",
        "region": "서울",
        "type": "HOTEL",
        "status": "ACTIVE",
        "thumbnailUrl": "https://cdn.example.com/img/property_10_thumb.jpg",
        "roomTypeCount": 3
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 5,
    "totalPages": 1,
    "last": true
  },
  "error": null
}
```

---

#### GET /api/extranet/properties/{id} — [BUILD]

숙소 상세 조회.

Response `200 OK`
```json
{
  "success": true,
  "data": {
    "id": 100,
    "name": "스테이호스트 서울 강남점",
    "type": "HOTEL",
    "description": "강남역 도보 5분...",
    "address": "서울특별시 강남구 테헤란로 123",
    "region": "서울",
    "latitude": 37.4979,
    "longitude": 127.0276,
    "checkInTime": "15:00",
    "checkOutTime": "11:00",
    "thumbnailUrl": "https://cdn.example.com/img/property_10_thumb.jpg",
    "status": "ACTIVE",
    "images": [
      { "id": 1, "imageUrl": "https://cdn.example.com/img/1.jpg", "sortOrder": 0 }
    ],
    "createdAt": "2026-03-01T09:00:00",
    "updatedAt": "2026-03-10T14:00:00"
  },
  "error": null
}
```

---

#### PUT /api/extranet/properties/{id} — [BUILD]

숙소 정보 수정.

Request
```json
{
  "name": "스테이호스트 서울 강남점 (리뉴얼)",
  "description": "2026년 리뉴얼 오픈! 새로운 인테리어로 재단장했습니다.",
  "checkInTime": "14:00",
  "checkOutTime": "12:00",
  "thumbnailUrl": "https://cdn.example.com/img/new_thumb.jpg"
}
```

Response `200 OK`
```json
{
  "success": true,
  "data": {
    "id": 100,
    "name": "스테이호스트 서울 강남점 (리뉴얼)",
    "updatedAt": "2026-03-28T11:00:00"
  },
  "error": null
}
```

---

#### PATCH /api/extranet/properties/{id}/status — [BUILD]

숙소 상태 변경. DRAFT → ACTIVE → INACTIVE 전환.

Request
```json
{
  "status": "ACTIVE"
}
```

Response `200 OK`
```json
{
  "success": true,
  "data": {
    "id": 100,
    "status": "ACTIVE",
    "updatedAt": "2026-03-28T11:05:00"
  },
  "error": null
}
```

---

### 2.4 객실 유형 관리

#### POST /api/extranet/properties/{id}/room-types — [BUILD]

객실 유형 등록.

Request
```json
{
  "name": "스탠다드 더블",
  "description": "편안한 더블 침대가 있는 표준 객실입니다.",
  "maxOccupancy": 2,
  "basePrice": 120000,
  "amenities": ["WiFi", "TV", "에어컨", "냉장고"],
  "totalRoomCount": 10
}
```

Response `201 Created`
```json
{
  "success": true,
  "data": {
    "id": 200,
    "propertyId": 100,
    "name": "스탠다드 더블",
    "maxOccupancy": 2,
    "basePrice": 120000,
    "totalRoomCount": 10,
    "status": "ACTIVE"
  },
  "error": null
}
```

---

#### GET /api/extranet/properties/{id}/room-types — [BUILD]

숙소의 객실 유형 목록 조회.

Response `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": 200,
      "name": "스탠다드 더블",
      "maxOccupancy": 2,
      "basePrice": 120000,
      "amenities": ["WiFi", "TV", "에어컨", "냉장고"],
      "totalRoomCount": 10,
      "status": "ACTIVE"
    },
    {
      "id": 201,
      "name": "디럭스 트윈",
      "maxOccupancy": 3,
      "basePrice": 180000,
      "amenities": ["WiFi", "TV", "에어컨", "냉장고", "욕조"],
      "totalRoomCount": 5,
      "status": "ACTIVE"
    }
  ],
  "error": null
}
```

---

#### PUT /api/extranet/room-types/{id} — [BUILD]

객실 유형 수정.

Request
```json
{
  "name": "스탠다드 더블 (개선)",
  "description": "새롭게 리뉴얼된 객실입니다.",
  "maxOccupancy": 2,
  "basePrice": 130000,
  "amenities": ["WiFi", "TV", "에어컨", "냉장고", "스마트 TV"]
}
```

Response `200 OK`
```json
{
  "success": true,
  "data": {
    "id": 200,
    "name": "스탠다드 더블 (개선)",
    "basePrice": 130000,
    "updatedAt": "2026-03-28T12:00:00"
  },
  "error": null
}
```

---

### 2.5 요금 관리

#### PUT /api/extranet/room-types/{id}/rates — [BUILD]

날짜 범위 요금 일괄 설정. 이미 등록된 날짜는 UPSERT 처리된다.

Request
```json
{
  "startDate": "2026-04-01",
  "endDate": "2026-04-30",
  "price": 150000,
  "daysOfWeek": [1, 2, 3, 4, 5]
}
```

> `daysOfWeek`: 1=월, 7=일. 생략 시 전체 날짜에 적용한다.

Response `200 OK`
```json
{
  "success": true,
  "data": {
    "roomTypeId": 200,
    "appliedDates": 22,
    "startDate": "2026-04-01",
    "endDate": "2026-04-30",
    "price": 150000
  },
  "error": null
}
```

---

#### GET /api/extranet/room-types/{id}/rates — [BUILD]

날짜 범위 요금 조회.

Query Parameters

| 파라미터 | 필수 | 설명 |
|---------|------|------|
| startDate | Y | 조회 시작일 (yyyy-MM-dd) |
| endDate | Y | 조회 종료일 (yyyy-MM-dd) |

Response `200 OK`
```json
{
  "success": true,
  "data": {
    "roomTypeId": 200,
    "rates": [
      { "date": "2026-04-01", "price": 150000 },
      { "date": "2026-04-02", "price": 150000 },
      { "date": "2026-04-05", "price": 120000 },
      { "date": "2026-04-06", "price": 120000 }
    ]
  },
  "error": null
}
```

> rate가 없는 날짜는 room_type의 base_price가 적용된다.

---

### 2.6 재고 관리

#### PUT /api/extranet/room-types/{id}/inventory — [BUILD]

날짜 범위 재고 일괄 설정.

재고를 설정할 때 `totalCount`를 변경하면 `total_count`가 갱신되고, `reserved_count`는 현재 예약 건수를 유지한다. `totalCount < reserved_count`인 경우 설정이 거부된다.

Request
```json
{
  "startDate": "2026-04-01",
  "endDate": "2026-04-30",
  "totalCount": 10
}
```

Response `200 OK`
```json
{
  "success": true,
  "data": {
    "roomTypeId": 200,
    "appliedDates": 30,
    "startDate": "2026-04-01",
    "endDate": "2026-04-30",
    "totalCount": 10
  },
  "error": null
}
```

---

#### GET /api/extranet/room-types/{id}/inventory — [BUILD]

날짜 범위 재고 조회.

Query Parameters

| 파라미터 | 필수 | 설명 |
|---------|------|------|
| startDate | Y | 조회 시작일 |
| endDate | Y | 조회 종료일 |

Response `200 OK`
```json
{
  "success": true,
  "data": {
    "roomTypeId": 200,
    "inventory": [
      {
        "date": "2026-04-01",
        "totalCount": 10,
        "reservedCount": 3,
        "availableCount": 7
      },
      {
        "date": "2026-04-02",
        "totalCount": 10,
        "reservedCount": 10,
        "availableCount": 0
      }
    ]
  },
  "error": null
}
```

> `availableCount = totalCount - reservedCount`. DB에는 저장하지 않고 조회 시 계산한다.

---

### 2.7 예약 조회

#### GET /api/extranet/reservations — [BUILD]

내 숙소의 예약 목록 조회. 파트너가 소유한 숙소의 예약만 반환한다.

Query Parameters

| 파라미터 | 필수 | 설명 |
|---------|------|------|
| propertyId | N | 특정 숙소 필터 |
| status | N | 예약 상태 필터 (CONFIRMED/CANCELLED) |
| checkInFrom | N | 체크인 시작일 필터 |
| checkInTo | N | 체크인 종료일 필터 |
| page | N | 페이지 번호 |
| size | N | 페이지 크기 |

Response `200 OK`
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 5001,
        "reservationNumber": "RSV-20260328-5001",
        "propertyName": "스테이호스트 서울 강남점",
        "roomTypeName": "스탠다드 더블",
        "guestName": "김민준",
        "checkInDate": "2026-04-10",
        "checkOutDate": "2026-04-12",
        "guestCount": 2,
        "basePrice": 300000,
        "discountAmount": 0,
        "finalPrice": 300000,
        "status": "CONFIRMED",
        "source": "DIRECT",
        "confirmedAt": "2026-03-28T09:15:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 48,
    "totalPages": 3,
    "last": false
  },
  "error": null
}
```

---

#### GET /api/extranet/reservations/{id} — [BUILD]

예약 상세 조회. 날짜별 요금 상세 포함.

Response `200 OK`
```json
{
  "success": true,
  "data": {
    "id": 5001,
    "reservationNumber": "RSV-20260328-5001",
    "propertyId": 100,
    "propertyName": "스테이호스트 서울 강남점",
    "roomTypeId": 200,
    "roomTypeName": "스탠다드 더블",
    "guestName": "김민준",
    "guestPhone": "010-1234-5678",
    "checkInDate": "2026-04-10",
    "checkOutDate": "2026-04-12",
    "guestCount": 2,
    "basePrice": 300000,
    "discountAmount": 0,
    "finalPrice": 300000,
    "status": "CONFIRMED",
    "source": "DIRECT",
    "dailyRateResults": [
      { "date": "2026-04-10", "price": 150000 },
      { "date": "2026-04-11", "price": 150000 }
    ],
    "confirmedAt": "2026-03-28T09:15:00",
    "createdAt": "2026-03-28T09:15:00"
  },
  "error": null
}
```

---

#### PATCH /api/extranet/reservations/{id}/confirm — [DESIGN-ONLY]

파트너 수동 확정 모드에서 PENDING 상태 예약을 확정한다. 기본 자동 확정 모드에서는 이 API가 호출될 일이 없다. 향후 파트너 옵션으로 수동 확정 모드를 지원할 때 구현한다.

Request (body 없음)

Response `200 OK`
```json
{
  "success": true,
  "data": {
    "id": 5001,
    "status": "CONFIRMED",
    "confirmedAt": "2026-03-28T10:00:00"
  },
  "error": null
}
```

---

## 3. Customer API (고객용)

고객이 숙소를 검색하고 예약하는 API다.

Base Path: `/api`

---

### 3.1 회원 인증

#### POST /api/public/users/signup — [BUILD]

회원가입.

Request
```json
{
  "email": "user@example.com",
  "password": "password1234!",
  "name": "이지은",
  "phone": "010-5678-1234"
}
```

Response `201 Created`
```json
{
  "success": true,
  "data": {
    "id": 3001,
    "email": "user@example.com",
    "name": "이지은"
  },
  "error": null
}
```

---

#### POST /api/public/users/login — [BUILD]

회원 로그인.

Request
```json
{
  "email": "user@example.com",
  "password": "password1234!"
}
```

Response `200 OK`
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": 3001,
      "email": "user@example.com",
      "name": "이지은"
    }
  },
  "error": null
}
```

---

#### GET /api/users/me — [BUILD]

내 회원 정보 조회. 인증 필요.

Response `200 OK`
```json
{
  "success": true,
  "data": {
    "id": 3001,
    "email": "user@example.com",
    "name": "이지은",
    "phone": "010-5678-1234",
    "status": "ACTIVE",
    "createdAt": "2026-03-01T10:00:00"
  },
  "error": null
}
```

---

### 3.2 숙소 검색

#### GET /api/public/search/properties — [BUILD]

숙소 검색. 인증 불필요.

검색 성능에 대해 고민이 있었다.

- 지역/호텔명 검색을 DB `LIKE`로 처리하면 `LIKE '%키워드%'` 패턴에서 인덱스를 활용하지 못해 풀스캔이 발생할 수 있다
- 대규모 트래픽에서는 Elasticsearch 같은 검색 엔진을 도입하는 것이 이상적이지만, 현재 단계에서는 두 가지로 대응했다

첫째, 지역 검색은 `region = :region` 동등 조건으로 처리한다. 한국 지역명은 유한하고("서울", "부산" 등) 정확히 일치하므로 `idx_property_region_status (region, status)` 커버링 인덱스가 유효하다.

둘째, 이름 검색은 `name LIKE :keyword%` 전방 일치(prefix) 방식을 우선 적용한다. 완전 일치 검색보다 히트율이 낮아지는 단점은 있지만, 풀스캔보다는 낫다. 검색 트래픽이 증가하면 Caffeine 캐시로 인기 키워드 결과를 캐시하거나, 검색 엔진 도입을 검토한다.

Query Parameters

| 파라미터 | 필수 | 설명 |
|---------|------|------|
| region | N | 지역 (서울, 부산, 제주 등) |
| keyword | N | 숙소명 검색어 |
| checkIn | N | 체크인 날짜 (yyyy-MM-dd) |
| checkOut | N | 체크아웃 날짜 (yyyy-MM-dd) |
| guestCount | N | 투숙 인원 수 |
| sort | N | 정렬 기준: `PRICE_ASC`, `PRICE_DESC`, `NAME_ASC` (기본: `NAME_ASC`) |
| page | N | 페이지 번호 (기본 0) |
| size | N | 페이지 크기 (기본 20) |

Response `200 OK`
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 100,
        "name": "스테이호스트 서울 강남점",
        "type": "HOTEL",
        "region": "서울",
        "address": "서울특별시 강남구 테헤란로 123",
        "thumbnailUrl": "https://cdn.example.com/img/property_10_thumb.jpg",
        "checkInTime": "15:00",
        "checkOutTime": "11:00",
        "minPrice": 120000,
        "availableRoomTypes": 2
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 35,
    "totalPages": 2,
    "last": false
  },
  "error": null
}
```

> `minPrice`는 요청한 날짜 범위 내 가장 저렴한 객실 요금이다. 날짜 미입력 시 base_price 기준으로 반환한다.
> `availableRoomTypes`는 요청 인원과 재고 조건을 충족하는 객실 유형 수다.

---

#### GET /api/public/search/properties/{id}/rates — [BUILD]

숙소의 객실별 날짜 범위 요금 조회. 검색 결과에서 요금 달력을 표시할 때 사용한다. 인증 불필요.

이 API는 조회 빈도가 높다. 숙소 상세 페이지 진입 시마다 30일치 요금을 한 번에 조회하는 패턴을 고려하면, room_type + date 조합으로 Caffeine 캐시(`rate:{roomTypeId}:{date}`, TTL 3분)가 효과적으로 작동한다.

Query Parameters

| 파라미터 | 필수 | 설명 |
|---------|------|------|
| startDate | Y | 시작일 |
| endDate | Y | 종료일 |

Response `200 OK`
```json
{
  "success": true,
  "data": {
    "propertyId": 100,
    "roomTypes": [
      {
        "id": 200,
        "name": "스탠다드 더블",
        "maxOccupancy": 2,
        "rates": [
          { "date": "2026-04-01", "price": 150000, "available": true },
          { "date": "2026-04-02", "price": 150000, "available": false },
          { "date": "2026-04-03", "price": 120000, "available": true }
        ]
      },
      {
        "id": 201,
        "name": "디럭스 트윈",
        "maxOccupancy": 3,
        "rates": [
          { "date": "2026-04-01", "price": 200000, "available": true },
          { "date": "2026-04-02", "price": 200000, "available": true },
          { "date": "2026-04-03", "price": 180000, "available": true }
        ]
      }
    ]
  },
  "error": null
}
```

> `available: false`는 해당 날짜 재고가 0인 경우다.

---

### 3.3 숙소 상세

#### GET /api/public/properties/{id} — [BUILD]

숙소 상세 정보 조회. 객실 유형 목록 포함. 인증 불필요.

Response `200 OK`
```json
{
  "success": true,
  "data": {
    "id": 100,
    "name": "스테이호스트 서울 강남점",
    "type": "HOTEL",
    "description": "강남역 도보 5분...",
    "address": "서울특별시 강남구 테헤란로 123",
    "region": "서울",
    "latitude": 37.4979,
    "longitude": 127.0276,
    "checkInTime": "15:00",
    "checkOutTime": "11:00",
    "thumbnailUrl": "https://cdn.example.com/img/property_10_thumb.jpg",
    "images": [
      { "imageUrl": "https://cdn.example.com/img/1.jpg", "sortOrder": 0 },
      { "imageUrl": "https://cdn.example.com/img/2.jpg", "sortOrder": 1 }
    ],
    "roomTypes": [
      {
        "id": 200,
        "name": "스탠다드 더블",
        "description": "편안한 더블 침대가 있는 표준 객실입니다.",
        "maxOccupancy": 2,
        "basePrice": 120000,
        "amenities": ["WiFi", "TV", "에어컨", "냉장고"]
      },
      {
        "id": 201,
        "name": "디럭스 트윈",
        "description": "트윈 침대 구성의 넓은 객실입니다.",
        "maxOccupancy": 3,
        "basePrice": 180000,
        "amenities": ["WiFi", "TV", "에어컨", "냉장고", "욕조"]
      }
    ]
  },
  "error": null
}
```

---

### 3.4 예약

#### POST /api/reservations — [BUILD]

예약 생성. 재고 차감과 예약 확정이 하나의 트랜잭션 안에서 원자적으로 처리된다.

내부적으로 다음 순서로 동작한다.

1. 체크인~체크아웃 각 날짜의 inventory 행에 `SELECT ... FOR UPDATE` 비관적 락 획득 (`ORDER BY room_type_id, date` 순서 고정)
2. 모든 날짜의 `available_count(= total_count - reserved_count) >= 1` 확인
3. 재고 충족 시 `reserved_count + 1` 갱신
4. 날짜별 요금 조회 후 `reservation_daily_rate` 스냅샷 저장
5. `reservation` 레코드를 `CONFIRMED` 상태로 즉시 생성
6. 트랜잭션 커밋 후 `ReservationCreatedEvent` 발행 (채널 동기화 비동기 트리거)

인증 필요.

Request
```json
{
  "propertyId": 100,
  "roomTypeId": 200,
  "checkInDate": "2026-04-10",
  "checkOutDate": "2026-04-12",
  "guestName": "김민준",
  "guestPhone": "010-1234-5678",
  "guestCount": 2
}
```

Response `201 Created`
```json
{
  "success": true,
  "data": {
    "id": 5001,
    "reservationNumber": "RSV-20260328-5001",
    "propertyName": "스테이호스트 서울 강남점",
    "roomTypeName": "스탠다드 더블",
    "checkInDate": "2026-04-10",
    "checkOutDate": "2026-04-12",
    "guestCount": 2,
    "basePrice": 300000,
    "discountAmount": 0,
    "finalPrice": 300000,
    "status": "CONFIRMED",
    "confirmedAt": "2026-03-28T09:15:00",
    "dailyRateResults": [
      { "date": "2026-04-10", "price": 150000 },
      { "date": "2026-04-11", "price": 150000 }
    ]
  },
  "error": null
}
```

실패 예시 `409 Conflict`
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "INVENTORY_EXHAUSTED",
    "message": "선택한 날짜(2026-04-11)의 재고가 부족합니다."
  }
}
```

---

#### GET /api/reservations — [BUILD]

내 예약 목록 조회. 인증 필요.

Query Parameters

| 파라미터 | 필수 | 설명 |
|---------|------|------|
| status | N | CONFIRMED / CANCELLED |
| page | N | 페이지 번호 |
| size | N | 페이지 크기 |

Response `200 OK`
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 5001,
        "reservationNumber": "RSV-20260328-5001",
        "propertyName": "스테이호스트 서울 강남점",
        "roomTypeName": "스탠다드 더블",
        "checkInDate": "2026-04-10",
        "checkOutDate": "2026-04-12",
        "basePrice": 300000,
        "discountAmount": 0,
        "finalPrice": 300000,
        "status": "CONFIRMED",
        "thumbnailUrl": "https://cdn.example.com/img/property_10_thumb.jpg"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 12,
    "totalPages": 1,
    "last": true
  },
  "error": null
}
```

---

#### GET /api/reservations/{id} — [BUILD]

예약 상세 조회. 본인 예약만 조회 가능하며, 타인 예약 접근 시 403을 반환한다. 인증 필요.

Response `200 OK`
```json
{
  "success": true,
  "data": {
    "id": 5001,
    "reservationNumber": "RSV-20260328-5001",
    "propertyId": 100,
    "propertyName": "스테이호스트 서울 강남점",
    "propertyAddress": "서울특별시 강남구 테헤란로 123",
    "roomTypeId": 200,
    "roomTypeName": "스탠다드 더블",
    "checkInDate": "2026-04-10",
    "checkOutDate": "2026-04-12",
    "checkInTime": "15:00",
    "checkOutTime": "11:00",
    "guestName": "김민준",
    "guestPhone": "010-1234-5678",
    "guestCount": 2,
    "basePrice": 300000,
    "discountAmount": 0,
    "finalPrice": 300000,
    "status": "CONFIRMED",
    "dailyRateResults": [
      { "date": "2026-04-10", "price": 150000 },
      { "date": "2026-04-11", "price": 150000 }
    ],
    "confirmedAt": "2026-03-28T09:15:00",
    "createdAt": "2026-03-28T09:15:00"
  },
  "error": null
}
```

---

#### POST /api/reservations/{id}/cancel — [BUILD]

예약 취소. 본인 예약만 취소 가능. 취소 시 inventory.reserved_count를 복원한다. 인증 필요.

Request
```json
{
  "cancelReason": "일정 변경으로 인한 취소"
}
```

Response `200 OK`
```json
{
  "success": true,
  "data": {
    "id": 5001,
    "reservationNumber": "RSV-20260328-5001",
    "status": "CANCELLED",
    "cancelledAt": "2026-03-29T11:00:00",
    "cancelReason": "일정 변경으로 인한 취소"
  },
  "error": null
}
```

---

## 4. Admin API (내부 관리용) — [DESIGN-ONLY]

플랫폼 운영자가 파트너 승인, 숙소 관리, 전체 예약 조회를 수행하는 API다. 구현은 하지 않으며 인터페이스와 명세만 정의한다.

Base Path: `/api/admin`
인증: 별도 Admin JWT 토큰 (role: ADMIN)

---

#### GET /api/admin/partners — [DESIGN-ONLY]

파트너 목록 조회. 상태별 필터, 페이지네이션 지원.

Query Parameters: `status`, `page`, `size`

Response 예시
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 10,
        "businessName": "㈜스테이호스트",
        "businessNumber": "123-45-67890",
        "representative": "홍길동",
        "status": "PENDING",
        "createdAt": "2026-03-01T09:00:00"
      }
    ],
    "page": 0, "size": 20, "totalElements": 100, "totalPages": 5, "last": false
  },
  "error": null
}
```

---

#### PATCH /api/admin/partners/{id}/status — [DESIGN-ONLY]

파트너 상태 변경 (PENDING → ACTIVE 승인, ACTIVE → SUSPENDED 정지 등).

Request
```json
{
  "status": "ACTIVE",
  "reason": "사업자 서류 확인 완료"
}
```

Response 예시
```json
{
  "success": true,
  "data": { "id": 10, "status": "ACTIVE", "updatedAt": "2026-03-28T13:00:00" },
  "error": null
}
```

---

#### GET /api/admin/properties — [DESIGN-ONLY]

전체 숙소 목록. `partnerId`, `status`, `region` 필터 지원.

---

#### GET /api/admin/reservations — [DESIGN-ONLY]

전체 예약 목록. `propertyId`, `status`, `checkInFrom`, `checkInTo` 필터 지원.

---

#### GET /api/admin/channels — [DESIGN-ONLY]

채널 목록 조회.

---

#### GET /api/admin/suppliers — [DESIGN-ONLY]

공급자 목록 조회.

---

## 5. Channel Manager API — [DESIGN-ONLY]

외부 OTA 채널과의 재고·요금·예약 동기화 API다. 실제 외부 채널 연동은 구현하지 않으며 인터페이스 + Mock으로 제공한다.

Base Path: `/api/channels`
인증: Admin JWT 토큰 또는 채널별 서비스 계정 토큰

---

#### POST /api/channels/{channelId}/push-inventory — [DESIGN-ONLY]

재고 변경을 외부 채널에 푸시한다 (OUTBOUND).

자사 예약·취소 발생 → `ReservationCreatedEvent` / `ReservationCancelledEvent` → 비동기 채널 푸시 순서로 동작한다. 이 API는 수동 트리거용이다.

Request
```json
{
  "propertyId": 100,
  "roomTypeId": 200,
  "dates": ["2026-04-10", "2026-04-11"],
  "availableCount": 5
}
```

Response 예시
```json
{
  "success": true,
  "data": {
    "channelId": 1,
    "syncLogId": 9001,
    "status": "SUCCESS",
    "syncedAt": "2026-03-28T14:00:00"
  },
  "error": null
}
```

---

#### POST /api/channels/{channelId}/push-rates — [DESIGN-ONLY]

요금 변경을 외부 채널에 푸시한다 (OUTBOUND).

Request
```json
{
  "propertyId": 100,
  "roomTypeId": 200,
  "rates": [
    { "date": "2026-04-10", "price": 150000 },
    { "date": "2026-04-11", "price": 150000 }
  ]
}
```

Response 예시
```json
{
  "success": true,
  "data": { "channelId": 1, "syncLogId": 9002, "status": "SUCCESS" },
  "error": null
}
```

---

#### POST /api/channels/{channelId}/webhook/reservation — [DESIGN-ONLY]

외부 채널에서 예약 발생 시 웹훅으로 수신한다 (INBOUND). 수신 후 자사 예약을 생성하고, 타 채널에 재고를 동기화한다.

Request (채널별 스키마 상이 — 내부에서 표준화 처리)
```json
{
  "externalReservationId": "BK-20260328-ABC123",
  "externalPropertyId": "BKCOM-PROP-9876",
  "externalRoomId": "BKCOM-ROOM-5432",
  "checkIn": "2026-04-10",
  "checkOut": "2026-04-12",
  "guestName": "John Doe",
  "guestCount": 2,
  "basePrice": 300000,
  "discountAmount": 0,
  "finalPrice": 300000,
  "currency": "KRW"
}
```

Response 예시
```json
{
  "success": true,
  "data": {
    "reservationId": 5002,
    "reservationNumber": "RSV-20260328-5002",
    "status": "CONFIRMED"
  },
  "error": null
}
```

---

#### GET /api/channels/{channelId}/sync-status — [DESIGN-ONLY]

채널 동기화 상태 및 최근 이력 조회.

Response 예시
```json
{
  "success": true,
  "data": {
    "channelId": 1,
    "channelName": "Booking.com",
    "status": "ACTIVE",
    "recentLogs": [
      {
        "id": 9001,
        "syncType": "INVENTORY",
        "direction": "OUTBOUND",
        "status": "SUCCESS",
        "retryCount": 0,
        "createdAt": "2026-03-28T14:00:00"
      }
    ]
  },
  "error": null
}
```

---

## 6. Supplier API — [DESIGN-ONLY]

외부 공급자로부터 숙소 데이터를 수집하고 자사 숙소에 매핑하는 API다. 배치 동기화와 수동 트리거를 지원한다.

Base Path: `/api/suppliers`
인증: Admin JWT 토큰

---

#### POST /api/suppliers/{supplierId}/sync — [BUILD]

수동 동기화 트리거. 배치 잡을 즉시 실행한다.

Request
```json
{
  "jobType": "INCREMENTAL"
}
```

Response 예시
```json
{
  "success": true,
  "data": {
    "syncJobId": 7001,
    "status": "RUNNING",
    "startedAt": "2026-03-28T15:00:00"
  },
  "error": null
}
```

---

#### GET /api/suppliers/{supplierId}/properties — [DESIGN-ONLY]

공급자로부터 수집한 숙소 목록. 매핑 상태 필터 지원.

Query Parameters: `mappingStatus` (MAPPED/UNMAPPED/CONFLICT), `page`, `size`

Response 예시
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 8001,
        "externalPropertyId": "SUPPLIER-PROP-12345",
        "mappingStatus": "UNMAPPED",
        "lastSyncedAt": "2026-03-28T06:00:00"
      }
    ],
    "page": 0, "size": 20, "totalElements": 50, "totalPages": 3, "last": false
  },
  "error": null
}
```

---

#### POST /api/suppliers/{supplierId}/properties/{externalId}/map — [DESIGN-ONLY]

공급자 숙소를 자사 숙소에 매핑한다.

Request
```json
{
  "propertyId": 100
}
```

Response 예시
```json
{
  "success": true,
  "data": {
    "supplierPropertyId": 8001,
    "propertyId": 100,
    "mappingStatus": "MAPPED",
    "createdAt": "2026-03-28T15:30:00"
  },
  "error": null
}
```

---

#### GET /api/suppliers/{supplierId}/sync-jobs — [DESIGN-ONLY]

동기화 작업 이력 조회.

Query Parameters: `status`, `page`, `size`

Response 예시
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 7001,
        "jobType": "INCREMENTAL",
        "status": "COMPLETED",
        "totalCount": 500,
        "successCount": 498,
        "failCount": 2,
        "startedAt": "2026-03-28T06:00:00",
        "completedAt": "2026-03-28T06:03:42"
      }
    ],
    "page": 0, "size": 20, "totalElements": 30, "totalPages": 2, "last": false
  },
  "error": null
}
```

---

## 7. 주요 오류 코드 목록

| 코드 | HTTP | 설명 |
|------|------|------|
| INVALID_INPUT | 400 | 입력값 검증 실패 |
| UNAUTHORIZED | 401 | 인증 토큰 없음 또는 만료 |
| FORBIDDEN | 403 | 접근 권한 없음 |
| NOT_FOUND | 404 | 리소스 없음 |
| DUPLICATE_BUSINESS_NUMBER | 409 | 중복 사업자번호 |
| DUPLICATE_EMAIL | 409 | 중복 이메일 |
| INVENTORY_EXHAUSTED | 409 | 재고 부족으로 예약 실패 |
| PROPERTY_NOT_ACTIVE | 400 | 비활성 숙소에 대한 예약 시도 |
| ROOM_TYPE_NOT_AVAILABLE | 400 | 비활성 객실 유형 |
| RESERVATION_NOT_CANCELLABLE | 400 | 취소 불가 상태의 예약 |
| PARTNER_NOT_ACTIVE | 403 | 미승인 또는 정지된 파트너 |
| INTERNAL_ERROR | 500 | 서버 내부 오류 |
