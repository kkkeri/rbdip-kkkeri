CREATE FUNCTION sync_customer_name_parts()
    RETURNS trigger
    LANGUAGE plpgsql
AS $$
DECLARE
separator_pos integer;
BEGIN
    separator_pos := strpos(NEW.full_name, ' ');

    IF separator_pos > 0 AND separator_pos < length(NEW.full_name) THEN
        NEW.first_name := left(NEW.full_name, separator_pos - 1);
        NEW.last_name := substr(NEW.full_name, separator_pos + 1);
ELSE
        NEW.first_name := NEW.full_name;
        NEW.last_name := '';
END IF;

RETURN NEW;
END;
$$;

CREATE TRIGGER customers_sync_name_parts
    BEFORE INSERT OR UPDATE OF full_name ON customers
    FOR EACH ROW
    EXECUTE FUNCTION sync_customer_name_parts();