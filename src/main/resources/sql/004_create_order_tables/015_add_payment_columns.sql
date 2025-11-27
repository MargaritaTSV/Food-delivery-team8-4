-- Добавляет столбцы payment_status и payment_method в таблицу orders, если их нет
ALTER TABLE orders
  ADD COLUMN IF NOT EXISTS payment_status VARCHAR(50) NOT NULL DEFAULT 'PENDING';

ALTER TABLE orders
  ADD COLUMN IF NOT EXISTS payment_method VARCHAR(50);
