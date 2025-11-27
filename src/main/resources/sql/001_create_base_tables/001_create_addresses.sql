CREATE TABLE IF NOT EXISTS addresses (
    id BIGSERIAL PRIMARY KEY,
    country VARCHAR(100) DEFAULT 'Russia',
    city VARCHAR(100) NOT NULL,
    street VARCHAR(255),
    building VARCHAR(20),
    apartment VARCHAR(20),
    entrance VARCHAR(10),
    floor INTEGER,
    postal_code VARCHAR(20),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    address_note TEXT,
    district VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);