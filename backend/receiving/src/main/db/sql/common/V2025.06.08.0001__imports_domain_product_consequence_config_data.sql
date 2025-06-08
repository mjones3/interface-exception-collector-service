DELETE FROM receiving.lk_product_consequence;

-- Product Consequences
INSERT INTO receiving.lk_product_consequence (id, product_category, acceptable, result_property, result_type, result_value, consequence_type, consequence_reason, order_number, active, create_date, modification_date)
VALUES
    (1,'REFRIGERATED', true, 'TEMPERATURE', 'VALUE_FORMULA', 'TEMPERATURE >= 1 && TEMPERATURE <= 10', 'RETURN_TO_INVENTORY', 'return-to-inventory.label', 1, true, now(), now()),
    (2,'REFRIGERATED', false, 'TEMPERATURE', 'VALUE_FORMULA', 'TEMPERATURE > 10 || TEMPERATURE < 1', 'ADD_INVENTORY_QUARANTINE', 'quarantine-reason.out-of-temperature-control', 2, true, now(), now()),
    (3,'ROOM_TEMPERATURE', true, 'TEMPERATURE', 'VALUE_FORMULA', 'TEMPERATURE >= 20 && TEMPERATURE <= 24', 'RETURN_TO_INVENTORY', 'return-to-inventory.label', 3, true, now(), now()),
    (4,'ROOM_TEMPERATURE', false, 'TEMPERATURE', 'VALUE_FORMULA', 'TEMPERATURE > 24 || TEMPERATURE < 20', 'ADD_INVENTORY_QUARANTINE', 'quarantine-reason.out-of-temperature-control', 4, true, now(), now()),
    (5,'FROZEN', true, 'VISUAL_INSPECTION', 'VALUE_EQUALS', 'ACCEPTABLE', 'RETURN_TO_INVENTORY', 'return-to-inventory.label', 5, true, now(), now()),
    (6,'FROZEN', false, 'VISUAL_INSPECTION', 'VALUE_EQUALS', 'unACCEPTABLE', 'ADD_INVENTORY_QUARANTINE', 'quarantine-reason.inspection-unacceptable', 6, true, now(), now()),
    (7,'ROOM_TEMPERATURE', true, 'TRANSIT_TIME', 'VALUE_FORMULA', 'TRANSIT_TIME >= 0 && TRANSIT_TIME <= 24', 'RETURN_TO_INVENTORY', 'return-to-inventory.label', 7, true, now(), now()),
    (8,'ROOM_TEMPERATURE', false, 'TRANSIT_TIME', 'VALUE_FORMULA', 'TRANSIT_TIME > 24 || TRANSIT_TIME < 0', 'ADD_INVENTORY_QUARANTINE', 'quarantine-reason.transit-time-exceeded', 8, true, now(), now()),
    (9,'ROOM_TEMPERATURE', true, 'VISUAL_INSPECTION', 'VALUE_EQUALS', 'ACCEPTABLE', 'RETURN_TO_INVENTORY', 'return-to-inventory.label', 12, true, now(), now()),
    (10,'ROOM_TEMPERATURE', false, 'VISUAL_INSPECTION', 'VALUE_EQUALS', 'UNACCEPTABLE', 'ADD_INVENTORY_QUARANTINE', 'quarantine-reason.inspection-unacceptable', 13, true, now(), now()),
    (11,'REFRIGERATED', true, 'VISUAL_INSPECTION', 'VALUE_EQUALS', 'ACCEPTABLE', 'RETURN_TO_INVENTORY', 'return-to-inventory.label', 14, true, now(), now()),
    (12,'REFRIGERATED', false, 'VISUAL_INSPECTION', 'VALUE_EQUALS', 'UNACCEPTABLE', 'ADD_INVENTORY_QUARANTINE', 'quarantine-reason.inspection-unacceptable', 15, true, now(), now());
