DELETE FROM receiving.lk_lookup WHERE type = 'TEMPERATURE_PRODUCT_CATEGORY';

-- Temperature Product Category
INSERT INTO lk_lookup (type, description_key, option_value, order_number, active)
VALUES ('TEMPERATURE_PRODUCT_CATEGORY', 'Frozen', 'FROZEN', 1, true),
       ('TEMPERATURE_PRODUCT_CATEGORY', 'Refrigerated', 'REFRIGERATED', 2, true),
       ('TEMPERATURE_PRODUCT_CATEGORY', 'Room Temperature', 'ROOM_TEMPERATURE', 3, true);
