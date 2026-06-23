CREATE TABLE IF NOT EXISTS coupons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    discount_amount BIGINT NOT NULL,
    status VARCHAR(16) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    recipient_phone VARCHAR(32),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS goods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1024),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

MERGE INTO goods (id, code, name, description, created_at, updated_at) KEY(id) VALUES
    (1, 'GOODS-001', '문화상품권 5천원권', '문화 콘텐츠 전용 상품권', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'GOODS-002', '문화상품권 1만원권', '문화 콘텐츠 전용 상품권', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 'GOODS-003', '주유 상품권 5만원권', '전국 주유소 사용 가능', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
