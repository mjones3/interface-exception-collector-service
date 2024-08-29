-- SHIPMENT
CREATE TABLE bld_shipment (
    id                         BIGSERIAL NOT NULL
        CONSTRAINT pk_bld_shipment PRIMARY KEY,
    order_number                   BIGINT NOT NULL,
    customer_code              BIGINT,
    customer_name varchar(255) NOT NULL,
    customer_phone_number varchar(255) NULL,
    location_code              VARCHAR(255) NOT NULL,
    delivery_type              VARCHAR(255)             NOT NULL,
    priority varchar(255) NOT NULL,
    shipment_method            VARCHAR(255)             NOT NULL,
    product_category varchar(255) NOT NULL,
    status                 VARCHAR(255)             NOT NULL,
    state                      VARCHAR(50)              NOT NULL,
    postal_code                VARCHAR(10)              NOT NULL,
    country                    VARCHAR(10)              NOT NULL,
    country_code               VARCHAR(10)              NOT NULL,
    city                       VARCHAR(255)             NOT NULL,
    district                   VARCHAR(50),
    address_line1              VARCHAR(255)             NOT NULL,
    address_line2              VARCHAR(255),
    address_contact_name varchar(255) NULL,
    shipping_date DATE NOT NULL,
    create_date                TIMESTAMP WITH TIME ZONE NOT NULL,
    modification_date          TIMESTAMP WITH TIME ZONE NOT NULL,
    delete_date                TIMESTAMP WITH TIME ZONE,
    "comments" varchar(1000) NULL,
    department_name varchar(255) NULL,
    created_by_employee_id varchar(50) NULL,
    completed_by_employee_id varchar(50) NULL,
    complete_date TIMESTAMP WITH TIME ZONE
);

CREATE TABLE bld_shipment_item (
    id BIGSERIAL               NOT NULL
       CONSTRAINT pk_bld_shipment_item PRIMARY KEY,
    shipment_id BIGINT         NOT NULL
       CONSTRAINT fk_shipment_shipment_item REFERENCES bld_shipment,

    product_family varchar(255) NOT NULL,
    blood_type varchar(255) NOT NULL,
    quantity int4 NOT NULL,
    "comments" varchar(1000) NULL,
    create_date                TIMESTAMP WITH TIME ZONE NOT NULL,
    modification_date          TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE bld_shipment_item_short_date_product (
    id BIGSERIAL               NOT NULL
       CONSTRAINT pk_bld_shipment_item_short_date_product PRIMARY KEY,
    shipment_item_id BIGINT         NOT NULL
       CONSTRAINT fk_shipment_shipment_item_short_date REFERENCES bld_shipment_item,
    unit_number                VARCHAR(255) NOT NULL,
    product_code               VARCHAR(255) NOT NULL,
    storage_location               VARCHAR(255) NULL,
    "comments" varchar(1000) NULL,
    create_date                TIMESTAMP WITH TIME ZONE NOT NULL,
    modification_date          TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE bld_shipment_item_packed (
    id BIGSERIAL               NOT NULL
        CONSTRAINT pk_bld_shipment_item_packed PRIMARY KEY,
    shipment_item_id BIGINT         NOT NULL
        CONSTRAINT fk_shipment_item_shipment_item_packed REFERENCES bld_shipment_item,
    unit_number                VARCHAR(255) NOT NULL,
    product_code               VARCHAR(255) NOT NULL,
    product_description        VARCHAR(255) NOT NULL,
    abo_rh                     VARCHAR(10) NOT NULL,
    packed_by_employee_id      varchar(50) NOT NULL,
    expiration_date          TIMESTAMP WITH TIME ZONE NOT NULL,
    collection_date          TIMESTAMP WITH TIME ZONE NULL,
    create_date                TIMESTAMP WITH TIME ZONE NOT NULL,
    modification_date          TIMESTAMP WITH TIME ZONE NOT NULL,
    visual_inspection          VARCHAR(50) NOT NULL
);

CREATE UNIQUE INDEX idx_bld_shipment_item_packed ON bld_shipment_item_packed (unit_number, product_code ,shipment_item_id);

ALTER TABLE bld_shipment ALTER COLUMN customer_code TYPE VARCHAR(50) using (customer_code::varchar);

ALTER TABLE bld_shipment_item_packed ADD COLUMN blood_type VARCHAR(10) NOT NULL;

ALTER TABLE bld_shipment_item_packed ADD COLUMN product_family varchar(255) NULL;
