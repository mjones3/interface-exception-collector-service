TRUNCATE TABLE recoveredplasmashipping.lk_system_process_property;

INSERT INTO recoveredplasmashipping.lk_system_process_property (system_process_type,property_key,property_value)
VALUES ('RPS_CARTON_PACKING_SLIP','BLOOD_CENTER_NAME','ARC-One Solutions'),
       ('RPS_CARTON_PACKING_SLIP','USE_TESTING_STATEMENT','Y'),
       ('RPS_CARTON_PACKING_SLIP','USE_SIGNATURE','Y'),
       ('RPS_CARTON_PACKING_SLIP','USE_TRANSPORTATION_NUMBER','Y'),
       ('RPS_CARTON_PACKING_SLIP','USE_LICENSE_NUMBER','Y'),
       ('RPS_CARTON_PACKING_SLIP','DATE_TIME_FORMAT','DD/MM/YYYY HH24:MM'),
       ('RPS_CARTON_PACKING_SLIP','DATE_FORMAT','DD/MM/YYYY'),
       ('RPS_CARTON_PACKING_SLIP','TESTING_STATEMENT_TXT','Products packed, inspected and found satisfactory by: %s');

