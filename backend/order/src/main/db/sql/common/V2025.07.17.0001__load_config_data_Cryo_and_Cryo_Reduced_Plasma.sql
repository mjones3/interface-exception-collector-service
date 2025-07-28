INSERT INTO lk_order_product_family (family_category, family_type, description_key, product_family, order_number, active, create_date, modification_date)
VALUES ('REFRIGERATED', 'PLASMA_MFG_NONINJECTABLE', 'plasma-mfg-noninjectable.label', 'PLASMA_MFG_NONINJECTABLE', 10, true, now(),
        now()),
       ('FROZEN', 'PLASMA_MFG_INJECTABLE', 'plasma-mfg-injectable.label''', 'PLASMA_MFG_INJECTABLE', 10, true, now(),
        now()),
       ('FROZEN', 'CRYOPRECIPITATE', 'cryoprecipitate.label', 'CRYOPRECIPITATE', 10, true, now(),
        now())
    ON CONFLICT DO NOTHING;


INSERT INTO lk_order_blood_type (product_family, blood_type, description_key, order_number, active, create_date,
                                 modification_date)
VALUES ('PLASMA_MFG_INJECTABLE', 'A', 'blood-type.a.label', 1, true, now(), now()),
       ('PLASMA_MFG_INJECTABLE', 'B', 'blood-type.b.label', 2, true, now(), now()),
       ('PLASMA_MFG_INJECTABLE', 'AB', 'blood-type.ab.label', 3, true, now(), now()),
       ('PLASMA_MFG_INJECTABLE', 'O', 'blood-type.o.label', 4, true, now(), now()),
       ('PLASMA_MFG_INJECTABLE', 'ANY', 'blood-type.any.label', 5, true, now(), now()),
       ('PLASMA_MFG_NONINJECTABLE', 'A', 'blood-type.a.label', 1, true, now(), now()),
       ('PLASMA_MFG_NONINJECTABLE', 'B', 'blood-type.b.label', 2, true, now(), now()),
       ('PLASMA_MFG_NONINJECTABLE', 'AB', 'blood-type.ab.label', 3, true, now(), now()),
       ('PLASMA_MFG_NONINJECTABLE', 'O', 'blood-type.o.label', 4, true, now(), now()),
       ('PLASMA_MFG_NONINJECTABLE', 'ANY', 'blood-type.any.label', 5, true, now(), now()),
       ('CRYOPRECIPITATE', 'A', 'blood-type.a.label', 1, true, now(), now()),
       ('CRYOPRECIPITATE', 'B', 'blood-type.b.label', 2, true, now(), now()),
       ('CRYOPRECIPITATE', 'AB', 'blood-type.ab.label', 3, true, now(), now()),
       ('CRYOPRECIPITATE', 'O', 'blood-type.o.label', 4, true, now(), now()),
       ('CRYOPRECIPITATE', 'ANY', 'blood-type.any.label', 5, true, now(), now())

ON CONFLICT DO NOTHING;
