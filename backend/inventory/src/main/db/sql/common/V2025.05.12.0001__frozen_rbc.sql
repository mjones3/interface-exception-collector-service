ALTER TABLE inventory.bld_inventory
    ADD COLUMN modification_location varchar(100);

ALTER TABLE inventory.bld_inventory
    ADD COLUMN product_modification_date TIMESTAMP WITH TIME ZONE;

INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E508500', 'FROZEN');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E510500', 'FROZEN');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E510600', 'FROZEN');
INSERT INTO inventory.lk_temperature_category_product_code (product_code, temperature_category) VALUES ('E510700', 'FROZEN');

