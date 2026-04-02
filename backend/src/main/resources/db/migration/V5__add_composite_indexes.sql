-- =============================================================
-- V5: 복합 인덱스 교체 + 누락 인덱스 추가 + 파트너 기본 상태 변경
--
-- 목적: 실제 쿼리 패턴에 맞는 복합 인덱스로 교체하여
--       MySQL leftmost prefix rule을 활용한 조회 성능 개선
-- =============================================================

-- -----------------------------------------------
-- 1. property: (region), (status) → (status, region)
--    - searchActive 쿼리에서 status는 항상 존재, region은 선택적
--    - leftmost prefix로 status-only 조회도 커버
-- -----------------------------------------------
DROP INDEX idx_property_region ON property;
DROP INDEX idx_property_status ON property;
CREATE INDEX idx_property_status_region ON property (status, region);

-- -----------------------------------------------
-- 2. room_type: (property_id) → (property_id, status)
--    - findByPropertyId: leftmost prefix 커버
--    - findByPropertyIdAndStatus: 복합 인덱스 정확 매칭
--    - findByPropertyIdInAndStatus: IN + equality 커버
-- -----------------------------------------------
DROP INDEX idx_room_type_property_id ON room_type;
CREATE INDEX idx_room_type_property_status ON room_type (property_id, status);

-- -----------------------------------------------
-- 3. reservation: (user_id) → (user_id, status)
--    - findByUserId: leftmost prefix 커버
--    - findByUserIdAndStatus: 복합 인덱스 정확 매칭
-- -----------------------------------------------
DROP INDEX idx_reservation_user_id ON reservation;
CREATE INDEX idx_reservation_user_status ON reservation (user_id, status);

-- -----------------------------------------------
-- 4. reservation: (property_id), (status), (room_type_id) →
--    (property_id, status, check_in_date)
--    - findByPropertyIdIn: leftmost prefix 커버
--    - findByPropertyIdsWithFilters: 3컬럼 복합 활용
--    - idx_reservation_room_type_id: 사용하는 쿼리 없음 (orphan)
-- -----------------------------------------------
DROP INDEX idx_reservation_property_id ON reservation;
DROP INDEX idx_reservation_status ON reservation;
DROP INDEX idx_reservation_room_type_id ON reservation;
CREATE INDEX idx_reservation_property_status_checkin ON reservation (property_id, status, check_in_date);

-- -----------------------------------------------
-- 5. channel_property_mapping: 신규 인덱스
--    - findByPropertyIdAndStatus 쿼리 지원
--    - UK(channel_id, property_id)는 property_id 선행 조회 불가
-- -----------------------------------------------
CREATE INDEX idx_cpm_property_status ON channel_property_mapping (property_id, status);

-- -----------------------------------------------
-- 6. channel_room_mapping: 신규 FK 인덱스
--    - findByChannelPropertyMappingId 쿼리 지원
-- -----------------------------------------------
CREATE INDEX idx_crm_cpm_id ON channel_room_mapping (channel_property_mapping_id);

-- -----------------------------------------------
-- 7. partner: 기본 상태를 PENDING → ACTIVE로 변경
--    - TODO: 관리자 승인 플로우 도입 시 PENDING으로 복원
-- -----------------------------------------------
ALTER TABLE partner ALTER COLUMN status SET DEFAULT 'ACTIVE';
