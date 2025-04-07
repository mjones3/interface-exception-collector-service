-- Product Types
DELETE FROM recoveredplasmashipping.lk_lookup WHERE type = 'RPS_PRODUCT_TYPE';
INSERT INTO recoveredplasmashipping.lk_lookup (type, description_key, option_value, order_number, active)
VALUES ('RPS_PRODUCT_TYPE', 'RP FROZEN WITHIN 120 HOURS', 'RP_FROZEN_WITHIN_120_HOURS', 1, true),
       ('RPS_PRODUCT_TYPE', 'RP FROZEN WITHIN 24 HOURS', 'RP_FROZEN_WITHIN_24_HOURS', 2, true),
       ('RPS_PRODUCT_TYPE', 'RP NONINJECTABLE FROZEN', 'RP_NONINJECTABLE_FROZEN', 3, true),
       ('RPS_PRODUCT_TYPE', 'RP NONINJECTABLE LIQUID RT', 'RP_NONINJECTABLE_LIQUID_RT', 4, true),
       ('RPS_PRODUCT_TYPE', 'RP FROZEN WITHIN 72 HOURS', 'RP_FROZEN_WITHIN_72_HOURS', 5, true),
       ('RPS_PRODUCT_TYPE', 'RP NONINJECTABLE REFRIGERATED', 'RP_NONINJECTABLE_REFRIGERATED', 6, true)
;

-- Shipment Types
DELETE FROM recoveredplasmashipping.lk_lookup WHERE type = 'RPS_SHIPMENT_TYPE';
INSERT INTO recoveredplasmashipping.lk_lookup (type, description_key, option_value, order_number, active)
VALUES ('RPS_SHIPMENT_TYPE', 'Open', 'OPEN', 1, true),
       ('RPS_SHIPMENT_TYPE', 'Closed', 'CLOSED', 2, true)
;
