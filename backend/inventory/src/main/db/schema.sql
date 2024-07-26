CREATE TABLE bld_inventory
(
    id                int8 NOT NULL,
    create_date       TIMESTAMP WITH TIME ZONE NOT NULL,
    modification_date TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_bld_inventory PRIMARY KEY (id)
);
