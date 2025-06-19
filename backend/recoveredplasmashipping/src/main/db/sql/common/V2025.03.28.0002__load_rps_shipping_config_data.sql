TRUNCATE TABLE recoveredplasmashipping.lk_recovered_plasma_shipment_criteria CASCADE;

INSERT INTO recoveredplasmashipping.lk_recovered_plasma_shipment_criteria (id, customer_code, product_type, order_number, active, create_date, modification_date)
VALUES (1,'408' ,'RP_FROZEN_WITHIN_120_HOURS', 1, true, now(), now()),
(2,'408' ,'RP_FROZEN_WITHIN_24_HOURS', 2, true, now(), now()),
(3,'408' ,'RP_NONINJECTABLE_FROZEN', 3, true, now(), now()),
(4,'409' ,'RP_NONINJECTABLE_LIQUID_RT', 4, true, now(), now()),
(5,'409' ,'RP_FROZEN_WITHIN_72_HOURS', 5, true, now(), now()),
(6,'410' ,'RP_NONINJECTABLE_REFRIGERATED', 6, true, now(), now());

TRUNCATE TABLE recoveredplasmashipping.lk_recovered_plasma_product_type CASCADE;

INSERT INTO recoveredplasmashipping.lk_recovered_plasma_product_type (id,product_type,product_type_description,order_number,active,create_date,modification_date)
VALUES (1,'RP_FROZEN_WITHIN_120_HOURS','RP FROZEN WITHIN 120 HOURS',1,true,now(),now()),
    (2,'RP_FROZEN_WITHIN_24_HOURS','RP FROZEN WITHIN 24 HOURS',2,true,now(),now()),
    (3,'RP_NONINJECTABLE_FROZEN','RP NONINJECTABLE FROZEN',3,true,now(),now()),
    (4,'RP_NONINJECTABLE_LIQUID_RT','RP NONINJECTABLE LIQUID RT',4,true,now(),now()),
    (5,'RP_FROZEN_WITHIN_72_HOURS','RP FROZEN WITHIN 72 HOURS',5,true,now(),now()),
    (6,'RP_NONINJECTABLE_REFRIGERATED','RP NONINJECTABLE REFRIGERATED',5,true,now(),now());

TRUNCATE TABLE recoveredplasmashipping.lk_recovered_plasma_product_type_product_code CASCADE;

INSERT INTO recoveredplasmashipping.lk_recovered_plasma_product_type_product_code (product_type_id,product_code)
VALUES (1,'E6022V00'),
       (2,'E2534V00'),
       (3,'E2603V00'),
       (4,'E2488V00'),
       (5,'E5880V00'),
       (6,'E6170V00');



