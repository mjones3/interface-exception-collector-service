-- Add missing product families
INSERT INTO inventory.lk_product_family (product_family, time_frame) VALUES ('WASHED_APHERESIS_PLATELETS', 0);
INSERT INTO inventory.lk_product_family (product_family, time_frame) VALUES ('WASHED_RED_BLOOD_CELLS', 0);
INSERT INTO inventory.lk_product_family (product_family, time_frame) VALUES ('WASHED_PRT_APHERESIS_PLATELETS', 0);


-- Add temperature categories for washed products
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category, active, create_date, modification_date) VALUES ('E354100', 'ROOM_TEMPERATURE', true, now(), now());
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category, active, create_date, modification_date) VALUES ('E354300', 'ROOM_TEMPERATURE', true, now(), now());
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category, active, create_date, modification_date) VALUES ('E354400', 'ROOM_TEMPERATURE', true, now(), now());
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category, active, create_date, modification_date) VALUES ('E456200', 'REFRIGERATED', true, now(), now());
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category, active, create_date, modification_date) VALUES ('E456300', 'REFRIGERATED', true, now(), now());
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category, active, create_date, modification_date) VALUES ('E640800', 'ROOM_TEMPERATURE', true, now(), now());
