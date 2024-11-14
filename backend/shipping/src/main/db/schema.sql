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

ALTER TABLE bld_shipment_item_packed ALTER COLUMN expiration_date TYPE TIMESTAMP WITHOUT TIME ZONE ;

ALTER TABLE bld_shipment ADD COLUMN external_id VARCHAR(50) DEFAULT NULL;

CREATE TABLE lk_lookup (
    id                BIGSERIAL                NOT NULL CONSTRAINT pk_lk_lookup PRIMARY KEY,
    type              VARCHAR(50)              NOT NULL,
    description_key   VARCHAR(255)             NOT NULL,
    option_value      VARCHAR(255)             NOT NULL,
    order_number      INTEGER DEFAULT 1        NOT NULL,
    active            BOOLEAN                  NOT NULL
);

CREATE UNIQUE INDEX uq_idx_lk_lookup_type_option_value ON lk_lookup (type, option_value);

CREATE TABLE lk_reason
(
    id               BIGSERIAL    NOT NULL CONSTRAINT pk_lk_reason PRIMARY KEY,
    type             varchar(255) NOT NULL,
    reason_key       varchar(255) NOT NULL,
    require_comments  BOOLEAN                  NOT NULL,
    order_number      INTEGER DEFAULT 1        NOT NULL,
    active            BOOLEAN                  NOT NULL
);

CREATE UNIQUE INDEX uq_idx_lk_reason_type_key ON lk_reason (type, reason_key);


ALTER TABLE bld_shipment_item_packed ADD COLUMN second_verification VARCHAR(50) DEFAULT NULL;

ALTER TABLE bld_shipment_item_packed ADD COLUMN verification_date TIMESTAMP WITH TIME ZONE DEFAULT NULL;

ALTER TABLE bld_shipment_item_packed ADD COLUMN verified_by_employee_id varchar(50) DEFAULT NULL;

ALTER TABLE bld_shipment_item_packed ADD COLUMN ineligible_status varchar(100) DEFAULT NULL;

ALTER TABLE bld_shipment_item_packed ADD COLUMN ineligible_action varchar(100) DEFAULT NULL;

ALTER TABLE bld_shipment_item_packed ADD COLUMN ineligible_reason varchar(100) DEFAULT NULL;

ALTER TABLE bld_shipment_item_packed ADD COLUMN ineligible_message varchar(255) DEFAULT NULL;


CREATE TABLE bld_shipment_item_removed (
    id BIGSERIAL               NOT NULL
      CONSTRAINT pk_bld_shipment_item_removed PRIMARY KEY,
    shipment_id BIGINT         NOT NULL
      CONSTRAINT fk_shipment_shipment_item_removed REFERENCES bld_shipment,
    unit_number                VARCHAR(255) NOT NULL,
    product_code               VARCHAR(255) NOT NULL,
    removed_by_employee_id      varchar(50) NOT NULL,
    removed_date                TIMESTAMP WITH TIME ZONE NOT NULL,
    ineligible_status          VARCHAR(100) NOT NULL
);

CREATE UNIQUE INDEX idx_bld_shipment_item_removed ON bld_shipment_item_removed (unit_number, product_code ,shipment_id);
