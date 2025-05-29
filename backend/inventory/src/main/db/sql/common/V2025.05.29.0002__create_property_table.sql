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
