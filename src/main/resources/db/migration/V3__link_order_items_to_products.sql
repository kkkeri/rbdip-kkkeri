ALTER TABLE order_items
    ADD COLUMN product_id BIGINT REFERENCES products(id);

UPDATE order_items AS item
SET product_id = (
    SELECT product.id
    FROM products AS product
    WHERE product.name = item.product_name
      AND product.price = item.product_price
);

ALTER TABLE order_items
    ALTER COLUMN product_id SET NOT NULL;

ALTER TABLE order_items
DROP COLUMN product_name,
    DROP COLUMN product_price;