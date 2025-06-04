TRUNCATE TABLE receiving.lk_location CASCADE;

INSERT INTO receiving.lk_location (id,external_id,code,name,city,state,postal_code,address_line_1,address_line_2,active,create_date,modification_date)
VALUES
    (1,'123456789','123456789','MDL Hub 1','Charlotte','NC','28209','444 Main St.','',true,now(),now()),
    (2,'DL1','DL1','Distribution and Labeling','Hialeah','FL','33016','555 Main St.','',true,now(),now()),
    (3,'DO1','DO1','Distribution Only','Pensacola','FL','32514','666 Main St.','',true,now(),now()),
    (4,'234567891','234567891','MDL Hub 2','Peoria','IL','61605','999 Main St.','',true,now(),now());

INSERT INTO receiving.lk_location_property (location_id,property_key,property_value)
VALUES
    -- Location 1 properties
    (1,'LABEL_ADDRESS_TYPE','3'),
    (1,'LICENSE_NUMBER','2222'),
    (1,'TZ','America/New_York'),
    (1,'REGISTRATION_NUMBER','3004054586'),
    (1,'PHONE_NUMBER','123-456-7894'),
    -- Location 2 properties
    (2,'TZ','America/New_York'),
    (2,'PHONE_NUMBER','123-456-7895'),
    -- Location 3 properties
    (3,'TZ','America/Chicago'),
    (3,'PHONE_NUMBER','123-456-7896'),
    -- Location 4 properties
    (4,'TZ','America/New_York'),
    (4,'PHONE_NUMBER','123-456-7899');

