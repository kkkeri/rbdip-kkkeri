UPDATE customers
SET full_name = full_name
WHERE first_name IS NULL OR last_name IS NULL;