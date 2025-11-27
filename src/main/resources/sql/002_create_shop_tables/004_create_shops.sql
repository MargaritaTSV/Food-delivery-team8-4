CREATE TABLE IF NOT EXISTS shops (
    shop_id BIGSERIAL PRIMARY KEY,
    naming VARCHAR(200) NOT NULL,
    description TEXT,
    public_email VARCHAR(255),
    email_for_auth VARCHAR(255) UNIQUE NOT NULL,
    phone_for_auth VARCHAR(20) UNIQUE NOT NULL,
    public_phone VARCHAR(20),
    status VARCHAR(50) NOT NULL,
    address_id BIGINT,
    working_hours_id BIGINT,
    owner_name VARCHAR(200),
    owner_contact_phone VARCHAR(20),
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    rating DOUBLE PRECISION DEFAULT 0.0,
    type VARCHAR(50),
    password VARCHAR(255) NOT NULL
);