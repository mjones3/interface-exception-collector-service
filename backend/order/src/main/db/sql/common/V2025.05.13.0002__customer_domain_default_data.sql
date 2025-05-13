TRUNCATE TABLE order_service.lk_customer CASCADE;

INSERT INTO order_service.lk_customer (id,external_id,customer_type,name,code,department_code,department_name,foreign_flag,phone_number,active,create_date,modification_date)
VALUES (1,'99789','1','Creative Testing Solutions','A1235','18563','Ortho','N','123-456-7890',true,now(),now()),
       (2,'99890','1','Advanced Medical Center','B2346','27654','Cardiology','N','234-567-8901',true,now(),now()),
       (3,'99901','1','Pioneer Health Services','C3457','38765','Neurology','N','345-678-9012',true,now(),now()),
       (4,'99912','1','Sunrise Health Clinic','D4568','49876','Pediatrics','N','456-789-0123',true,now(),now()),
       (5,'99923','1','Harmony Medical Group','E5679','50987','Dermatology','N','567-890-1234',true,now(),now());

INSERT INTO order_service.lk_customer_address (customer_id,address_type,contact_name,state,postal_code,country,country_code,city,district,address_line1,address_line2,active,create_date,modification_date)
VALUES (1,'SHIPPING','Jane Smith','VA','20152','US','55','chantilly','DA','123 Main street','APT 1',true,now(),now()),
       (1,'BILLING','Jane Smith','VA','10040','US','55','Burlington','VT','123 Main street','APT 1',true,now(),now()),
       (2,'SHIPPING','John Smith','CA','90210','US','55','BurBeverly Hills','LA','456 Elm Street','Suite 200',true,now(),now()),
       (2,'BILLING','Jane Doe','CA','90001','US','55','Los Angeles','LA','789 Oak Avenue','Floor 3',true,now(),now()),
       (3,'SHIPPING','Alice Johnson','TX','73301','US','55','Austin','TX','1234 Pine Street','Building A',true,now(),now()),
       (3,'BILLING','Bob Williams','TX','75001','US','55','Addison','TX','5678 Maple Drive','Suite 101',true,now(),now()),
       (4,'SHIPPING','Michael Brown','FL','33101','US','55','Miami','FL','9101 Sunset Boulevard','Suite 500',true,now(),now()),
       (4,'BILLING','Sarah Green','FL','33301','US','55','Fort Lauderdale','FL','1122 Ocean Drive','Apt 3B',true,now(),now()),
       (5,'SHIPPING','Emily Davis','NY','10001','US','55','New York','NY','123 Broadway','Suite 400',true,now(),now()),
       (5,'BILLING','David Miller','NY','10002','US','55','New York','NY','456 Fifth Avenue','Floor 2',true,now(),now());
