# ADR-001: 모놀리식 + 패키지 분리 아키텍처 채택

## 상태

Accepted (2026-03-28)

---

## 배경

이 플랫폼은 Partner, Property, Booking, Search, Channel Manager, Supplier 등 최소 6개의 명확히 구분되는 Bounded Context를 포함하는 OTA 도메인이다.

아키텍처를 결정할 때 두 가지 상충하는 목표가 있었다.

1. 구현 완성도: 동시성 처리, 캐시 전략, 예약 플로우가 실제로 동작하는 코드로 증명되어야 한다.
2. 아키텍처 깊이: 도메인 경계가 명확하게 표현되어야 하고, 향후 확장 가능한 구조여야 한다.

---

## 결정

단일 Spring Boot 애플리케이션 + 도메인별 패키지 분리 구조를 채택한다.

패키지 구조는 다음과 같이 Bounded Context를 반영한다.

```
com.jemini.stayhost
├── partner/          # Partner Context
├── user/             # User Context
├── property/         # Property Context (숙소, 객실, 요금, 재고)
├── booking/          # Booking Context (예약, 취소)
├── search/           # Search Context (검색, 캐시)
├── customer/         # Customer Context (회원, 인증)
├── channel/          # Channel Manager Context (DESIGN-ONLY)
└── supplier/         # Supplier Context (DESIGN-ONLY, 매핑 테이블은 BUILD)
```

각 Context 내부는 `domain`, `application`, `infrastructure`, `api` 레이어로 구성하여 레이어드 아키텍처의 의존성 방향을 유지하지만, 인프라 Layer 는 DIP 를 적용한다.

---

## 대안
- Option A: 멀티 모듈 Gradle
  - 장점
    - 컴파일 타임에 모듈 간 의존성을 강제할 수 있다.
  - 단점
    - 하지만, 초기 설정 비용이 상당하다.
- Option B: MSA (마이크로서비스)
  - 장점
    - 독립 배포, 독립 스케일링, 장애 격리 등 장점이 있다.
  - 단점
    - 인프라 복잡도가 수십 배 증가, 장애에 대한 격리 견고하게 처리 해야된다.
    - 인프라 구성에 더 많은 시간을 쓰게 된다.

## 기대 결과
- 초기 구축이 빠르다.
- 도메인 경계가 패키지로 명확히 표현되어 설계 의도가 드러난다.
- 향후 아키텍처가 변경될 수 있기에 패키지 및 레이어 별 의존성 규칙을 엄격히 지켜야 한다.