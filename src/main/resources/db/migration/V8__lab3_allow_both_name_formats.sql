CREATE OR REPLACE FUNCTION sync_customer_name_parts()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
separator_pos integer;
    use_parts boolean;
BEGIN
    use_parts := NEW.full_name IS NULL;

    IF TG_OP = 'UPDATE' THEN
        IF NEW.full_name IS NOT DISTINCT FROM OLD.full_name
           AND (NEW.first_name, NEW.last_name)
               IS DISTINCT FROM (OLD.first_name, OLD.last_name) THEN
            use_parts := true;
END IF;
END IF;

    IF use_parts THEN
        NEW.full_name := CASE
            WHEN NEW.last_name = '' THEN NEW.first_name
            ELSE NEW.first_name || ' ' || NEW.last_name
END;
ELSE
        separator_pos := strpos(NEW.full_name, ' ');

        IF separator_pos > 0 AND separator_pos < length(NEW.full_name) THEN
            NEW.first_name := left(NEW.full_name, separator_pos - 1);
            NEW.last_name := substr(NEW.full_name, separator_pos + 1);
ELSE
            NEW.first_name := NEW.full_name;
            NEW.last_name := '';
END IF;
END IF;

RETURN NEW;
END;
$$;

DROP TRIGGER customers_sync_name_parts ON customers;

CREATE TRIGGER customers_sync_name_parts
    BEFORE INSERT OR UPDATE OF full_name, first_name, last_name ON customers
    FOR EACH ROW
    EXECUTE FUNCTION sync_customer_name_parts();

ALTER TABLE customers
    ADD CONSTRAINT customers_name_parts_contacts_key
        UNIQUE NULLS NOT DISTINCT (first_name, last_name, address, phone);