TRUNCATE TABLE recoveredplasmashipping.lk_location CASCADE;

INSERT INTO recoveredplasmashipping.lk_location (id,external_id,code,name,city,state,postal_code,address_line_1,address_line_2,active,create_date, modification_date)
VALUES (1,'123456789','123456789','MDL Hub 1','Charlotte','NC','28209','444 Main St.','',true,now(),now()),
       (2,'234567891','234567891','MDL Hub 2','Peoria','IL','61605','999 Main St.','',true,now(),now());

INSERT INTO recoveredplasmashipping.lk_location_property (location_id,property_key,property_value)
VALUES (1,'RPS_LOCATION_SHIPMENT_CODE','2765'),
      (1, 'RPS_PARTNER_PREFIX','BPM'),
      (1, 'RPS_LOCATION_CARTON_CODE','MH1'),
      (1, 'RPS_USE_PARTNER_PREFIX','Y'),
      (2,'RPS_LOCATION_SHIPMENT_CODE','406'),
      (2, 'RPS_LOCATION_CARTON_CODE','MH1'),
      (2, 'RPS_USE_PARTNER_PREFIX','N');
