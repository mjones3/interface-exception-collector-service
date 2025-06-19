ALTER TABLE recoveredplasmashipping.bld_recovered_plasma_shipment ADD COLUMN last_unsuitable_report_run_date timestamptz NULL;

-- Shipment Types
DELETE FROM recoveredplasmashipping.lk_lookup WHERE type = 'RPS_SHIPMENT_TYPE';
INSERT INTO recoveredplasmashipping.lk_lookup (type, description_key, option_value, order_number, active)
VALUES ('RPS_SHIPMENT_TYPE', 'In Progress', 'IN_PROGRESS', 1, true),
       ('RPS_SHIPMENT_TYPE', 'Open', 'OPEN', 2, true),
       ('RPS_SHIPMENT_TYPE', 'Processing', 'PROCESSING', 3, true),
       ('RPS_SHIPMENT_TYPE', 'Closed', 'CLOSED', 4, true);



