CREATE TABLE IF NOT EXISTS couriers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    status VARCHAR(50) NOT NULL,
    transport_type VARCHAR(50),
    current_order_id BIGINT,
    current_balance DOUBLE PRECISION DEFAULT 0.0,
    bank_card BIGINT
);