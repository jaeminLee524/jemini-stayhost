-- Partner Context
CREATE TABLE partner (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    business_name   VARCHAR(200)    NOT NULL,
    business_number VARCHAR(20)     NOT NULL,
    representative  VARCHAR(100)    NOT NULL,
    phone           VARCHAR(20)     NULL,
    email           VARCHAR(200)    NULL,
    login_id        VARCHAR(100)    NOT NULL,
    password        VARCHAR(255)    NOT NULL,
    bank_name       VARCHAR(50)     NULL,
    bank_account    VARCHAR(50)     NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_partner_business_number (business_number),
    UNIQUE KEY uk_partner_login_id (login_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Property Context
CREATE TABLE property (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    partner_id      BIGINT          NOT NULL,
    name            VARCHAR(200)    NOT NULL,
    type            VARCHAR(30)     NOT NULL,
    description     TEXT            NULL,
    address         VARCHAR(500)    NULL,
    region          VARCHAR(100)    NOT NULL,
    latitude        DECIMAL(10, 7)  NULL,
    longitude       DECIMAL(10, 7)  NULL,
    check_in_time   TIME            NULL,
    check_out_time  TIME            NULL,
    thumbnail_url   VARCHAR(500)    NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'INACTIVE',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_property_partner_id (partner_id),
    INDEX idx_property_region (region),
    INDEX idx_property_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE property_image (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    property_id     BIGINT          NOT NULL,
    image_url       VARCHAR(500)    NOT NULL,
    sort_order      INT             NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_property_image_property_id (property_id),
    CONSTRAINT fk_property_image_property FOREIGN KEY (property_id) REFERENCES property (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE room_type (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    property_id         BIGINT          NOT NULL,
    name                VARCHAR(200)    NOT NULL,
    description         TEXT            NULL,
    max_occupancy       INT             NOT NULL,
    base_price          DECIMAL(12, 2)  NOT NULL,
    amenities           JSON            NULL,
    total_room_count    INT             NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_room_type_property_id (property_id),
    CONSTRAINT fk_room_type_property FOREIGN KEY (property_id) REFERENCES property (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE rate (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    room_type_id    BIGINT          NOT NULL,
    date            DATE            NOT NULL,
    price           DECIMAL(12, 2)  NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_rate_room_type_date (room_type_id, date),
    CONSTRAINT fk_rate_room_type FOREIGN KEY (room_type_id) REFERENCES room_type (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE inventory (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    room_type_id    BIGINT          NOT NULL,
    date            DATE            NOT NULL,
    total_count     INT             NOT NULL,
    reserved_count  INT             NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_inventory_room_type_date (room_type_id, date),
    CONSTRAINT fk_inventory_room_type FOREIGN KEY (room_type_id) REFERENCES room_type (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- User Context
CREATE TABLE users (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    email           VARCHAR(200)    NOT NULL,
    password        VARCHAR(255)    NOT NULL,
    name            VARCHAR(100)    NOT NULL,
    phone           VARCHAR(20)     NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Booking Context
CREATE TABLE reservation (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    reservation_number  VARCHAR(50)     NOT NULL,
    user_id             BIGINT          NOT NULL,
    property_id         BIGINT          NOT NULL,
    room_type_id        BIGINT          NOT NULL,
    check_in_date       DATE            NOT NULL,
    check_out_date      DATE            NOT NULL,
    guest_name          VARCHAR(100)    NOT NULL,
    guest_phone         VARCHAR(20)     NULL,
    guest_count         INT             NOT NULL,
    base_price          DECIMAL(12, 2)  NOT NULL,
    discount_amount     DECIMAL(12, 2)  NOT NULL DEFAULT 0,
    final_price         DECIMAL(12, 2)  NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'CONFIRMED',
    source              VARCHAR(20)     NOT NULL DEFAULT 'DIRECT',
    cancelled_at        DATETIME        NULL,
    cancel_reason       VARCHAR(500)    NULL,
    confirmed_at        DATETIME        NULL,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_reservation_number (reservation_number),
    INDEX idx_reservation_user_id (user_id),
    INDEX idx_reservation_property_id (property_id),
    INDEX idx_reservation_room_type_id (room_type_id),
    INDEX idx_reservation_status (status),
    CONSTRAINT fk_reservation_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_reservation_property FOREIGN KEY (property_id) REFERENCES property (id),
    CONSTRAINT fk_reservation_room_type FOREIGN KEY (room_type_id) REFERENCES room_type (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE reservation_daily_rate (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    reservation_id  BIGINT          NOT NULL,
    date            DATE            NOT NULL,
    price           DECIMAL(12, 2)  NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_reservation_daily_rate_reservation_id (reservation_id),
    CONSTRAINT fk_reservation_daily_rate_reservation FOREIGN KEY (reservation_id) REFERENCES reservation (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
