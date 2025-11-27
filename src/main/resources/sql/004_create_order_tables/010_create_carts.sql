CREATE TABLE IF NOT EXISTS carts (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT,
    UNIQUE(client_id)
);