-- Add estimated_delivery_time column if it is missing (backward compatibility)
ALTER TABLE orders
ADD COLUMN IF NOT EXISTS estimated_delivery_time TIMESTAMP;
