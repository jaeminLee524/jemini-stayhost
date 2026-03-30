-- Supplier Context
CREATE TABLE supplier (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '공급자 ID',
    name            VARCHAR(200)    NOT NULL               COMMENT '공급자명',
    code            VARCHAR(30)     NOT NULL               COMMENT '공급자 코드',
    api_base_url    VARCHAR(500)    NULL                   COMMENT 'API 엔드포인트',
    api_key         VARCHAR(255)    NULL                   COMMENT '인증키',
    sync_type       VARCHAR(20)     NOT NULL DEFAULT 'PULL' COMMENT '동기화 방식(PULL/PUSH)',
    sync_interval   INT                      DEFAULT 3600  COMMENT '동기화 주기(초)',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT '상태(ACTIVE/INACTIVE)',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id),
    UNIQUE KEY uk_supplier_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='공급자';

CREATE TABLE supplier_property (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '공급자 숙소 ID',
    supplier_id             BIGINT          NOT NULL               COMMENT '공급자 ID (FK)',
    external_property_id    VARCHAR(100)    NOT NULL               COMMENT '공급자 측 숙소 ID',
    raw_data                JSON            NOT NULL               COMMENT '공급자 원본 데이터',
    last_synced_at          DATETIME        NULL                   COMMENT '마지막 동기화 일시',
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id),
    UNIQUE KEY uk_supplier_property (supplier_id, external_property_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='공급자 숙소';

CREATE TABLE supplier_property_mapping (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '매핑 ID',
    supplier_property_id    BIGINT          NOT NULL               COMMENT '공급자 숙소 ID (FK)',
    property_id             BIGINT          NOT NULL               COMMENT '자사 숙소 ID (FK)',
    mapping_status          VARCHAR(20)     NOT NULL DEFAULT 'UNMAPPED' COMMENT '매핑 상태(MAPPED/UNMAPPED/CONFLICT)',
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id),
    UNIQUE KEY uk_supplier_mapping (supplier_property_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='공급자 숙소 매핑';

CREATE TABLE supplier_sync_job (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '작업 ID',
    supplier_id     BIGINT          NOT NULL               COMMENT '공급자 ID (FK)',
    job_type        VARCHAR(30)     NOT NULL               COMMENT '작업 유형(FULL_SYNC/INCREMENTAL/RATE_UPDATE/INVENTORY_UPDATE)',
    status          VARCHAR(20)     NOT NULL               COMMENT '작업 상태(RUNNING/COMPLETED/FAILED)',
    total_count     INT             NOT NULL DEFAULT 0     COMMENT '전체 건수',
    success_count   INT             NOT NULL DEFAULT 0     COMMENT '성공 건수',
    fail_count      INT             NOT NULL DEFAULT 0     COMMENT '실패 건수',
    error_message   TEXT            NULL                   COMMENT '에러 메시지',
    started_at      DATETIME        NOT NULL               COMMENT '시작 일시',
    completed_at    DATETIME        NULL                   COMMENT '완료 일시',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id),
    INDEX idx_sync_job_supplier (supplier_id),
    INDEX idx_sync_job_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='공급자 동기화 작업';
