INSERT INTO lk_order_blood_type (product_family, blood_type, description_key, order_number, active, create_date,
                                 modification_date)
VALUES ('APHERESIS_PLATELETS_LEUKOREDUCED', 'A', 'blood-type.a.label', 1, true, now(), now()),
       ('APHERESIS_PLATELETS_LEUKOREDUCED', 'B', 'blood-type.b.label', 2, true, now(), now()),
       ('APHERESIS_PLATELETS_LEUKOREDUCED', 'AB', 'blood-type.ab.label', 3, true, now(), now()),
       ('APHERESIS_PLATELETS_LEUKOREDUCED', 'O', 'blood-type.o.label', 4, true, now(), now()),
       ('APHERESIS_PLATELETS_LEUKOREDUCED', 'ANY', 'blood-type.any.label', 5, true, now(), now()),
      ('PRT_APHERESIS_PLATELETS', 'A', 'blood-type.a.label', 1, true, now(), now()),
       ('PRT_APHERESIS_PLATELETS', 'B', 'blood-type.b.label', 2, true, now(), now()),
       ('PRT_APHERESIS_PLATELETS', 'AB', 'blood-type.ab.label', 3, true, now(), now()),
       ('PRT_APHERESIS_PLATELETS', 'O', 'blood-type.o.label', 4, true, now(), now()),
       ('PRT_APHERESIS_PLATELETS', 'ANY', 'blood-type.any.label', 5, true, now(), now())


ON CONFLICT DO NOTHING;


INSERT INTO lk_order_product_family (family_category, family_type, description_key, product_family, order_number,
                                     active, create_date, modification_date)
VALUES ('ROOM_TEMPERATURE', 'TRANSFUSABLE_PRODUCT', 'apheresis-platelets-leukoreduced.label', 'APHERESIS_PLATELETS_LEUKOREDUCED', 7, true, now(),
        now()),
       ('ROOM_TEMPERATURE', 'TRANSFUSABLE_PRODUCT', 'prt-apheresis-platelets.label', 'PRT_APHERESIS_PLATELETS', 8, true, now(),
        now()),
       ('REFRIGERATED', 'TRANSFUSABLE_PRODUCT', 'prt-apheresis-platelets.label', 'PRT_APHERESIS_PLATELETS', 9, true, now(),
        now())
ON CONFLICT DO NOTHING;
