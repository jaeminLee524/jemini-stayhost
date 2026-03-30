## Day 3 - 동시성 + Supplier + Channel + 코드 품질 (3/30~31)

### 수행 내용

동시성 제어:
- CAS 재고 캐시(InventoryCache) + ReservationFacade 2단계 필터링 구조 완성
- 예약 취소 WHERE status='CONFIRMED' + affected rows 동시성 방어
- JPQL 상태값 하드코딩을 enum 파라미터로 변경
- DB 커넥션 풀 분리 (AbstractRoutingDataSource: primary 10 / read 20)
- CAS 캐시 miss 시 보수적 차단 정책 주석 문서화

Supplier 연동:
- V2 마이그레이션 (supplier, supplier_property, supplier_property_mapping, supplier_sync_job)
- SupplierAdapter 인터페이스 + MockSupplierAdapter (가상 데이터 반환)
- SupplierSyncService 동기화 오케스트레이션 (어댑터 탐색 → fetch → upsert)
- SyncJob REQUIRES_NEW 독립 트랜잭션으로 FAILED 상태 유실 방지
- POST /api/suppliers/{supplierId}/sync 수동 동기화 API
- SupplierSyncService 단위 테스트 5개

Channel Manager:
- V3 마이그레이션 (channel, channel_property_mapping, channel_room_mapping, channel_rate_policy, channel_sync_log)
- ChannelAdapter 인터페이스 + MockChannelAdapter
- ChannelManagerService: CompletableFuture + channelExecutor 병렬 푸시
- ChannelSyncListener: InventoryChangedEvent/ReservationCreatedEvent → 채널 동기화 트리거
- ChannelRatePolicy.applyMarkup() 마크업 계산 (PERCENTAGE/FIXED)
- ChannelManagerService 단위 테스트 7개 (부분 실패, 빈 어댑터 등 엣지 케이스 포함)

코드 품질 개선:
- DateUtil 유틸 생성 (dayCountInclusive, dateRangeInclusive) + 5개 파일 중복 제거
- findProperties() StringBuilder JPQL → 정적 JPQL (:param IS NULL OR) 패턴 개선
- step-down rule 7개 파일 메서드 순서 정리
- static import 일괄 적용 8개 파일 (Collectors, DateUtil, StringUtils, hasText)
- 섹션 구분 주석 9개 제거
- isNotBlank() 적용 (null+blank 체크 통일)
- buildDetailResult() 내 stream 로직 private method 추출 (toImageEntries, toRoomTypeEntries)
- DDL FK 제약조건 전체 제거 (V1, V2, V3)
- Swagger @Profile 제거 + Supplier/Channel API 그룹 추가
- docker-compose 읽기/쓰기 풀 분리 환경변수 대응
- ReservationFacade Javadoc 단일 인스턴스 전제/다중 인스턴스 전환 의도 반영

### 의사결정
- [CAS 캐시 miss → 보수적 차단]: 캐시에 키가 없으면 DB에 재고가 있어도 차단. false negative(판매 기회 손실)는 허용하되 false positive(없는 재고 판매)는 절대 방지. 추후 캐시 miss 시 DB fallback 전환 가능
- [SyncJob REQUIRES_NEW]: 동기화 실패 시 outer 트랜잭션 롤백과 독립적으로 FAILED 상태를 보존해야 하므로 별도 트랜잭션. saveSyncJob에만 적용하여 영향 범위 최소화
- [SupplierAdapter → domain.component]: 헥사고날의 port 패턴 대신 프로젝트 기존 구조(Reader/Manager와 같은 위치)에 맞춰 domain.component 배치. DTO는 domain.dto
- [FK 제약조건 전체 제거]: 애플리케이션 레벨에서 참조 무결성 관리. FK로 인한 데드락, 마이그레이션 제약 방지
- [findProperties 정적 JPQL]: StringBuilder 동적 빌드 대신 (:param IS NULL OR) 패턴. 현재 규모에서 인덱스 활용 저하는 무시 가능, 대규모 시 Specification 전환 검토
- [Channel 부분 실패 허용]: N개 채널 중 일부 실패 시 성공 채널은 유지, 실패만 로그. 되돌리면 오히려 불일치 확대. eventual consistency로 해결
