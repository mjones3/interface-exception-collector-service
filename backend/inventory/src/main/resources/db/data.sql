truncate lk_text_config;
truncate lk_product_family;

insert into lk_text_config (context, key_code, text)
values ('INVENTORY_NOT_FOUND_IN_LOCATION', 'DEFAULT', 'This product is not in this location and cannot be shipped.');
insert into lk_text_config (context, key_code, text)
values ('INVENTORY_IS_EXPIRED', 'DEFAULT', 'This product is expired and has been discarded. Place in biohazard container.');
insert into lk_text_config (context, key_code, text)
values ('INVENTORY_IS_UNSUITABLE', 'DEFAULT', 'This product has been discarded for %s. Place in biohazard container.');
insert into lk_text_config (context, key_code, text)
values ('INVENTORY_IS_QUARANTINED', 'DEFAULT', 'This product is currently in quarantine and needs to be returned to storage.');
insert into lk_text_config (context, key_code, text)
values ('INVENTORY_IS_DISCARDED', 'DEFAULT', 'This product has already been discarded for %s in the system. Place in biohazard container.');
insert into lk_text_config (context, key_code, text)
values ('INVENTORY_NOT_EXIST', 'DEFAULT', 'This product does not exist and cannot be shipped.');
insert into lk_text_config (context, key_code, text)
values ('INVENTORY_IS_SHIPPED', 'DEFAULT', 'This product was previously shipped.');

insert into lk_text_config (context, key_code, text)
values ('INVENTORY_IS_UNSUITABLE', 'ACTIVE_DEFERRAL', 'This product has an active deferral with a discard consequence and has been discarded. Place in biohazard container.');

insert into lk_text_config (context, key_code, text)
values ('INVENTORY_IS_QUARANTINED_DETAIL', 'PENDING_FURTHER_REVIEW_INSPECTION', 'PENDING FURTHER REVIEW / INSPECTION');

insert into lk_product_family (product_family, time_frame)
values ('PLASMA_TRANSFUSABLE', 30);
insert into lk_product_family (product_family, time_frame)
values ('RED_BLOOD_CELLS', 5);
insert into lk_product_family (product_family, time_frame)
values ('RED_BLOOD_CELLS_LEUKOREDUCED', 5);
insert into lk_product_family (product_family, time_frame)
values ('WHOLE_BLOOD', 5);
insert into lk_product_family (product_family, time_frame)
values ('WHOLE_BLOOD_LEUKOREDUCED', 5);

