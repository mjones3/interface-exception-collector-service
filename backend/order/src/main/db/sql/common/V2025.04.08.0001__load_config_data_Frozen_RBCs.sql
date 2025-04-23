
INSERT INTO lk_order_product_family (family_category, family_type, description_key, product_family, order_number,
                                     active, create_date, modification_date)
VALUES ('FROZEN', 'TRANSFUSABLE_PRODUCT', 'red-blood-cells-leukoreduced.label', 'RED_BLOOD_CELLS_LEUKOREDUCED', 10, true, now(),
        now())
ON CONFLICT DO NOTHING;



