ALTER TABLE shops ADD CONSTRAINT fk_shops_working_hours
    FOREIGN KEY (working_hours_id) REFERENCES working_hours(id);
ALTER TABLE shops ADD CONSTRAINT fk_shops_addresses
    FOREIGN KEY (address_id) REFERENCES addresses(id);
ALTER TABLE products ADD CONSTRAINT fk_products_shops
    FOREIGN KEY (shop_id) REFERENCES shops(shop_id);