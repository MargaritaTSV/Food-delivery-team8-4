CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(50) NOT NULL,
    customer_id BIGINT REFERENCES clients(id),
    restaurant_id BIGINT REFERENCES shops(shop_id),
    delivery_address_id BIGINT REFERENCES addresses(id),
    delivery_address TEXT,
    courier_id BIGINT REFERENCES couriers(id),
    total_price DOUBLE PRECISION,
    payment_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(50),
    estimated_delivery_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);