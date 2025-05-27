DELETE FROM recoveredplasmashipping.lk_system_process_property WHERE system_process_type = 'RPS_CARTON_LABEL';

INSERT INTO recoveredplasmashipping.lk_system_process_property (system_process_type,property_key,property_value)
VALUES ('RPS_CARTON_LABEL','USE_TRANSPORTATION_NUMBER','Y'),
       ('RPS_CARTON_LABEL','BLOOD_CENTER_NAME','American Red Cross'),
       ('RPS_CARTON_LABEL','USE_TOTAL_CARTONS','N');

