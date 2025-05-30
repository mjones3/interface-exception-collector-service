CREATE TABLE inventory.bld_inventory_property
(
    id                          uuid NOT NULL,
    key                         VARCHAR(255) NOT NULL,
    value                       VARCHAR(255) NOT NULL,
    inventory_id                          uuid NOT NULL,
    create_date                 TIMESTAMP WITH TIME ZONE NOT NULL,
    modification_date           TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_bld_inventory_property PRIMARY KEY (id)
);

ALTER TABLE inventory.bld_inventory_property
ADD CONSTRAINT fk_inventory_property_inventory
FOREIGN KEY (inventory_id)
REFERENCES inventory.bld_inventory (id)
ON DELETE CASCADE;
