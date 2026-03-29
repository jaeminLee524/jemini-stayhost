# 구현 타임라인

> 마감: 4/2 수 23:59

## 일정

| Day | 날짜 | 요일 | 작업 |
|-----|------|------|------|
| 1-2 | 3/28-29 | 금토 | 설계 문서 20개 + 리뷰/보강 (완료) |
| 3 | 3/29 | 토 | 프로젝트 셋업 + Extranet API + Customer API |
| 4 | 3/30 | 일 | 동시성(CAS+비관적 락) + Supplier 동기화 + API 마무리 |
| 5 | 3/31 | 월 | 테스트 + Channel/Supplier Mock + 시딩 + Swagger + README |
| 버퍼 | 4/1 | 화 | 최종 점검 + 보완 |
| 제출 | 4/2 | 수 | 23:59 마감 |

## 구현 스텝 (순서대로)

Day 3 — 프로젝트 셋업 + API 구현:
1. 공통: ApiBaseResponse, ResultType, ErrorMessage
2. 공통: ErrorCode enum, BusinessException, NotFoundException, Auth 예외
3. 공통: ApiControllerAdvice
4. 공통: JWT (JwtProvider, JwtAuthenticationFilter, JwtPrincipal, SecurityConfig)
5. 공통: 래퍼 타입 (UserId, PartnerId) + ArgumentResolver
6. 공통: MdcLoggingFilter, AOP 요청/응답 로깅
7. Entity 전체 (Partner, Property, PropertyImage, RoomType, Rate, Inventory, User, Reservation, ReservationDailyRate)
8. Flyway DDL: V1__init.sql
9. Docker Compose 기동 확인
10. Extranet: 파트너 등록/로그인
11. Extranet: 숙소 CRUD + 상태 변경
12. Extranet: 객실 유형 CRUD
13. Extranet: 요금 bulk 설정/조회
14. Extranet: 재고 bulk 설정/조회
15. Customer: 회원가입/로그인
16. Customer: 숙소 검색 (지역/이름, 페이징)
17. Customer: 숙소 상세 + 요금 조회
18. Customer: 예약 생성 (기본)
19. Customer: 예약 취소
20. Customer: 내 예약 목록/상세
21. Extranet: 예약 목록/상세 조회

Day 4 — 동시성 + Supplier + 마무리:
22. Caffeine 캐시 설정 (property, roomType, rate)
23. 캐시 무효화 이벤트 (@TransactionalEventListener)
24. InventoryCache (Caffeine CAS, @PostConstruct 워밍업)
25. 예약 생성에 Caffeine CAS 1차 필터링
26. 예약 생성에 DB 비관적 락 (SELECT FOR UPDATE)
27. 예약 취소에 WHERE status='CONFIRMED' + affected rows
28. TOCTOU 방어적 재검증
29. DB 커넥션 풀 분리 (AbstractRoutingDataSource)
30. Supplier: SupplierAdapter + MockSupplierAdapter
31. Supplier: 수동 동기화 API → property 테이블 저장
32. 통합 검색에 Supplier 상품 노출 확인
33. 전체 API Swagger 확인

Day 5 — 테스트 + 마무리:
34. Testcontainers 동시성 테스트 (100:1, 50:10)
35. Channel: ChannelAdapter + MockChannelAdapter + CompletableFuture 병렬
36. 이벤트 연결: ReservationCreatedEvent → ChannelManager
37. k6 부하 테스트 스크립트
38. Flyway 시딩 데이터
39. Swagger Docs 인터페이스 분리 (GroupedOpenApi)
40. README.md 최종
41. 코드 정리
42. journal/ai-usage 최종 갱신
