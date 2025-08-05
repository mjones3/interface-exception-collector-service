ALTER TABLE inventory.bld_inventory
    ADD COLUMN carton_number varchar(100);

insert into lk_text_config (context, key_code, text)
values ('INVENTORY_IS_PACKED', 'DEFAULT', 'This product is part of a carton and cannot be added.');
