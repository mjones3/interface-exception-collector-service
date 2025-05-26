TRUNCATE TABLE receiving.lk_lookup;

-- Temperature Product Category
INSERT INTO lk_lookup (type, description_key, option_value, order_number, active)
VALUES ('TEMPERATURE_PRODUCT_CATEGORY', 'Frozen', 'FROZEN', 1, true),
       ('TEMPERATURE_PRODUCT_CATEGORY', 'Refrigerator', 'REFRIGERATED', 2, true),
       ('TEMPERATURE_PRODUCT_CATEGORY', 'Room Temperature', 'ROOM_TEMPERATURE', 3, true);


--- Transit Time Zone
INSERT INTO lk_lookup (type, description_key, option_value, order_number, active)
VALUES ('TRANSIT_TIME_ZONE', 'ET', 'America/New_York', 1, true),
       ('TRANSIT_TIME_ZONE', 'CT', 'America/Chicago', 2, true),
       ('TRANSIT_TIME_ZONE', 'MT', 'America/Denver', 3, true),
    ('TRANSIT_TIME_ZONE', 'MST', 'America/Phoenix', 4, true),
('TRANSIT_TIME_ZONE', 'MST', 'America/Los_Angeles', 5, true),
('TRANSIT_TIME_ZONE', 'AST', 'America/Puerto_Rico', 6, true);

-- Visual Inspection
INSERT INTO lk_lookup (type, description_key, option_value, order_number, active)
VALUES ('VISUAL_INSPECTION_STATUS', 'Satisfactory', 'SATISFACTORY', 1, true),
       ('VISUAL_INSPECTION_STATUS', 'Unsatisfactory', 'UNSATISFACTORY', 2, true);

-- Product Consequences
INSERT INTO receiving.lk_product_consequence (product_category, acceptable, result_property, result_type, result_value, consequence_type, consequence_reason, order_number, active, create_date, modification_date)
VALUES
    ('REFRIGERATED', true, 'TEMPERATURE', 'VALUE_FORMULA', 'TEMPERATURE >= 1 && TEMPERATURE <= 10', 'RETURN_TO_INVENTORY', 'return-to-inventory.label', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('REFRIGERATED', false, 'TEMPERATURE', 'VALUE_FORMULA', 'TEMPERATURE > 10 || TEMPERATURE < 1', 'ADD_INVENTORY_QUARANTINE', 'quarantine-reason.out-of-temperature-control', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ROOM_TEMPERATURE', true, 'TEMPERATURE', 'VALUE_FORMULA', 'TEMPERATURE >= 20 && TEMPERATURE <= 24', 'RETURN_TO_INVENTORY', 'return-to-inventory.label', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ROOM_TEMPERATURE', false, 'TEMPERATURE', 'VALUE_FORMULA', 'TEMPERATURE > 24 || TEMPERATURE < 20', 'ADD_INVENTORY_QUARANTINE', 'quarantine-reason.out-of-temperature-control', 4, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FROZEN', true, 'VISUAL_INSPECTION', 'VALUE_EQUALS', 'ACCEPTABLE', 'RETURN_TO_INVENTORY', 'return-to-inventory.label', 5, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('FROZEN', false, 'VISUAL_INSPECTION', 'VALUE_EQUALS', 'unACCEPTABLE', 'ADD_INVENTORY_QUARANTINE', 'quarantine-reason.inspection-unacceptable', 6, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ROOM_TEMPERATURE', true, 'TRANSIT_TIME', 'VALUE_FORMULA', 'TRANSIT_TIME >= 0 && TRANSIT_TIME <= 24', 'RETURN_TO_INVENTORY', 'return-to-inventory.label', 7, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ROOM_TEMPERATURE', false, 'TRANSIT_TIME', 'VALUE_FORMULA', 'TRANSIT_TIME > 24', 'ADD_INVENTORY_QUARANTINE', 'quarantine-reason.transit-time-exceeded', 8, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ROOM_TEMPERATURE', true, 'VISUAL_INSPECTION', 'VALUE_EQUALS', 'ACCEPTABLE', 'RETURN_TO_INVENTORY', 'return-to-inventory.label', 12, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ROOM_TEMPERATURE', false, 'VISUAL_INSPECTION', 'VALUE_EQUALS', 'UNACCEPTABLE', 'ADD_INVENTORY_QUARANTINE', 'quarantine-reason.inspection-unacceptable', 13, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('REFRIGERATED', true, 'VISUAL_INSPECTION', 'VALUE_EQUALS', 'ACCEPTABLE', 'RETURN_TO_INVENTORY', 'return-to-inventory.label', 14, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('REFRIGERATED', false, 'VISUAL_INSPECTION', 'VALUE_EQUALS', 'UNACCEPTABLE', 'ADD_INVENTORY_QUARANTINE', 'quarantine-reason.inspection-unacceptable', 15, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
