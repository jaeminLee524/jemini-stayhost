-- Channel Manager Context
CREATE TABLE channel (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '채널 ID',
    name            VARCHAR(100)    NOT NULL               COMMENT '채널명',
    code            VARCHAR(30)     NOT NULL               COMMENT '채널 코드',
    api_base_url    VARCHAR(500)    NULL                   COMMENT 'API 기본 URL',
    api_key         VARCHAR(255)    NULL                   COMMENT 'API 인증키',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT '상태(ACTIVE/INACTIVE)',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id),
    UNIQUE KEY uk_channel_name (name),
    UNIQUE KEY uk_channel_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='외부 채널';

CREATE TABLE channel_property_mapping (
    id                      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '매핑 ID',
    channel_id              BIGINT          NOT NULL               COMMENT '채널 ID',
    property_id             BIGINT          NOT NULL               COMMENT '자사 숙소 ID',
    external_property_id    VARCHAR(100)    NOT NULL               COMMENT '채널 측 숙소 ID',
    status                  VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT '매핑 상태(ACTIVE/INACTIVE)',
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id),
    UNIQUE KEY uk_channel_property (channel_id, property_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='채널-숙소 매핑';

CREATE TABLE channel_room_mapping (
    id                          BIGINT          NOT NULL AUTO_INCREMENT COMMENT '매핑 ID',
    channel_property_mapping_id BIGINT          NOT NULL               COMMENT '상위 숙소 매핑 ID',
    room_type_id                BIGINT          NOT NULL               COMMENT '자사 객실유형 ID',
    external_room_id            VARCHAR(100)    NOT NULL               COMMENT '채널 측 객실 ID',
    created_at                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='채널-객실 매핑';

CREATE TABLE channel_rate_policy (
    id                          BIGINT          NOT NULL AUTO_INCREMENT COMMENT '정책 ID',
    channel_property_mapping_id BIGINT          NOT NULL               COMMENT '숙소 채널 매핑 ID',
    room_type_id                BIGINT          NOT NULL               COMMENT '객실유형 ID',
    markup_type                 VARCHAR(20)     NOT NULL DEFAULT 'PERCENTAGE' COMMENT '마진 유형(PERCENTAGE/FIXED)',
    markup_value                DECIMAL(10, 2)  NOT NULL DEFAULT 0     COMMENT '마진값',
    created_at                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='채널별 요금 정책';

CREATE TABLE channel_sync_log (
    id               BIGINT          NOT NULL AUTO_INCREMENT COMMENT '로그 ID',
    channel_id       BIGINT          NOT NULL               COMMENT '채널 ID',
    property_id      BIGINT          NULL                   COMMENT '대상 숙소 ID',
    sync_type        VARCHAR(30)     NOT NULL               COMMENT '동기화 유형(INVENTORY/RATE/RESERVATION)',
    direction        VARCHAR(10)     NOT NULL               COMMENT '방향(OUTBOUND/INBOUND)',
    status           VARCHAR(20)     NOT NULL               COMMENT '결과(SUCCESS/FAILED/PARTIAL)',
    request_payload  JSON            NULL                   COMMENT '요청 데이터',
    response_payload JSON            NULL                   COMMENT '응답 데이터',
    error_message    TEXT            NULL                   COMMENT '에러 메시지',
    retry_count      INT             NOT NULL DEFAULT 0     COMMENT '재시도 횟수',
    created_at       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id),
    INDEX idx_sync_log_channel (channel_id),
    INDEX idx_sync_log_channel_type (channel_id, sync_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='채널 동기화 로그';
