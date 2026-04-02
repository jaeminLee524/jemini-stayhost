CREATE TABLE room_type_image (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '이미지 ID',
    room_type_id    BIGINT          NOT NULL               COMMENT '객실유형 ID (FK)',
    image_url       VARCHAR(500)    NOT NULL               COMMENT '이미지 URL',
    sort_order      INT             NOT NULL DEFAULT 0     COMMENT '정렬 순서',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id),
    INDEX idx_room_type_image_room_type_id (room_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='객실 유형 이미지';
