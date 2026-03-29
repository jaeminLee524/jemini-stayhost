# 코드 컨벤션

> 작성일: 2026-03-28

---

## 1. 패키지 구조 규칙

### 최상위 구조: Bounded Context 기반

패키지는 기술적 역할(controller, service, repository)이 아니라 도메인(Bounded Context)을 최상위 기준으로 삼는다.

- 이 결정은 "지금은 단일 모듈이지만, 나중에 모듈 또는 서비스로 분리할 수 있어야 한다"는 원칙에서 나왔다
- 기술 계층을 최상위로 두면 `controller` 패키지 안에 모든 도메인의 컨트롤러가 섞이게 되어, 도메인별 분리가 불가능해진다

```
com.jemini.stayhost
├── partner/                        # Partner Context
│   ├── domain/                     # 핵심 도메인 (JPA 어노테이션만 허용, Spring 의존 금지)
│   │   ├── model/                  # Entity, VO, Enum
│   │   ├── component/              # Reader/Manager 인터페이스 (도구 계약)
│   │   └── event/                  # 도메인 이벤트
│   ├── application/                # 유스케이스 (비즈니스 로직)
│   │   └── service/
│   │       └── PartnerService.java # Reader/Manager 인터페이스에만 의존
│   ├── infrastructure/             # 인프라 구현체 (DIP 역전)
│   │   ├── persistence/            # JPA Repository 구현 (Adapter)
│   │   └── component/              # Reader/Manager 구현체 (@Component)
│   └── presentation/               # API 계층
│       ├── controller/
│       ├── docs/                   # Swagger Docs 인터페이스
│       └── dto/                    # Request/Response DTO
│
├── property/                       # Property Context
│   ├── domain/
│   │   ├── model/
│   │   ├── component/              # PropertyReader, InventoryManager 등 인터페이스
│   │   └── event/
│   ├── application/
│   ├── infrastructure/
│   │   ├── persistence/
│   │   └── component/              # PropertyReaderImpl, InventoryManagerImpl
│   └── presentation/
│       ├── controller/
│       ├── docs/
│       └── dto/
│
├── booking/                        # Booking Context
│   ├── domain/
│   │   ├── model/
│   │   ├── component/              # ReservationReader, ReservationManager 등 인터페이스
│   │   └── event/
│   ├── application/
│   │   ├── facade/
│   │   │   └── ReservationFacade.java  # cross-context 조합 (타 context Service 호출)
│   │   └── service/
│   │       └── ReservationService.java # 순수 booking 로직 (@Transactional 관리)
│   ├── infrastructure/
│   │   ├── persistence/
│   │   └── component/              # ReservationReaderImpl, ReservationManagerImpl
│   └── presentation/
│       ├── controller/
│       ├── docs/
│       └── dto/
│
├── user/                           # Customer Context
│   ├── domain/
│   ├── application/
│   ├── infrastructure/
│   └── presentation/
│
├── search/                         # Search Context (읽기 전용)
│   ├── application/                # 캐시 + 조회 로직
│   └── presentation/
│
├── channel/                        # Channel Manager (DESIGN-ONLY)
│   ├── domain/
│   ├── application/
│   └── infrastructure/
│       └── adapter/                # ChannelAdapter 인터페이스 + Mock
│
├── supplier/                       # Supplier (DESIGN-ONLY)
│   ├── domain/
│   ├── application/
│   └── infrastructure/
│       └── adapter/                # SupplierAdapter 인터페이스 + Mock
│
└── common/
    ├── config/                     # Spring 설정 (Cache, Security 등)
    ├── response/                   # ApiBaseResponse, ResultType, ErrorMessage
    ├── exception/                  # ApiControllerAdvice, BusinessException, ErrorCode
    └── security/                   # JWT 필터, 인증 설정
```

> 설계 참고: 당근마켓의 헥사고날 아키텍처 구조를 참고했으나, 풀 헥사고날(port/port-in-impl/port-out-impl)은 본 프로젝트에서 과하다고 판단했다. 대신 핵심 원칙인 DIP(Dependency Inversion Principle) 만 적용하여, domain이 infrastructure를 모르는 구조를 모놀리스에서 실현했다.

### 계층 간 의존 방향 (DIP 적용)

```
presentation → application → domain ← infrastructure
                                          (DIP 역전)
```

- domain: JPA 어노테이션(`@Entity`, `@Id`, `@Column` 등 `jakarta.persistence` 패키지)만 허용한다. Spring 프레임워크(`@Service`, `@Component`, `@Autowired` 등)에는 의존하지 않는다. Repository 인터페이스와 Reader/Manager 인터페이스(도구 계약)를 정의한다
- application: domain의 Reader/Manager 인터페이스에만 의존한다. Repository를 직접 사용하지 않고, Reader(읽기)/Manager(쓰기)를 통해서만 데이터에 접근한다
- infrastructure: domain의 Repository 인터페이스와 Reader/Manager 인터페이스를 구현(Adapter) 한다. Reader/Manager 구현체가 JPA Repository를 내부적으로 사용한다. 기술 의존성은 이 계층에만 존재한다

```
presentation → application(Service/Facade)
                    ↓ 의존
               domain(Reader/Manager 인터페이스)
                    ↑ 구현 (DIP 역전)
               infrastructure(ReaderImpl/ManagerImpl → JPA Repository)
```
- presentation: API 진입점. controller, Swagger docs 인터페이스, DTO를 포함한다. application에만 의존한다
- 다른 Context의 내부 클래스에 직접 접근하는 것은 금지한다. Context 간 통신은 application의 공개 메서드 또는 `ApplicationEvent`를 통한다

### Cross-Context 참조 규칙: Facade 패턴

Context 간 비즈니스 로직 조합이 필요할 때는 Facade를 사용한다.

- Domain Service가 타 Context의 Service나 Reader/Manager를 직접 참조하는 것은 금지한다

```
booking/
  application/
    ReservationFacade.java     ← 교차 컨텍스트 오케스트레이션
    ReservationService.java    ← 순수 booking 도메인 로직만
```

- Facade (`ReservationFacade`): `@Transactional` 없음. 읽기/검증/오케스트레이션만 담당한다. 다른 context의 Service를 호출하여 조합한다. Reader/Manager를 직접 호출하지 않는다. Facade는 비즈니스가 복잡하고 트랜잭션 분리가 필요한 경우에만 사용한다. 단순한 CRUD는 Controller → Service 직접 호출로 충분하다.
- Domain Service (`ReservationService`): `@Transactional`을 관리하는 유일한 계층. Reader/Manager를 내부적으로 호출한다.
- Reader/Manager: `@Transactional` 없음. 순수 데이터 접근 도구. Service를 통해서만 호출되며, Facade/Controller에서 직접 호출하지 않는다.

#### 호출 규칙

```
Controller → Facade → Service → Reader/Manager → Repository
                      (트랜잭션 관리)  (순수 데이터 접근)
```

- Reader/Manager는 트랜잭션을 소유하지 않는다. 호출한 Service의 트랜잭션에 참여한다.
- Facade/Controller가 Reader/Manager를 직접 호출하는 것은 금지한다. 반드시 Service를 경유한다.
- 이렇게 하면 트랜잭션 경계가 항상 Service 레벨에서 결정되어, 트랜잭션 전파 충돌이 발생하지 않는다.

#### Facade 트랜잭션 규칙

Facade에 `@Transactional`을 걸면 읽기/검증 구간까지 트랜잭션에 포함되어 커넥션 점유 시간이 길어진다.

- 특히 비관적 락을 사용하는 예약 플로우에서는 락 점유 시간이 불필요하게 늘어난다

따라서 다음 원칙을 따른다.

- Facade: `@Transactional` 없음. Service의 쓰기 트랜잭션이 독립적으로 동작한다
- Service (읽기): `@Transactional(readOnly = true)` — Read 커넥션 풀 라우팅 기준
- Service (쓰기): `@Transactional` — 원자적 쓰기 보장

```java
// Facade — 트랜잭션 없음, Service만 호출
public ReservationResponse create(CreateReservationCommand cmd) {
    // 1. 읽기/검증 — Service를 통해 조회 (Reader 직접 호출 X)
    RoomType roomType = propertyService.getActiveRoomType(cmd.roomTypeId());
    roomType.validateGuestCount(cmd.guestCount());

    // 2. Caffeine CAS 1차 필터링
    inventoryCache.tryDecreaseAll(cmd.roomTypeId(), cmd.dates());

    // 3. 원자적 쓰기는 단일 서비스에 위임
    return reservationService.createWithInventoryLock(cmd);
}

// Service (읽기) — Reader를 내부에서 호출
@Transactional(readOnly = true)
public RoomType getActiveRoomType(Long roomTypeId) {
    return roomTypeReader.getActiveById(roomTypeId); // Reader 직접 호출은 Service만 가능
}

// Service (쓰기) — Manager/Reader를 내부에서 호출
@Transactional
public ReservationResponse createWithInventoryLock(CreateReservationCommand cmd) {
    // 방어적 재검증 (TOCTOU 방지)
    RoomType roomType = roomTypeReader.getActiveById(cmd.roomTypeId());
    // 비관적 락 + 재고 차감
    List<Inventory> inventories = inventoryReader.findForUpdate(cmd.roomTypeId(), cmd.dates());
    inventoryManager.decreaseStock(inventories);
    // 예약 생성
    Reservation reservation = reservationManager.save(Reservation.create(cmd, inventories));
    eventPublisher.publish(new ReservationCreatedEvent(reservation.getId()));
    return ReservationResponse.from(reservation);
}

// Reader — 트랜잭션 없음, 순수 데이터 접근
public RoomType getActiveById(Long roomTypeId) {
    return roomTypeRepository.findByIdAndStatus(roomTypeId, Status.ACTIVE)
        .orElseThrow(() -> new NotFoundException(ErrorCode.ROOM_TYPE_NOT_FOUND));
}
```

이렇게 분리하면 세 가지 이점이 있다.

- 트랜잭션 경계가 명확해지고, 비관적 락 점유 시간을 최소화할 수 있다
- 나중에 도메인을 별도 모듈/서비스로 분리할 때 Facade만 수정하면 된다
- Domain Service는 자기 도메인에 대한 순수 로직만 담당하므로 그대로 가져갈 수 있다

---

## 2. 네이밍 규칙

### 엔티티 (Entity)

단수형을 사용한다.

- JPA 엔티티는 테이블과 1:1 대응되므로 복수형을 쓰면 읽기 어렵다

```java
// 올바름
@Entity public class Property { }
@Entity public class RoomType { }
@Entity public class Reservation { }
@Entity public class Partner { }

// 잘못됨
@Entity public class Properties { }
@Entity public class RoomTypes { }
```

### Repository

`{Entity}Repository` 패턴을 따른다. Spring Data JPA의 관례와 일치한다.

```java
public interface PropertyRepository extends JpaRepository<Property, Long> { }
public interface RoomTypeRepository extends JpaRepository<RoomType, Long> { }
public interface InventoryRepository extends JpaRepository<Inventory, Long> { }
public interface ReservationRepository extends JpaRepository<Reservation, Long> { }
```

### Service

`{Domain}Service` 패턴을 따른다.

- 도메인 이름은 Context 이름 또는 주요 엔티티 이름을 사용한다

```java
@Service public class PartnerService { }
@Service public class PropertyService { }
@Service public class RoomTypeService { }
@Service public class BookingService { }       // Booking Context의 핵심 서비스
@Service public class SearchService { }
@Service public class UserService { }
```

하나의 Context에 서비스가 여러 개 필요한 경우 엔티티 이름으로 세분화한다.


```java
@Service public class InventoryService { }    // property 패키지 내
@Service public class RateService { }         // property 패키지 내
```

### Controller

역할 접두사 + 도메인 + `Controller` 패턴을 사용한다.

- 이 규칙은 URL 구조와 자연스럽게 대응된다

| 역할 접두사 | 대상 사용자 | URL 접두사 |
|------------|-----------|-----------|
| `Extranet` | 파트너(사업자) | `/api/extranet/` |
| `Customer` | 일반 회원 | `/api/` |
| `Admin` | 운영자 | `/api/admin/` |

```java
// 파트너용 컨트롤러
@RestController public class ExtranetPropertyController { }
@RestController public class ExtranetRoomTypeController { }
@RestController public class ExtranetReservationController { }
@RestController public class ExtranetPartnerController { }

// 고객용 컨트롤러
@RestController public class CustomerSearchController { }
@RestController public class CustomerReservationController { }
@RestController public class CustomerUserController { }

// 운영자용 컨트롤러
@RestController public class AdminPartnerController { }
@RestController public class AdminPropertyController { }
```

### DTO 네이밍

| 계층 | Request | Response |
|------|---------|----------|
| API (Presentation) | `~~Request` | `~~Response` |
| Service (POST/PUT/DELETE) | `~~Command` | `~~Result` |
| Service (GET) | `~~Search` | `~~Result` |

범용 데이터 운반 DTO: `~~Carrier`

```java
// API 계층
public record CreateReservationRequest(Long roomTypeId, LocalDate checkIn, ...) {}
public record ReservationResponse(Long id, String reservationNumber, ...) {}

// Service 계층
public record CreateReservationCommand(Long roomTypeId, LocalDate checkIn, ...) {}
public record ReservationResult(Long id, String reservationNumber, ...) {}
public record ReservationSearch(Long userId, String status, Pageable pageable) {}
```

Record를 DTO로 선택한 이유는 불변성과 간결함이다.

- Java 21에서 record는 `equals`, `hashCode`, `toString`을 자동 생성한다
- setter가 없어 의도치 않은 변경을 방지한다

### 컴포넌트 네이밍

| 역할 | 접미사 | 설명 |
|------|--------|------|
| 읽기 전용 | `Reader` | 조회 로직 (SELECT) |
| 쓰기 (생성/수정/삭제) | `Manager` | 변경 로직 (INSERT/UPDATE/DELETE) |
| 비즈니스 검증 | `Validator` | 유효성 검사 로직 |
| 교차 컨텍스트 조합 | `Facade` | 여러 Service를 조합하는 오케스트레이터 |
| 외부 연동 | `Adapter` | 외부 API 호출 (Channel, Supplier) |

Reader/Manager 분리는 선택 사항이다.

- 도메인 복잡도가 높아 읽기/쓰기를 분리하는 것이 유리할 때 적용한다
- 단순한 CRUD 도메인에서는 `Service` 하나로 충분하다

---

## 3. API URL 규칙

### URL 구조

```
/api/{역할}/{도메인}/{id}/{하위리소스}
```

역할별 접두사가 URL에서 접근 권한을 명확히 드러낸다. Spring Security 설정에서도 URL 패턴으로 권한을 제어하므로 일관성이 중요하다.

### 인증 여부에 따른 URL 구분

인증이 필요 없는 엔드포인트는 `/api/public/` 접두사를 사용한다. 이 규칙은 Spring Security 설정에서 `permitAll()` 대상을 `/api/public/` 패턴 하나로 통일할 수 있게 한다.

| 구분 | URL 패턴 | 예시 |
|------|----------|------|
| 비인증 (공개) | `/api/public/...` | 로그인, 회원가입, 숙소 검색, 요금 조회 |
| 인증 필요 | `/api/...` | 예약 생성/취소, Extranet 관리, 내 정보 조회 |

공개 엔드포인트 URL 구조:

```
/api/public/{역할}/{도메인}/{id}/{하위리소스}
```

### Extranet API (파트너용)

```
POST   /api/public/extranet/auth/login    ← 인증 불필요 (공개)
POST   /api/extranet/partners
GET    /api/extranet/partners/me
PUT    /api/extranet/partners/me

POST   /api/extranet/properties
GET    /api/extranet/properties
GET    /api/extranet/properties/{id}
PUT    /api/extranet/properties/{id}
PATCH  /api/extranet/properties/{id}/status

POST   /api/extranet/properties/{id}/room-types
GET    /api/extranet/properties/{id}/room-types
PUT    /api/extranet/room-types/{id}

PUT    /api/extranet/room-types/{id}/rates        ← 날짜 범위 요금 일괄 설정
GET    /api/extranet/room-types/{id}/rates
PUT    /api/extranet/room-types/{id}/inventory    ← 날짜 범위 재고 일괄 설정
GET    /api/extranet/room-types/{id}/inventory

GET    /api/extranet/reservations
GET    /api/extranet/reservations/{id}
```

### Customer API (고객용)

```
POST   /api/public/users/signup           ← 인증 불필요 (공개)
POST   /api/public/users/login            ← 인증 불필요 (공개)
GET    /api/users/me

GET    /api/public/search/properties              ← 숙소 검색 (지역, 날짜, 인원), 인증 불필요 (공개)
GET    /api/public/search/properties/{id}/rates   ← 요금 조회, 인증 불필요 (공개)

GET    /api/public/properties/{id}               ← 숙소 상세 (객실 포함), 인증 불필요 (공개)

POST   /api/reservations                  ← 예약 생성
GET    /api/reservations
GET    /api/reservations/{id}
POST   /api/reservations/{id}/cancel
```

### Admin API (운영자용)

```
GET    /api/admin/partners
PATCH  /api/admin/partners/{id}/status
GET    /api/admin/properties
GET    /api/admin/reservations
GET    /api/admin/channels
GET    /api/admin/suppliers
```

### URL 네이밍 원칙

- 소문자 케밥케이스(kebab-case)를 사용한다. `roomTypes` 대신 `room-types`.
- 리소스는 복수형 명사를 사용한다. `/properties`, `/reservations`.
- 동사는 HTTP 메서드로 표현한다. `POST /reservations`는 예약 생성이고, `DELETE /reservations/{id}` 대신 `POST /reservations/{id}/cancel`로 상태 전이를 명시한다.
- 상태 변경은 `PATCH /{id}/status` 또는 `POST /{id}/{action}` 패턴을 사용한다.

---

## 4. 응답 형식

모든 API 응답은 `ApiBaseResponse<T>` 래퍼로 감싼다. 클라이언트가 응답 구조를 일관되게 처리할 수 있도록 하기 위함이다.

```java
// common/response/ApiBaseResponse.java
public record ApiBaseResponse<T>(
        ResultType result,
        T data,
        ErrorMessage error
) {
    public static <T> ApiBaseResponse<T> success(T data) {
        return new ApiBaseResponse<>(ResultType.SUCCESS, data, null);
    }

    public static ApiBaseResponse<Void> success() {
        return new ApiBaseResponse<>(ResultType.SUCCESS, null, null);
    }

    public static ApiBaseResponse<?> error(ErrorCode errorCode, String message) {
        return new ApiBaseResponse<>(ResultType.ERROR, null, new ErrorMessage(errorCode, message));
    }
}

// common/response/ResultType.java
public enum ResultType { SUCCESS, ERROR }

// common/response/ErrorMessage.java
public record ErrorMessage(String code, String message) {
    public ErrorMessage(ErrorCode errorCode, String message) {
        this(errorCode.name(), message);
    }
}
```

```java
// 페이지네이션 응답 (common/response/PageResult.java)
// Service에서 Page -> PageResult 변환 후 반환. Controller에 Page 객체를 내리지 않는다.
public record PageResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static <T> PageResult<T> from(Page<T> page) { ... }
    public <R> PageResult<R> map(Function<T, R> mapper) { ... }
}
```

응답 예시:

```json
// 성공
{
  "result": "SUCCESS",
  "data": {
    "id": 1,
    "name": "한강뷰 호텔",
    "region": "서울"
  },
  "error": null
}

// 실패
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "INVENTORY_INSUFFICIENT",
    "message": "선택한 날짜에 객실이 매진되었습니다."
  }
}
```

---

## 5. 예외 처리

### 구조

```
ApiControllerAdvice (common/exception/)
    ├── NotFoundException → 404 (log.info)
    ├── BusinessException → 400/409 (log.warn)
    ├── AuthenticationException → 401 (log.info)
    ├── AuthorizationException → 403 (log.info)
    ├── MethodArgumentNotValidException → 400 (log.info)
    └── Exception → 500 (log.error)
```

### 예외 계층

공통 베이스 클래스에서 ErrorCode enum을 강제한다. 도메인별 예외를 만들지 않고, ErrorCode로 구분한다.

```java
// common/exception/BusinessException.java
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String detail) {
        super(detail);
        this.errorCode = errorCode;
    }
}

// common/exception/NotFoundException.java
public class NotFoundException extends BusinessException {
    public NotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}

// common/exception/AuthenticationException.java (커스텀, Spring Security 것 아님)
// common/exception/AuthorizationException.java
```

사용 예시:

```java
throw new NotFoundException(ErrorCode.PROPERTY_NOT_FOUND);
throw new BusinessException(ErrorCode.INVENTORY_INSUFFICIENT);
throw new BusinessException(ErrorCode.DUPLICATE_RESERVATION, "2026-04-01 중복");
```

### GlobalExceptionHandler

```java
// common/exception/GlobalExceptionHandler.java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(e.getMessage(), e.getErrorCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(message, "VALIDATION_ERROR"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e) {
        return ResponseEntity.internalServerError()
            .body(ApiResponse.error("서버 오류가 발생했습니다.", "INTERNAL_SERVER_ERROR"));
    }
}
```

에러 코드는 `{DOMAIN}_{PROBLEM}` 형식으로 정의한다. 클라이언트가 에러 코드로 분기 처리할 수 있도록 의미 있는 코드를 사용한다.

| 에러 코드 | 상황 |
|----------|------|
| `INVENTORY_NOT_ENOUGH` | 예약 시 재고 부족 |
| `PROPERTY_NOT_FOUND` | 숙소 ID 없음 |
| `ROOM_TYPE_NOT_FOUND` | 객실 유형 ID 없음 |
| `RESERVATION_NOT_FOUND` | 예약 ID 없음 |
| `PARTNER_ACCESS_DENIED` | 타 파트너 소유 숙소 접근 |
| `ALREADY_CANCELLED` | 이미 취소된 예약 재취소 시도 |
| `VALIDATION_ERROR` | 요청 파라미터 유효성 오류 |

---

## 6. Git 커밋 메시지 규칙

Conventional Commits 사양을 따른다. 커밋 메시지만 보고도 변경 내용의 성격을 파악할 수 있도록 한다.

### 형식

```
{type}({scope}): {description}

[optional body]
[optional footer]
```

### 타입 목록

| 타입 | 용도 |
|------|------|
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `docs` | 문서 작성·수정 |
| `refactor` | 동작 변경 없는 코드 개선 |
| `test` | 테스트 코드 추가·수정 |
| `chore` | 빌드 설정, 의존성 변경 |
| `perf` | 성능 개선 |

### 스코프 예시

스코프는 Bounded Context 이름을 사용한다.

```
feat(property): 날짜 범위 요금 일괄 설정 API 구현
feat(booking): 비관적 락 기반 예약 생성 구현
fix(inventory): 체크아웃 날짜 재고 차감 범위 오류 수정
test(booking): 동시 예약 요청 동시성 테스트 추가
refactor(search): Caffeine 캐시 키 전략 하위 단위로 변경
docs(overview): 아키텍처 다이어그램 추가
chore: Testcontainers MySQL 의존성 추가
```

### 커밋 단위 원칙

- 하나의 커밋은 하나의 논리적 변경을 담는다.
- "그리고"로 연결되는 커밋 메시지는 분리 대상이다.
- 테스트 코드는 프로덕션 코드와 같은 커밋에 포함하는 것을 권장한다.

---

## 7. 코드 스타일

### 기본 원칙

- Google Java Style Guide를 기반으로 하되, 들여쓰기는 4 스페이스를 사용한다. (intellij-java-google-style.xml 참고)
- 한 줄에 충분히 들어가는 코드는 불필요하게 줄바꿈하지 않는다 (Google Style 200자 기준)
- `final` 변수를 선호한다. 메서드 파라미터, 지역 변수에 가능한 한 `final`을 붙인다. 재할당이 필요하지 않음을 명시적으로 표현한다.

### 메서드 설계 원칙

- Tell Don't Ask: 객체에게 판단을 위임한다. 외부에서 상태를 꺼내 판단하지 않고, 객체의 메서드를 호출한다 (예: `property.validateOwner(partnerId)`)
- Single Level of Abstraction + Composed Method Pattern: 하나의 메서드는 하나의 추상화 수준만 가진다
- Extract Method: 복합 로직은 private 메서드로 추출하여 메인 메서드의 추상화 수준을 통일한다
- 정적 팩토리 메서드: builder는 내부에서만, 비즈니스 로직에서는 `Reservation.create(cmd, ...)` 호출

### record 스타일

- record 컴포넌트가 3개 이상이면 각 줄로 분리한다 (Put record components on separate lines)

```java
// 3개 이상: 줄 분리
public record JwtPrincipal(
        Long subjectId,
        String role,
        String context
) {
}

// 2개 이하: 한 줄 허용
public record UserId(Long value) {
}
```

```java
// 선호
public ReservationResponse createReservation(
        final CreateReservationRequest request,
        final Long userId) {
    final RoomType roomType = roomTypeRepository.findById(request.roomTypeId())
        .orElseThrow(() -> new RoomTypeNotFoundException(request.roomTypeId()));
    // ...
}

// 지양
public ReservationResponse createReservation(
        CreateReservationRequest request,
        Long userId) {
    RoomType roomType = roomTypeRepository.findById(request.roomTypeId())
        .orElseThrow(() -> new RoomTypeNotFoundException(request.roomTypeId()));
    // ...
}
```

### 불변 객체 선호

엔티티의 상태 변경은 메서드를 통해 명시적으로 표현한다. 무분별한 setter는 도메인 로직이 서비스 레이어로 누출되는 원인이 된다.

```java
// 선호: 도메인 메서드로 상태 변경
@Entity
public class Reservation {

    private ReservationStatus status;
    private LocalDateTime cancelledAt;

    public void cancel() {
        if (this.status == ReservationStatus.CANCELLED) {
            throw new AlreadyCancelledException();
        }
        this.status = ReservationStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }
}

// 지양: setter로 외부에서 상태 직접 변경
reservation.setStatus(ReservationStatus.CANCELLED);
reservation.setCancelledAt(LocalDateTime.now());
```

### Lombok 사용 지침

| 어노테이션 | 사용 여부 | 이유 |
|-----------|---------|------|
| `@Getter` | 허용 | 반복 코드 제거 |
| `@Setter` | 지양 | 불변 객체 원칙 |
| `@Builder` | 허용 | 객체 생성 가독성 |
| `@NoArgsConstructor(access = PROTECTED)` | 허용 | JPA 요건, 외부 직접 생성 방지 |
| `@AllArgsConstructor` | 지양 | 필드 순서 변경 시 컴파일 오류 미감지 위험 |
| `@Data` | 금지 | equals/hashCode가 JPA 엔티티에서 문제 유발 |

---

## 8. 테스트 네이밍

### 한글 메서드명 허용

테스트 메서드는 한글 이름을 사용한다. 이 결정은 다소 논쟁적일 수 있다. 영문 메서드명이 표준처럼 느껴지기 때문이다. 그러나 테스트의 목적은 시나리오를 명확하게 표현하는 것이고, 한글로 작성하면 비즈니스 규칙을 그대로 코드로 옮길 수 있다는 장점이 있다.

```java
// 선호
@Test
void 동시_예약_요청_시_재고_초과_방지() { }

@Test
void 체크아웃_날짜_재고는_차감하지_않는다() { }

@Test
void 이미_취소된_예약을_다시_취소하면_예외가_발생한다() { }

@Test
void 파트너가_타_파트너_숙소에_접근하면_예외가_발생한다() { }

// 지양
@Test
void testConcurrentReservation() { }

@Test
void cancelAlreadyCancelledReservation_throwsException() { }
```

### 테스트 구조: Given-When-Then

```java
@Test
void 재고_1개_객실에_100명이_동시_예약하면_1건만_성공한다() {
    // Given
    final int totalInventory = 1;
    final int concurrentRequests = 100;
    // 재고 설정, 테스트 데이터 준비

    // When
    final List<Future<ReservationResult>> futures = submitConcurrentReservations(concurrentRequests);
    final List<ReservationResult> results = collectResults(futures);

    // Then
    final long successCount = results.stream()
        .filter(ReservationResult::isSuccess)
        .count();
    assertThat(successCount).isEqualTo(totalInventory);

    final Inventory inventory = inventoryRepository.findByRoomTypeIdAndDate(roomTypeId, checkInDate);
    assertThat(inventory.getReservedCount()).isEqualTo(totalInventory);
}
```

### 테스트 클래스 분류

| 접미사 | 의미 | 환경 |
|--------|------|------|
| `Test` | 단위 테스트 | JVM (Mockito) |
| `IntegrationTest` | 통합 테스트 | Spring Context + H2 |
| `ConcurrencyTest` | 동시성 테스트 | Testcontainers + MySQL |

동시성 테스트에 H2를 쓰지 않는 이유는 H2의 `SELECT FOR UPDATE` 동작이 MySQL과 다르기 때문이다. 비관적 락 테스트는 반드시 실제 MySQL(Testcontainers)에서 실행해야 의미 있는 결과를 얻을 수 있다.

```java
// 동시성 테스트 예시
@SpringBootTest
@Testcontainers
class ReservationConcurrencyTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("stayhost_test");

    @Test
    void 동시_예약_요청_시_재고_초과_방지() {
        // 100개 스레드, ExecutorService, CountDownLatch 활용
    }

    @Test
    void 멀티나이트_예약_시_데드락이_발생하지_않는다() {
        // 서로 다른 날짜 순서로 예약 시도하는 두 트랜잭션
    }
}
```

---

## 9. 설정 파일 구조

환경별 설정은 Spring Profile로 분리한다.

```
src/main/resources/
├── application.yml              ← 공통 설정
├── application-local.yml        ← 로컬 개발 환경
├── application-test.yml         ← 테스트 환경
└── application-prod.yml         ← 운영 환경 (민감 정보는 환경 변수)
```

```yaml
# application.yml (공통)
spring:
  jpa:
    open-in-view: false          # OSIV 비활성화: 트랜잭션 범위를 서비스 레이어로 제한
    hibernate:
      ddl-auto: validate         # 운영 환경에서 스키마 자동 변경 방지

app:
  cache:
    property-ttl: 600            # 10분
    rate-ttl: 180                # 3분
    property-max-size: 5000
    rate-max-size: 30000
```

`open-in-view: false`는 의도적인 선택이다. OSIV가 활성화되면 트랜잭션이 닫힌 후에도 영속성 컨텍스트가 열려 있어 컨트롤러 레이어에서 지연 로딩이 발생할 수 있다. 이는 N+1 문제를 숨기고 예측하기 어려운 쿼리를 만든다. 비활성화하면 서비스 레이어 안에서 필요한 데이터를 모두 로딩해야 하며, 이 제약이 오히려 성능 문제를 조기에 발견하게 만든다.

---

## 10. 인증 정보 주입: 래퍼 타입 + ArgumentResolver

Controller에서 `@AuthenticationPrincipal`을 직접 사용하지 않는다. 래퍼 타입(`UserId`, `PartnerId`) + `HandlerMethodArgumentResolver`로 분리한다.

- Controller는 Spring Security 구현을 모른다. 필요한 값만 래퍼 타입으로 받는다
- 인증 방식 변경 시 Resolver만 수정하면 Controller는 그대로
- 래퍼 타입이 null이면 비인증 상태. 공개 API에서 null 허용 가능
- 정적 팩토리 메서드(`UserId.of()`)로 생성

```java
// Controller
@PostMapping("/api/reservations")
public ApiBaseResponse<ReservationResponse> create(
        @RequestBody CreateReservationRequest request,
        UserId userId) { ... }

// 래퍼 타입
public record UserId(Long value) {
    public static UserId of(Long value) { return new UserId(value); }
}
```

상세: [14-security-design.md](14-security-design.md) 2.5절

---

## 11. 코딩 원칙

### Tell, Don't Ask

객체의 상태를 꺼내서 외부에서 판단하지 않는다. 객체에게 행동을 요청한다.

```java
// Bad — 상태를 꺼내서 외부에서 판단
if (reservation.getStatus().equals("CANCELLED")) {
    throw new BusinessException(ErrorCode.RESERVATION_ALREADY_CANCELLED);
}

// Good — 객체에게 판단을 위임
reservation.validateCancellable(); // 내부에서 상태 확인 + 예외 throw
```

### Single Level of Abstraction (SLA)

하나의 메서드 안에서 추상화 수준을 일정하게 유지한다. 고수준 흐름과 저수준 구현을 섞지 않는다.

```java
// Bad — 추상화 수준 혼재
public ReservationResult create(CreateReservationCommand cmd) {
    RoomType roomType = roomTypeRepository.findById(cmd.roomTypeId())
        .orElseThrow(() -> new NotFoundException(ErrorCode.ROOM_TYPE_NOT_FOUND));
    if (cmd.guestCount() > roomType.getMaxOccupancy()) {
        throw new BusinessException(ErrorCode.INVALID_GUEST_COUNT);
    }
    List<Inventory> inventories = inventoryRepository.findForUpdate(cmd.roomTypeId(), cmd.dates());
    inventories.forEach(inv -> inv.decrease());
    Reservation reservation = Reservation.create(cmd, roomType, calculateTotalPrice(inventories));
    reservationRepository.save(reservation);
    return ReservationResult.from(reservation);
}

// Good — Composed Method Pattern
public ReservationResult create(CreateReservationCommand cmd) {
    RoomType roomType = getRoomType(cmd.roomTypeId());
    roomType.validateGuestCount(cmd.guestCount());
    List<Inventory> inventories = lockAndDecreaseInventory(cmd.roomTypeId(), cmd.dates());
    Reservation reservation = createReservation(cmd, roomType, inventories);
    return ReservationResult.from(reservation);
}
```

### 정적 팩토리 메서드 패턴

비즈니스 로직 내에서 `builder()` 호출이나 `new` 생성자 호출은 흐름을 방해한다. 정적 팩토리 메서드로 생성 로직을 캡슐화하고, builder는 정적 팩토리 메서드 내부에서만 사용한다.

| 용도 | 메서드명 | 설명 |
|------|----------|------|
| 객체 생성 | `create` | 도메인 객체 생성 (내부에서 builder 사용) |
| 간단한 래퍼 | `of` | 값 객체, 래퍼 타입 생성 (`UserId.of(1L)`) |
| Result → Response 변환 | `mapToResponse` | Service Result를 API Response로 변환 |
| Entity → Result 변환 | `from` | Entity에서 Result 생성 |

```java
// Good — 비즈니스 로직에서는 정적 팩토리 메서드만 호출
Reservation reservation = Reservation.create(command, roomType, totalPrice);
ReservationResponse response = ReservationResponse.mapToResponse(result);
ReservationResult result = ReservationResult.from(reservation);

// Good — 정적 팩토리 메서드 내부에서 builder 사용
public class Reservation {
    public static Reservation create(CreateReservationCommand cmd,
                                     RoomType roomType,
                                     BigDecimal totalPrice) {
        return Reservation.builder()
            .userId(cmd.userId())
            .roomTypeId(roomType.getId())
            .checkInDate(cmd.checkIn())
            .checkOutDate(cmd.checkOut())
            .status(ReservationStatus.CONFIRMED)
            .confirmedAt(LocalDateTime.now())
            .totalPrice(totalPrice)
            .build();
    }
}

// Bad — 비즈니스 로직에서 builder/new 직접 사용
Reservation reservation = Reservation.builder()
    .userId(command.userId())
    .roomTypeId(command.roomTypeId())
    .status("CONFIRMED")
    .build();
```

### 상수 사용

하드코딩 대신 상수(`Constants`) 또는 `enum`을 사용한다.

```java
// Bad
if (status.equals("CONFIRMED")) { ... }
Thread.sleep(300000); // 5분?

// Good
if (status == ReservationStatus.CONFIRMED) { ... }
Thread.sleep(PENDING_TIMEOUT_MILLIS);
```

### 파라미터 포매팅

파라미터가 2개 이상인 메서드는 각 파라미터를 별도 줄에 작성한다.

```java
// Good
Optional<Inventory> findByRoomTypeIdAndDate(
    @Param("roomTypeId") Long roomTypeId,
    @Param("date") LocalDate date
);

// Bad
Optional<Inventory> findByRoomTypeIdAndDate(@Param("roomTypeId") Long roomTypeId, @Param("date") LocalDate date);
```
