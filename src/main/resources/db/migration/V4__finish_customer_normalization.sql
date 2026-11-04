INSERT INTO customers (full_name, address, phone)
SELECT DISTINCT customer_full_name, customer_address, customer_phone
FROM orders
WHERE customer_id IS NULL
    ON CONFLICT (full_name, address, phone) DO NOTHING;

UPDATE orders AS o
SET customer_id = c.id
    FROM customers AS c
WHERE o.customer_id IS NULL
  AND c.full_name = o.customer_full_name
  AND c.address IS NOT DISTINCT FROM o.customer_address
    AND c.phone IS NOT DISTINCT FROM o.customer_phone;

ALTER TABLE orders
    ALTER COLUMN customer_id SET NOT NULL;

ALTER TABLE orders
DROP COLUMN customer_full_name,
    DROP COLUMN customer_address,
    DROP COLUMN customer_phone;