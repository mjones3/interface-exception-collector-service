
CREATE TABLE recoveredplasmashipping.bld_recovered_plasma_shipment_carton_item (
    id										        BIGSERIAL NOT NULL CONSTRAINT bld_recovered_plasma_shipment_carton_item_pkey PRIMARY KEY ,
    carton_id                                       bigint NOT NULL CONSTRAINT fk_bld_recovered_plasma_shipment_carton_item references recoveredplasmashipping.bld_recovered_plasma_shipment_carton,
    unit_number                 VARCHAR(255) NOT NULL,
    product_code                VARCHAR(255) NOT NULL,
    product_description         VARCHAR(255) NOT NULL,
    abo_rh                      VARCHAR(10) NOT NULL,
    product_type                varchar(255) NULL,
    packed_by_employee_id       varchar(50) NOT NULL,
    status                      VARCHAR(100) NOT NULL,
    volume                      INTEGER NOT NULL,
    weight                      INTEGER NOT NULL,
    expiration_date             TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    collection_date             TIMESTAMP WITH TIME ZONE NULL,
    create_date                 TIMESTAMP WITH TIME ZONE NOT NULL,
    modification_date           TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX uq_idx_bld_recovered_plasma_shipment_carton_item_product ON recoveredplasmashipping.bld_recovered_plasma_shipment_carton_item (carton_id,unit_number,product_code);
