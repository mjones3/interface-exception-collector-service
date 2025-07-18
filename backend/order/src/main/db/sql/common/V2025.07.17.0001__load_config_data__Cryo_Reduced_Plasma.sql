INSERT INTO lk_order_product_family (family_category, family_type, description_key, product_family, order_number, active, create_date, modification_date)
VALUES ('REFRIGERATED', 'PLASMA_MFG_NONINJECTABLE', 'plasma-mfg-noninjectable.label', 'PLASMA_MFG_NONINJECTABLE', 10, true, now(),
        now()),
       ('FROZEN', 'PLASMA_MFG_INJECTABLE', 'plasma-mfg-injectable.label''', 'PLASMA_MFG_INJECTABLE', 10, true, now(),
        now()),
       ('FROZEN', 'CRYOPRECIPITATE', 'cryoprecipitate.label', 'CRYOPRECIPITATE', 10, true, now(),
        now())
    ON CONFLICT DO NOTHING;
