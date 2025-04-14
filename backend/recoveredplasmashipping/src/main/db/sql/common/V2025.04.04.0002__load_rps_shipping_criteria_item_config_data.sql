TRUNCATE TABLE recoveredplasmashipping.lk_recovered_plasma_shipment_criteria_item CASCADE;

INSERT INTO recoveredplasmashipping.lk_recovered_plasma_shipment_criteria_item(recovered_plasma_shipment_criteria_id, type, value, message, message_type)
VALUES (1, 'MINIMUM_VOLUME','165','Product Volume does not match criteria','WARN'),
       (1, 'MINIMUM_UNITS_BY_CARTON','20','Minimum number of products does not match','WARN'),
       (1, 'MAXIMUM_UNITS_BY_CARTON','20','Maximum number of products exceeded','WARN'),
       (2, 'MINIMUM_VOLUME','200','Product Volume does not match criteria','WARN'),
       (2, 'MINIMUM_UNITS_BY_CARTON','20','Minimum number of products does not match','WARN'),
       (2, 'MAXIMUM_UNITS_BY_CARTON','20','Maximum number of products exceeded','WARN'),
       (3, 'MINIMUM_UNITS_BY_CARTON','25','Minimum number of products does not match','WARN'),
       (3, 'MAXIMUM_UNITS_BY_CARTON','30','Maximum number of products exceeded','WARN'),
       (4, 'MINIMUM_UNITS_BY_CARTON','15','Minimum number of products does not match','WARN'),
       (4, 'MAXIMUM_UNITS_BY_CARTON','20','Maximum number of products exceeded','WARN'),
       (5, 'MINIMUM_UNITS_BY_CARTON','15','Minimum number of products does not match','WARN'),
       (5, 'MAXIMUM_UNITS_BY_CARTON','20','Maximum number of products exceeded','WARN'),
       (6, 'MINIMUM_UNITS_BY_CARTON','15','Minimum number of products does not match','WARN'),
       (6, 'MAXIMUM_UNITS_BY_CARTON','20','Maximum number of products exceeded','WARN');
