TRUNCATE TABLE lk_lookup;
TRUNCATE TABLE lk_order_blood_type;
TRUNCATE TABLE lk_order_product_family;
TRUNCATE TABLE lk_reason;

-- Shipping Methods
INSERT INTO lk_lookup (type, description_key, option_value, order_number, active)
VALUES ('ORDER_SHIPPING_METHOD', 'order-shipping-method.shuttle.label', 'SHUTTLE', 1, true),
       ('ORDER_SHIPPING_METHOD', 'order-shipping-method.fedex.label', 'FEDEX', 2, true),
       ('ORDER_SHIPPING_METHOD', 'order-shipping-method.courier.label', 'COURIER', 3, true)
ON CONFLICT DO NOTHING;

-- Priority
INSERT INTO lk_lookup (type, description_key, option_value, order_number, active)
VALUES ('ORDER_PRIORITY', 'order-priority.stat.label', 'STAT', 1, true),
       ('ORDER_PRIORITY', 'order-priority.asap.label', 'ASAP', 2, true),
       ('ORDER_PRIORITY', 'order-priority.routine.label', 'ROUTINE', 3, true),
       ('ORDER_PRIORITY', 'order-priority.scheduled.label', 'SCHEDULED', 4, true),
       ('ORDER_PRIORITY', 'order-priority.date-time.label', 'DATE_TIME', 5, true)
ON CONFLICT DO NOTHING;

-- Order Statuses
INSERT INTO lk_lookup (type, description_key, option_value, order_number, active)
VALUES
       ('ORDER_STATUS', 'order-status.open.label', 'OPEN', 2, true),
       ('ORDER_STATUS', 'order-status.in-progress.label', 'IN_PROGRESS', 5, true),
       ('ORDER_STATUS', 'order-status.completed.label', 'COMPLETED', 6, true)
ON CONFLICT DO NOTHING;

-- Shipment Statuses
INSERT INTO lk_lookup (type, description_key, option_value, order_number, active)
VALUES ('SHIPMENT_STATUS', 'shipment-status.open.label', 'OPEN', 1, true),
       ('SHIPMENT_STATUS', 'shipment-status.completed.label', 'COMPLETED', 2, true)
ON CONFLICT DO NOTHING;

-- Product Category
INSERT INTO lk_lookup (type, description_key, option_value, order_number, active)
VALUES ('PRODUCT_CATEGORY', 'product-category.frozen.label', 'FROZEN', 1, true),
       ('PRODUCT_CATEGORY', 'product-category.refrigerated.label', 'REFRIGERATED', 2, true),
       ('PRODUCT_CATEGORY', 'product-category.room-temperature.label', 'ROOM_TEMPERATURE', 3,
        true)
ON CONFLICT DO NOTHING;

-- Blood Types
INSERT INTO lk_lookup (type, description_key, option_value, order_number, active)
VALUES ('BLOOD_TYPE', 'blood-type.a-positive.label', 'AP', 1, true),
       ('BLOOD_TYPE', 'blood-type.a-negative.label', 'AN', 2, true),
       ('BLOOD_TYPE', 'blood-type.b-positive.label', 'BP', 3, true),
       ('BLOOD_TYPE', 'blood-type.b-negative.label', 'BN', 4, true),
       ('BLOOD_TYPE', 'blood-type.o-positive.label', 'OP', 5, true),
       ('BLOOD_TYPE', 'blood-type.o-negative.label', 'ON', 6, true),
       ('BLOOD_TYPE', 'blood-type.ab-positive.label', 'ABP', 7, true),
       ('BLOOD_TYPE', 'blood-type.ab-negative.label', 'ABN', 8, true)
ON CONFLICT DO NOTHING;

INSERT INTO lk_order_blood_type (product_family, blood_type, description_key, order_number, active, create_date,
                                 modification_date)
VALUES ('PLASMA_TRANSFUSABLE', 'A', 'blood-type.a.label', 1, true, now(), now()),
       ('PLASMA_TRANSFUSABLE', 'B', 'blood-type.b.label', 2, true, now(), now()),
       ('PLASMA_TRANSFUSABLE', 'AB', 'blood-type.ab.label', 3, true, now(), now()),
       ('PLASMA_TRANSFUSABLE', 'O', 'blood-type.o.label', 4, true, now(), now()),
       ('PLASMA_TRANSFUSABLE', 'ANY', 'blood-type.any.label', 5, true, now(), now()),
       ('RED_BLOOD_CELLS_LEUKOREDUCED', 'AP', 'blood-type.ap.label', 1, true, now(), now()),
       ('RED_BLOOD_CELLS_LEUKOREDUCED', 'AN', 'blood-type.an.label', 2, true, now(), now()),
       ('RED_BLOOD_CELLS_LEUKOREDUCED', 'BP', 'blood-type.bp.label', 3, true, now(), now()),
       ('RED_BLOOD_CELLS_LEUKOREDUCED', 'BN', 'blood-type.bn.label', 4, true, now(), now()),
       ('RED_BLOOD_CELLS_LEUKOREDUCED', 'OP', 'blood-type.op.label', 5, true, now(), now()),
       ('RED_BLOOD_CELLS_LEUKOREDUCED', 'ON', 'blood-type.on.label', 6, true, now(), now()),
       ('RED_BLOOD_CELLS_LEUKOREDUCED', 'ABP', 'blood-type.abp.label', 7, true, now(), now()),
       ('RED_BLOOD_CELLS_LEUKOREDUCED', 'ABN', 'blood-type.abn.label', 8, true, now(), now()),
       ('RED_BLOOD_CELLS_LEUKOREDUCED', 'ANY', 'blood-type.any.label', 9, true, now(), now()),
       ('WHOLE_BLOOD_LEUKOREDUCED', 'AP', 'blood-type.ap.label', 1, true, now(), now()),
       ('WHOLE_BLOOD_LEUKOREDUCED', 'AN', 'blood-type.an.label', 2, true, now(), now()),
       ('WHOLE_BLOOD_LEUKOREDUCED', 'BP', 'blood-type.bp.label', 3, true, now(), now()),
       ('WHOLE_BLOOD_LEUKOREDUCED', 'BN', 'blood-type.bn.label', 4, true, now(), now()),
       ('WHOLE_BLOOD_LEUKOREDUCED', 'OP', 'blood-type.op.label', 5, true, now(), now()),
       ('WHOLE_BLOOD_LEUKOREDUCED', 'ON', 'blood-type.on.label', 6, true, now(), now()),
       ('WHOLE_BLOOD_LEUKOREDUCED', 'ABP', 'blood-type.abp.label', 7, true, now(), now()),
       ('WHOLE_BLOOD_LEUKOREDUCED', 'ABN', 'blood-type.abn.label', 8, true, now(), now()),
       ('WHOLE_BLOOD_LEUKOREDUCED', 'ANY', 'blood-type.any.label', 9, true, now(), now()),
       ('WHOLE_BLOOD', 'AP', 'blood-type.ap.label', 1, true, now(), now()),
       ('WHOLE_BLOOD', 'AN', 'blood-type.an.label', 2, true, now(), now()),
       ('WHOLE_BLOOD', 'BP', 'blood-type.bp.label', 3, true, now(), now()),
       ('WHOLE_BLOOD', 'BN', 'blood-type.bn.label', 4, true, now(), now()),
       ('WHOLE_BLOOD', 'OP', 'blood-type.op.label', 5, true, now(), now()),
       ('WHOLE_BLOOD', 'ON', 'blood-type.on.label', 6, true, now(), now()),
       ('WHOLE_BLOOD', 'ABP', 'blood-type.abp.label', 7, true, now(), now()),
       ('WHOLE_BLOOD', 'ABN', 'blood-type.abn.label', 8, true, now(), now()),
       ('WHOLE_BLOOD', 'ANY', 'blood-type.any.label', 9, true, now(), now()),
       ('RED_BLOOD_CELLS', 'AP', 'blood-type.ap.label', 1, true, now(), now()),
       ('RED_BLOOD_CELLS', 'AN', 'blood-type.an.label', 2, true, now(), now()),
       ('RED_BLOOD_CELLS', 'BP', 'blood-type.bp.label', 3, true, now(), now()),
       ('RED_BLOOD_CELLS', 'BN', 'blood-type.bn.label', 4, true, now(), now()),
       ('RED_BLOOD_CELLS', 'OP', 'blood-type.op.label', 5, true, now(), now()),
       ('RED_BLOOD_CELLS', 'ON', 'blood-type.on.label', 6, true, now(), now()),
       ('RED_BLOOD_CELLS', 'ABP', 'blood-type.abp.label', 7, true, now(), now()),
       ('RED_BLOOD_CELLS', 'ABN', 'blood-type.abn.label', 8, true, now(), now()),
       ('RED_BLOOD_CELLS', 'ANY', 'blood-type.any.label', 9, true, now(), now())
ON CONFLICT DO NOTHING;


INSERT INTO lk_order_product_family (family_category, family_type, description_key, product_family, order_number,
                                     active, create_date, modification_date)
VALUES ('FROZEN', 'TRANSFUSABLE_PRODUCT', 'plasma-transfusable.label', 'PLASMA_TRANSFUSABLE', 1, true, now(),
        now()),
       ('REFRIGERATED', 'TRANSFUSABLE_PRODUCT', 'red-blood-cells-leukoreduced.label', 'RED_BLOOD_CELLS_LEUKOREDUCED', 2, true, now(),
        now()),
       ('REFRIGERATED', 'TRANSFUSABLE_PRODUCT', 'plasma-transfusable.label', 'PLASMA_TRANSFUSABLE', 3, true, now(),
        now()),
       ('REFRIGERATED', 'TRANSFUSABLE_PRODUCT', 'red-blood-cells.label', 'RED_BLOOD_CELLS', 4, true, now(),
        now()),
       ('REFRIGERATED', 'TRANSFUSABLE_PRODUCT', 'whole-blood.label', 'WHOLE_BLOOD', 5, true, now(),
        now()),
       ('REFRIGERATED', 'TRANSFUSABLE_PRODUCT', 'whole-blood-leukoreduced.label', 'WHOLE_BLOOD_LEUKOREDUCED', 6, true, now(),
        now())
ON CONFLICT DO NOTHING;


-- Shipment Type
INSERT INTO lk_lookup (type, description_key, option_value, order_number, active)
VALUES ('ORDER_SHIPMENT_TYPE', 'order-shipment-type.customer.label', 'CUSTOMER', 1, true)
ON CONFLICT DO NOTHING;

-- Order Status Color
INSERT INTO lk_lookup (type, description_key, option_value, order_number, active)
VALUES ('ORDER_PRIORITY_COLOR', 'STAT', '#ff3333', 1, true),
       ('ORDER_PRIORITY_COLOR', 'ASAP', '#ffb833', 2, true),
       ('ORDER_PRIORITY_COLOR', 'ROUTINE', '#d7d6d3', 3, true),
       ('ORDER_PRIORITY_COLOR', 'SCHEDULED', '#97a6f2', 4, true),
       ('ORDER_PRIORITY_COLOR', 'DATE-TIME', '#0930f6', 5, true)
ON CONFLICT DO NOTHING;
