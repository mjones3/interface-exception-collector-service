INSERT INTO lk_order_product_family (family_category, family_type, description_key, product_family, order_number, active, create_date, modification_date)
VALUES ('ROOM_TEMPERATURE', 'WASHED_APHERESIS_PLATELETS', 'washed-apheresis-platelets.label', 'WASHED_APHERESIS_PLATELETS', 10, true, now(),
        now()),
       ('ROOM_TEMPERATURE', 'WASHED_PRT_APHERESIS_PLATELETS', 'washed-prt-apheresis-platelets.label''', 'WASHED_PRT_APHERESIS_PLATELETS', 10, true, now(),
        now()),
       ('REFRIGERATED', 'WASHED_RED_BLOOD_CELLS', 'washed-red-blood-cells.label', 'WASHED_RED_BLOOD_CELLS', 10, true, now(),
        now())
    ON CONFLICT DO NOTHING;


INSERT INTO lk_order_blood_type (product_family, blood_type, description_key, order_number, active, create_date,
                                 modification_date)
VALUES ('WASHED_APHERESIS_PLATELETS', 'A', 'blood-type.a.label', 1, true, now(), now()),
       ('WASHED_APHERESIS_PLATELETS', 'B', 'blood-type.b.label', 2, true, now(), now()),
       ('WASHED_APHERESIS_PLATELETS', 'AB', 'blood-type.ab.label', 3, true, now(), now()),
       ('WASHED_APHERESIS_PLATELETS', 'O', 'blood-type.o.label', 4, true, now(), now()),
       ('WASHED_APHERESIS_PLATELETS', 'ANY', 'blood-type.any.label', 5, true, now(), now()),
       ('WASHED_PRT_APHERESIS_PLATELETS', 'A', 'blood-type.a.label', 1, true, now(), now()),
       ('WASHED_PRT_APHERESIS_PLATELETS', 'B', 'blood-type.b.label', 2, true, now(), now()),
       ('WASHED_PRT_APHERESIS_PLATELETS', 'AB', 'blood-type.ab.label', 3, true, now(), now()),
       ('WASHED_PRT_APHERESIS_PLATELETS', 'O', 'blood-type.o.label', 4, true, now(), now()),
       ('WASHED_PRT_APHERESIS_PLATELETS', 'ANY', 'blood-type.any.label', 5, true, now(), now()),
       ('WASHED_RED_BLOOD_CELLS', 'AP', 'blood-type.ap.label', 1, true, now(), now()),
       ('WASHED_RED_BLOOD_CELLS', 'AN', 'blood-type.an.label', 1, true, now(), now()),
       ('WASHED_RED_BLOOD_CELLS', 'BP', 'blood-type.bp.label', 2, true, now(), now()),
       ('WASHED_RED_BLOOD_CELLS', 'BN', 'blood-type.bn.label', 1, true, now(), now()),
       ('WASHED_RED_BLOOD_CELLS', 'ABP', 'blood-type.abp.label', 3, true, now(), now()),
       ('WASHED_RED_BLOOD_CELLS', 'ABN', 'blood-type.abn.label', 1, true, now(), now()),
       ('WASHED_RED_BLOOD_CELLS', 'OP', 'blood-type.op.label', 4, true, now(), now()),
       ('WASHED_RED_BLOOD_CELLS', 'ON', 'blood-type.on.label', 1, true, now(), now()),
       ('WASHED_RED_BLOOD_CELLS', 'ANY', 'blood-type.any.label', 5, true, now(), now())

    ON CONFLICT DO NOTHING;
