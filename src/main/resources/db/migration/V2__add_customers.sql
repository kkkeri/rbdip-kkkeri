CREATE TABLE customers (
                           id BIGSERIAL PRIMARY KEY,
                           full_name VARCHAR(255) NOT NULL,
                           address VARCHAR(500),
                           phone VARCHAR(50),
                           UNIQUE NULLS NOT DISTINCT (full_name, address, phone)
);

ALTER TABLE orders
    ADD COLUMN customer_id BIGINT REFERENCES customers(id);

INSERT INTO customers (full_name, address, phone)
SELECT DISTINCT customer_full_name, customer_address, customer_phone
FROM orders;

UPDATE orders AS o
SET customer_id = c.id
    FROM customers AS c
WHERE c.full_name = o.customer_full_name
  AND c.address IS NOT DISTINCT FROM o.customer_address
    AND c.phone IS NOT DISTINCT FROM o.customer_phone;