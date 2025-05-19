DELETE FROM recoveredplasmashipping.lk_system_process_property WHERE system_process_type = 'RPS_SHIPPING_SUMMARY_REPORT';

INSERT INTO recoveredplasmashipping.lk_system_process_property (system_process_type,property_key,property_value)
VALUES ('RPS_SHIPPING_SUMMARY_REPORT','TESTING_STATEMENT_TXT','All products in this shipment meet FDA and American Red Cross testing requirements. These units are acceptable for further manufacture.'),
       ('RPS_SHIPPING_SUMMARY_REPORT','USE_TESTING_STATEMENT','Y'),
       ('RPS_SHIPPING_SUMMARY_REPORT','USE_HEADER_SECTION','Y'),
       ('RPS_SHIPPING_SUMMARY_REPORT','HEADER_SECTION_TXT','American Red Cross'),
       ('RPS_SHIPPING_SUMMARY_REPORT','USE_TRANSPORTATION_NUMBER','Y'),
       ('RPS_SHIPPING_SUMMARY_REPORT','DATE_TIME_FORMAT','MM/dd/yyyy HH:mm z'),
       ('RPS_SHIPPING_SUMMARY_REPORT','DATE_FORMAT','MM/dd/yyyy'),
       ('RPS_SHIPPING_SUMMARY_REPORT','ADDRESS_FORMAT','{address} {city}, {state}, {zipCode} {country}'),
       ('RPS_SHIPPING_SUMMARY_REPORT','BLOOD_CENTER_NAME','American Red Cross');


