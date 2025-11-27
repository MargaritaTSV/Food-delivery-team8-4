-- Полная схема Food Delivery: создает все таблицы и индексы за один проход.
-- Выполнять после опционального 000_drop_tables.sql, если нужна переинициализация.

-- Адреса
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

-- Рабочие часы
CREATE TABLE IF NOT EXISTS working_hours (
    id BIGSERIAL PRIMARY KEY,
    monday VARCHAR(50),
    tuesday VARCHAR(50),
    wednesday VARCHAR(50),
    thursday VARCHAR(50),
    friday VARCHAR(50),
    saturday VARCHAR(50),
    sunday VARCHAR(50)
);

-- Клиенты
CREATE TABLE IF NOT EXISTS clients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    phone VARCHAR(20) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    address_id BIGINT REFERENCES addresses(id),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    order_history TEXT[]
);

-- Магазины
CREATE TABLE IF NOT EXISTS shops (
    shop_id BIGSERIAL PRIMARY KEY,
    naming VARCHAR(200) NOT NULL,
    description TEXT,
    public_email VARCHAR(255),
    email_for_auth VARCHAR(255) UNIQUE NOT NULL,
    phone_for_auth VARCHAR(20) UNIQUE NOT NULL,
    public_phone VARCHAR(20),
    status VARCHAR(50) NOT NULL,
    address_id BIGINT REFERENCES addresses(id),
    working_hours_id BIGINT REFERENCES working_hours(id),
    owner_name VARCHAR(200),
    owner_contact_phone VARCHAR(20),
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    rating DOUBLE PRECISION DEFAULT 0.0,
    type VARCHAR(50),
    password VARCHAR(255) NOT NULL
);

-- Продукты
CREATE TABLE IF NOT EXISTS products (
    product_id BIGSERIAL PRIMARY KEY,
    shop_id BIGINT REFERENCES shops(shop_id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    weight DOUBLE PRECISION,
    price DOUBLE PRECISION NOT NULL,
    category VARCHAR(50),
    is_available BOOLEAN DEFAULT TRUE,
    cooking_time_minutes BIGINT
);

-- Курьеры
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

-- Заказы
CREATE TABLE IF NOT EXISTS orders (
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

-- Позиции заказа
CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id) ON DELETE CASCADE,
    product_id BIGINT REFERENCES products(product_id),
    product_name VARCHAR(200) NOT NULL,
    quantity INTEGER NOT NULL,
    price DOUBLE PRECISION NOT NULL
);

-- Корзины
CREATE TABLE IF NOT EXISTS carts (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT REFERENCES clients(id) ON DELETE CASCADE,
    UNIQUE(client_id)
);

-- Платежи
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id) ON DELETE CASCADE,
    amount DOUBLE PRECISION NOT NULL,
    method VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Позиции корзины
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT REFERENCES carts(id) ON DELETE CASCADE,
    product_id BIGINT REFERENCES products(product_id) ON DELETE CASCADE,
    product_name VARCHAR(200) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    price DOUBLE PRECISION NOT NULL
);

-- Индексы
CREATE INDEX IF NOT EXISTS idx_clients_phone ON clients(phone);
CREATE INDEX IF NOT EXISTS idx_clients_email ON clients(email);
CREATE INDEX IF NOT EXISTS idx_shops_email_auth ON shops(email_for_auth);
CREATE INDEX IF NOT EXISTS idx_shops_phone_auth ON shops(phone_for_auth);
CREATE INDEX IF NOT EXISTS idx_couriers_phone ON couriers(phone_number);
CREATE INDEX IF NOT EXISTS idx_carts_client_id ON carts(client_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX IF NOT EXISTS idx_orders_customer_id ON orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_courier_id ON orders(courier_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_products_shop_id ON products(shop_id);
