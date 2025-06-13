TRUNCATE TABLE recoveredplasmashipping.lk_customer CASCADE;

INSERT INTO recoveredplasmashipping.lk_customer (
    external_id, customer_type, name, code, department_code, department_name, foreign_flag, phone_number, contact_name, state, postal_code
    , country, country_code, city, district, address_line1, address_line2, active,delete_date, create_date, modification_date )
VALUES ('408','RECOVERED_PLASMA','Prothya','408','','','N','(999)999-9000'
    ,'','NJ','99999','USA','1','North Brunswick','','100 Mountain Blvd.','',true,NULL,now(),now()),
       ('409','RECOVERED_PLASMA','Southern Biologics','409','','','N','(850)224-9829'
       ,'','FL','32303','USA','1','Tallahassee','','4801 Woodlane Circle','Unit 105',true,NULL,now(),now())
,('410','RECOVERED_PLASMA','Bio Products','410','','','N','(470)123-6566'
 ,'','GA','30041','USA','1','Atlanta','','147 Wild Violet St.','',true,NULL,now(),now());
