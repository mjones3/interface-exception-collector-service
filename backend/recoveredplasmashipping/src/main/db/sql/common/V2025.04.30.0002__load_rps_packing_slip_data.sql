TRUNCATE TABLE recoveredplasmashipping.lk_system_process_property;

INSERT INTO recoveredplasmashipping.lk_system_process_property (system_process_type,property_key,property_value)
VALUES ('RPS_CARTON_PACKING_SLIP','BLOOD_CENTER_NAME','ARC-One Solutions'),
       ('RPS_CARTON_PACKING_SLIP','USE_TESTING_STATEMENT','Y'),
       ('RPS_CARTON_PACKING_SLIP','USE_SIGNATURE','Y'),
       ('RPS_CARTON_PACKING_SLIP','USE_TRANSPORTATION_NUMBER','Y'),
       ('RPS_CARTON_PACKING_SLIP','USE_LICENSE_NUMBER','Y'),
       ('RPS_CARTON_PACKING_SLIP','DATE_TIME_FORMAT','MM/dd/yyyy HH:mm'),
       ('RPS_CARTON_PACKING_SLIP','DATE_FORMAT','MM/dd/yyyy'),
       ('RPS_CARTON_PACKING_SLIP','ADDRESS_FORMAT','{address} {city}, {state}, {zipCode} {country}'),
       ('RPS_CARTON_PACKING_SLIP','TESTING_STATEMENT_TXT','Products packed, inspected and found satisfactory by: {employeeName}');



INSERT INTO recoveredplasmashipping.lk_location_property (location_id,property_key,property_value)
VALUES (1,'TZ','America/New_York'),
       (1,'LICENSE_NUMBER','2222'),
       (2, 'TZ','America/Chicago'),
       (2,'LICENSE_NUMBER','1875');
