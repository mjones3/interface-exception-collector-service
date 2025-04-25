-- Shipment Types
DELETE FROM recoveredplasmashipping.lk_lookup WHERE type = 'RPS_SHIPMENT_TYPE';
INSERT INTO recoveredplasmashipping.lk_lookup (type, description_key, option_value, order_number, active)
VALUES ('RPS_SHIPMENT_TYPE', 'In Progress', 'IN_PROGRESS', 1, true),
    ('RPS_SHIPMENT_TYPE', 'Open', 'OPEN', 2, true),
       ('RPS_SHIPMENT_TYPE', 'Closed', 'CLOSED', 3, true)
;
