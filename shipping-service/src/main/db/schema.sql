CREATE TABLE bld_shipping_service
(
    id                int8 NOT NULL,
    create_date       TIMESTAMP WITH TIME ZONE NOT NULL,
    modification_date TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_bld_shipping_service PRIMARY KEY (id)
);

CREATE TABLE bld_order (
    -- order
                           id BIGSERIAL NOT NULL,
                           order_number int4 NOT NULL,
                           location_code int8 NULL,
                           delivery_type varchar(255) NOT NULL,
                           shipping_method varchar(255) NOT NULL,
                           product_category varchar(255) NOT NULL,
                           shipping_date DATE NOT NULL,
                           status varchar(255) NOT NULL,
                           priority varchar(255) NOT NULL,
                           create_date timestamptz NOT NULL,
    --- customer
                           shipping_customer_code int8 NULL,
                           shipping_customer_name varchar(255) NOT NULL,
                           billing_customer_name varchar(255) NOT NULL,
                           billing_customer_code int8 NULL,
                           customer_phone_number varchar(255) NULL,
    --- customer address
                           customer_address_contact_name varchar(255) NULL,
                           customer_address_state varchar(50) NOT NULL,
                           customer_address_postal_code varchar(10) NOT NULL,
                           customer_address_country varchar(10) NOT NULL,
                           customer_address_country_code varchar(10) NOT NULL,
                           customer_address_city varchar(255) NOT NULL,
                           customer_address_district varchar(50) NULL,
                           customer_address_line1 varchar(255) NOT NULL,
                           customer_address_line2 varchar(255) NULL,
                           CONSTRAINT pk_bld_order PRIMARY KEY (id)
);

CREATE TABLE bld_order_item (
                                id BIGSERIAL NOT NULL,
                                order_id int8 NOT NULL,
                                product_family varchar(255) NOT NULL,
                                blood_type varchar(255) NOT NULL,
                                quantity int4 NOT NULL,
                                "comments" varchar(1000) NULL,
                                create_date timestamptz NOT NULL,
                                CONSTRAINT pk_bld_order_item PRIMARY KEY (id),
                                CONSTRAINT fk_order_order_item FOREIGN KEY (order_id) REFERENCES bld_order(id)
);

-- SHIPMENT
CREATE TABLE bld_shipment (
    id                         BIGSERIAL NOT NULL
        CONSTRAINT pk_bld_shipment PRIMARY KEY,
    order_id                   BIGINT NOT NULL
        CONSTRAINT fk_bld_shipment_order_id REFERENCES bld_order,
    customer_code              BIGINT,
    location_code              INTEGER NOT NULL,
    delivery_type              VARCHAR(255)             NOT NULL,
    shipment_method            VARCHAR(255)             NOT NULL,
    status_key                 VARCHAR(255)             NOT NULL,
    state                      VARCHAR(50)              NOT NULL,
    postal_code                VARCHAR(10)              NOT NULL,
    country                    VARCHAR(10)              NOT NULL,
    country_code               VARCHAR(10)              NOT NULL,
    city                       VARCHAR(255)             NOT NULL,
    district                   VARCHAR(50),
    address_line1              VARCHAR(255)             NOT NULL,
    address_line2              VARCHAR(255),
    create_date                TIMESTAMP WITH TIME ZONE NOT NULL,
    modification_date          TIMESTAMP WITH TIME ZONE NOT NULL,
    delete_date                TIMESTAMP WITH TIME ZONE
);
CREATE TABLE bld_shipment_item (
    id BIGSERIAL               NOT NULL
        CONSTRAINT pk_bld_shipment_item PRIMARY KEY,
    shipment_id BIGINT         NOT NULL
        CONSTRAINT fk_shipment_shipment_item REFERENCES bld_shipment,
    unit_number                VARCHAR(255) NOT NULL,
    product_code               VARCHAR(255) NOT NULL,
    create_date                TIMESTAMP WITH TIME ZONE NOT NULL,
    modification_date          TIMESTAMP WITH TIME ZONE NOT NULL
);

-- PRODUCT
CREATE TABLE bld_product (
    id                         BIGSERIAL NOT NULL
        CONSTRAINT pk_bld_product PRIMARY KEY,
    unit_number                VARCHAR(36) NOT NULL,
    product_code               VARCHAR(50) NOT NULL,
    product_description        VARCHAR(255) NOT NULL,
    isbt_product_code          VARCHAR(20) NOT NULL,
    expiration_date            TIMESTAMP WITH TIME ZONE NOT NULL,
    quarantined                BOOLEAN NOT NULL,
    discarded                  BOOLEAN NOT NULL,
    labeled                    BOOLEAN NOT NULL,
    storage_location           VARCHAR(255) NOT NULL,
    create_date                TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE UNIQUE INDEX idx_bld_product_key ON bld_product (unit_number, product_code);
