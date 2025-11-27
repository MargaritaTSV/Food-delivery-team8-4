ALTER TABLE carts ADD CONSTRAINT fk_carts_client
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE;

ALTER TABLE orders ADD CONSTRAINT fk_order_customer
    FOREIGN KEY (customer_id) REFERENCES clients(id);

ALTER TABLE orders ADD CONSTRAINT fk_order_shop
    FOREIGN KEY (restaurant_id) REFERENCES shops(shop_id);

ALTER TABLE orders ADD CONSTRAINT fk_order_delivery_address
    FOREIGN KEY (delivery_address_id) REFERENCES addresses(id);

ALTER TABLE orders ADD CONSTRAINT fk_order_courier
    FOREIGN KEY (courier_id) REFERENCES couriers(id);

ALTER TABLE order_items ADD CONSTRAINT fk_order_items_product
    FOREIGN KEY (product_id) REFERENCES products(product_id);

ALTER TABLE cart_items ADD CONSTRAINT fk_cart_items_cart
    FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE;