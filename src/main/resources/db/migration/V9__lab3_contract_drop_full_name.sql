ALTER TABLE customers
    ALTER COLUMN first_name SET NOT NULL,
ALTER COLUMN last_name SET NOT NULL;

DROP TRIGGER customers_sync_name_parts ON customers;
DROP FUNCTION sync_customer_name_parts();

ALTER TABLE customers
DROP COLUMN full_name;