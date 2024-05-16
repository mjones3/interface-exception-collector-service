CREATE TABLE bld_shipping_service
(
    id                int8 NOT NULL,
    create_date       TIMESTAMP WITH TIME ZONE NOT NULL,
    modification_date TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_bld_shipping_service PRIMARY KEY (id)
);
