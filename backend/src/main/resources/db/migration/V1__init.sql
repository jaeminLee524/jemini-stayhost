-- Partner Context
CREATE TABLE partner (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '파트너 ID',
    business_name   VARCHAR(200)    NOT NULL               COMMENT '상호명',
    business_number VARCHAR(20)     NOT NULL               COMMENT '사업자등록번호',
    representative  VARCHAR(100)    NOT NULL               COMMENT '대표자명',
    phone           VARCHAR(20)     NULL                   COMMENT '연락처',
    email           VARCHAR(200)    NULL                   COMMENT '이메일',
    login_id        VARCHAR(100)    NOT NULL               COMMENT '로그인 ID',
    password        VARCHAR(255)    NOT NULL               COMMENT '비밀번호(BCrypt)',
    bank_name       VARCHAR(50)     NULL                   COMMENT '정산 은행명',
    bank_account    VARCHAR(50)     NULL                   COMMENT '정산 계좌번호',
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT '상태(PENDING/ACTIVE/SUSPENDED)',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id),
    UNIQUE KEY uk_partner_business_number (business_number),
    UNIQUE KEY uk_partner_login_id (login_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='파트너(숙소 사업자)';

-- Property Context
CREATE TABLE property (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '숙소 ID',
    partner_id      BIGINT          NOT NULL               COMMENT '파트너 ID (FK)',
    name            VARCHAR(200)    NOT NULL               COMMENT '숙소명',
    type            VARCHAR(30)     NOT NULL               COMMENT '숙소 유형(HOTEL/MOTEL/PENSION/RESORT)',
    description     TEXT            NULL                   COMMENT '숙소 소개',
    address         VARCHAR(500)    NULL                   COMMENT '주소',
    region          VARCHAR(100)    NOT NULL               COMMENT '지역(SEOUL/BUSAN/JEJU 등)',
    latitude        DECIMAL(10, 7)  NULL                   COMMENT '위도',
    longitude       DECIMAL(10, 7)  NULL                   COMMENT '경도',
    check_in_time   TIME            NULL                   COMMENT '체크인 시간',
    check_out_time  TIME            NULL                   COMMENT '체크아웃 시간',
    thumbnail_url   VARCHAR(500)    NULL                   COMMENT '썸네일 이미지 URL',
    status          VARCHAR(20)     NOT NULL DEFAULT 'INACTIVE' COMMENT '상태(ACTIVE/INACTIVE)',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id),
    INDEX idx_property_partner_id (partner_id),
    INDEX idx_property_region (region),
    INDEX idx_property_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='숙소';

CREATE TABLE property_image (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '이미지 ID',
    property_id     BIGINT          NOT NULL               COMMENT '숙소 ID (FK)',
    image_url       VARCHAR(500)    NOT NULL               COMMENT '이미지 URL',
    sort_order      INT             NOT NULL DEFAULT 0     COMMENT '정렬 순서',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id),
    INDEX idx_property_image_property_id (property_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='숙소 이미지';

CREATE TABLE room_type (
    id                  BIGINT          NOT NULL AUTO_INCREMENT COMMENT '객실유형 ID',
    property_id         BIGINT          NOT NULL               COMMENT '숙소 ID (FK)',
    name                VARCHAR(200)    NOT NULL               COMMENT '객실유형명',
    description         TEXT            NULL                   COMMENT '객실 설명',
    max_occupancy       INT             NOT NULL               COMMENT '최대 수용 인원',
    base_price          DECIMAL(12, 2)  NOT NULL               COMMENT '기본 요금',
    amenities           JSON            NULL                   COMMENT '편의시설 목록(JSON)',
    total_room_count    INT             NOT NULL               COMMENT '총 객실 수 (메타 정보, inventory.total_count 초기값 기준)',
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT '상태(ACTIVE/INACTIVE)',
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id),
    INDEX idx_room_type_property_id (property_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='객실 유형';

CREATE TABLE rate (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '요금 ID',
    room_type_id    BIGINT          NOT NULL               COMMENT '객실유형 ID (FK)',
    date            DATE            NOT NULL               COMMENT '적용 날짜',
    price           DECIMAL(12, 2)  NOT NULL               COMMENT '해당일 요금',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id),
    UNIQUE KEY uk_rate_room_type_date (room_type_id, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='일별 요금';

CREATE TABLE inventory (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '재고 ID',
    room_type_id    BIGINT          NOT NULL               COMMENT '객실유형 ID (FK)',
    date            DATE            NOT NULL               COMMENT '적용 날짜',
    total_count     INT             NOT NULL               COMMENT '총 객실 수',
    reserved_count  INT             NOT NULL DEFAULT 0     COMMENT '예약된 객실 수',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id),
    UNIQUE KEY uk_inventory_room_type_date (room_type_id, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='일별 재고';

-- User Context
CREATE TABLE users (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '회원 ID',
    email           VARCHAR(200)    NOT NULL               COMMENT '이메일(로그인 ID)',
    password        VARCHAR(255)    NOT NULL               COMMENT '비밀번호(BCrypt)',
    name            VARCHAR(100)    NOT NULL               COMMENT '이름',
    phone           VARCHAR(20)     NULL                   COMMENT '연락처',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT '상태(ACTIVE/WITHDRAWN)',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='회원(고객)';

-- Booking Context
CREATE TABLE reservation (
    id                  BIGINT          NOT NULL AUTO_INCREMENT COMMENT '예약 ID',
    reservation_number  VARCHAR(50)     NOT NULL               COMMENT '예약번호',
    user_id             BIGINT          NOT NULL               COMMENT '회원 ID (FK)',
    property_id         BIGINT          NOT NULL               COMMENT '숙소 ID (FK)',
    room_type_id        BIGINT          NOT NULL               COMMENT '객실유형 ID (FK)',
    check_in_date       DATE            NOT NULL               COMMENT '체크인 날짜',
    check_out_date      DATE            NOT NULL               COMMENT '체크아웃 날짜',
    guest_name          VARCHAR(100)    NOT NULL               COMMENT '투숙객 이름',
    guest_phone         VARCHAR(20)     NULL                   COMMENT '투숙객 연락처',
    guest_count         INT             NOT NULL               COMMENT '투숙 인원',
    base_price          DECIMAL(12, 2)  NOT NULL               COMMENT '기본 금액(일별 요금 합계)',
    discount_amount     DECIMAL(12, 2)  NOT NULL DEFAULT 0     COMMENT '할인 금액',
    final_price         DECIMAL(12, 2)  NOT NULL               COMMENT '최종 결제 금액',
    status              VARCHAR(20)     NOT NULL DEFAULT 'CONFIRMED' COMMENT '상태(CONFIRMED/CANCELLED)',
    source              VARCHAR(20)     NOT NULL DEFAULT 'DIRECT' COMMENT '예약 경로(DIRECT/CHANNEL)',
    cancelled_at        DATETIME        NULL                   COMMENT '취소일시',
    cancel_reason       VARCHAR(500)    NULL                   COMMENT '취소 사유',
    confirmed_at        DATETIME        NULL                   COMMENT '확정일시',
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id),
    UNIQUE KEY uk_reservation_number (reservation_number),
    INDEX idx_reservation_user_id (user_id),
    INDEX idx_reservation_property_id (property_id),
    INDEX idx_reservation_room_type_id (room_type_id),
    INDEX idx_reservation_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='예약';

CREATE TABLE reservation_daily_rate (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '일별요금 ID',
    reservation_id  BIGINT          NOT NULL               COMMENT '예약 ID (FK)',
    date            DATE            NOT NULL               COMMENT '적용 날짜',
    price           DECIMAL(12, 2)  NOT NULL               COMMENT '해당일 요금',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id),
    INDEX idx_reservation_daily_rate_reservation_id (reservation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='예약 일별 요금 내역';
