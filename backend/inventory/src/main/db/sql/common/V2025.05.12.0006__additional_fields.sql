ALTER TABLE inventory.bld_inventory
    RENAME COLUMN location TO inventory_location;

ALTER TABLE inventory.bld_inventory
    ADD COLUMN collection_location VARCHAR(255) NULL;

ALTER TABLE inventory.bld_inventory
    ADD COLUMN collection_timezone VARCHAR(255) NULL;
