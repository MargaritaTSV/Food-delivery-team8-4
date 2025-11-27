-- Тестовые заказы
INSERT INTO orders (id, status, customer_id, restaurant_id, delivery_address_id, delivery_address, courier_id, total_price, payment_status, payment_method, estimated_delivery_time)
VALUES
  (7001, 'CONFIRMED', 3001, 4001, 1001, 'Тверская, 1', 6001, 1040.0, 'PAID', 'CARD', NOW() + INTERVAL '45 minutes'),
  (7002, 'PENDING', 3002, 4002, 1002, 'Невский проспект, 25', 6002, 980.0, 'PENDING', 'CASH', NOW() + INTERVAL '55 minutes'),
  (7003, 'DELIVERED', 3003, 4003, 1003, 'Баумана, 5', 6003, 840.0, 'PAID', 'ONLINE', NOW() - INTERVAL '10 minutes'),
  (7004, 'CONFIRMED', 3004, 4004, 1004, 'Ленина, 50', 6004, 870.0, 'PAID', 'CARD', NOW() + INTERVAL '40 minutes'),
  (7005, 'READY_FOR_PICKUP', 3005, 4005, 1005, 'Малышева, 15', NULL, 630.0, 'PENDING', 'CASH', NOW() + INTERVAL '60 minutes'),
  (7006, 'CONFIRMED', 3006, 4006, 1006, 'Большая Покровская, 32', 6006, 300.0, 'PAID', 'ONLINE', NOW() + INTERVAL '30 minutes'),
  (7007, 'DELIVERED', 3007, 4007, 1007, 'Ленинградская, 12', 6007, 880.0, 'PAID', 'CARD', NOW() - INTERVAL '5 minutes'),
  (7008, 'CONFIRMED', 3008, 4008, 1008, 'Советский проспект, 18', 6008, 480.0, 'PAID', 'CARD', NOW() + INTERVAL '35 minutes'),
  (7009, 'DELIVERED', 3009, 4009, 1009, 'Светланская, 9', 6009, 430.0, 'PAID', 'ONLINE', NOW() - INTERVAL '20 minutes'),
  (7010, 'CONFIRMED', 3010, 4010, 1010, 'Красная, 120', 6010, 360.0, 'PAID', 'CARD', NOW() + INTERVAL '50 minutes'),
  (7011, 'DELIVERED', 3011, 4011, 1011, 'Пушкинская, 80', 6011, 470.0, 'PAID', 'ONLINE', NOW() - INTERVAL '2 minutes'),
  (7012, 'PENDING', 3012, 4012, 1012, 'Комсомольский проспект, 40', 6012, 320.0, 'PENDING', 'CASH', NOW() + INTERVAL '70 minutes'),
  (7013, 'CONFIRMED', 3013, 4013, 1013, 'Проспект Революции, 10', 6013, 1350.0, 'PAID', 'CARD', NOW() + INTERVAL '65 minutes'),
  (7014, 'DELIVERED', 3014, 4014, 1014, 'Октябрьской Революции, 5', 6014, 310.0, 'PAID', 'ONLINE', NOW() - INTERVAL '15 minutes'),
  (7015, 'CONFIRMED', 3015, 4015, 1015, 'Республики, 62', 6015, 390.0, 'PAID', 'CARD', NOW() + INTERVAL '25 minutes'),
  (7016, 'CONFIRMED', 3016, 4016, 1016, 'Карла Маркса, 15', 6016, 420.0, 'PAID', 'ONLINE', NOW() + INTERVAL '28 minutes'),
  (7017, 'READY_FOR_PICKUP', 3017, 4001, 1017, 'Кирова, 25', NULL, 520.0, 'PENDING', 'CASH', NOW() + INTERVAL '75 minutes'),
  (7018, 'DELIVERED', 3018, 4002, 1018, 'Курортный проспект, 8', 6002, 540.0, 'PAID', 'CARD', NOW() - INTERVAL '25 minutes')
ON CONFLICT DO NOTHING;
