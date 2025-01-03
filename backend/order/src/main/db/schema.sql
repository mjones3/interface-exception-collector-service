CREATE SCHEMA IF NOT EXISTS order_service;

CREATE TABLE order_service.lk_lookup (
    id                BIGSERIAL                NOT NULL CONSTRAINT pk_lk_lookup PRIMARY KEY,
    type              VARCHAR(50)              NOT NULL,
    description_key   VARCHAR(255)             NOT NULL,
    option_value      VARCHAR(255)             NOT NULL,
    order_number      INTEGER DEFAULT 1        NOT NULL,
    active            BOOLEAN                  NOT NULL
);
CREATE UNIQUE INDEX uq_idx_lk_lookup_type_option_value ON order_service.lk_lookup (type, option_value);

CREATE TABLE order_service.lk_order_blood_type (
    id                BIGSERIAL                NOT NULL CONSTRAINT pk_lk_order_blood_type PRIMARY KEY,
    product_family    VARCHAR(255)             NOT NULL,
    blood_type        VARCHAR(255)             NOT NULL,
    description_key   VARCHAR(255)             NOT NULL,
    order_number      INTEGER                  NOT NULL,
    active            BOOLEAN                  NOT NULL,
    create_date       TIMESTAMP WITH TIME ZONE NOT NULL,
    modification_date TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX uq_idx_lk_order_blood_type ON order_service.lk_order_blood_type (product_family, blood_type);

CREATE TABLE order_service.lk_order_product_family (
    id                BIGSERIAL                NOT NULL CONSTRAINT pk_lk_order_product_family PRIMARY KEY,
    family_category   VARCHAR(255)             NOT NULL,
    family_type       VARCHAR(255)             NOT NULL,
    description_key   VARCHAR(255)             NOT NULL,
    product_family    VARCHAR(255)             NOT NULL,
    order_number      INTEGER                  NOT NULL,
    active            BOOLEAN                  NOT NULL,
    create_date       TIMESTAMP WITH TIME ZONE NOT NULL,
    modification_date TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX uq_idx_lk_order_product_family ON order_service.lk_order_product_family (family_category, product_family);

CREATE TABLE order_service.bld_order (
    id                         BIGSERIAL                          NOT NULL CONSTRAINT pk_bld_order PRIMARY KEY,
    order_number               BIGSERIAL                          NOT NULL,
    external_id                VARCHAR(50),
    location_code              VARCHAR(255)                       NOT NULL,
    shipment_type              VARCHAR(100) DEFAULT 'CUSTOMER'    NOT NULL,
    shipping_method            VARCHAR(255)                       NOT NULL,
    shipping_customer_name     VARCHAR(255)                       NOT NULL,
    shipping_customer_code     VARCHAR(10)                        NOT NULL,
    billing_customer_name      VARCHAR(255)                       NOT NULL,
    billing_customer_code      VARCHAR(10)                        NOT NULL,
    desired_shipping_date      DATE                               NOT NULL,
    will_call_pickup           BOOLEAN,
    phone_number               VARCHAR(50),
    product_category           VARCHAR(255)                       NOT NULL,
    comments                   VARCHAR(1000),
    status                     VARCHAR(255)                       NOT NULL,
    priority                   integer                            NOT NULL,
    delivery_type              VARCHAR(255)                       NOT NULL,
    create_employee_id         VARCHAR(50)                        NOT NULL,
    create_date                TIMESTAMP WITH TIME ZONE           NOT NULL,
    modification_date          TIMESTAMP WITH TIME ZONE           NOT NULL,
    delete_date                TIMESTAMP WITH TIME ZONE
);

CREATE UNIQUE INDEX uq_idx_bld_order_external_id ON order_service.bld_order (external_id);

CREATE TABLE order_service.bld_order_item (
    id                BIGSERIAL                   NOT NULL CONSTRAINT pk_bld_order_item PRIMARY KEY,
    order_id          BIGINT                      NOT NULL CONSTRAINT fk_order_order_item references order_service.bld_order,
    product_family    VARCHAR(255)                NOT NULL,
    blood_type        VARCHAR(5)                  NOT NULL,
    quantity          INTEGER                     NOT NULL,
    quantity_shipped  INTEGER                     NOT NULL DEFAULT 0,
    comments          VARCHAR(1000),
    create_date       TIMESTAMP WITH TIME ZONE    NOT NULL,
    modification_date TIMESTAMP WITH TIME ZONE    NOT NULL
);

CREATE TABLE order_service.bld_order_shipment (
    id                BIGSERIAL                   NOT NULL CONSTRAINT pk_bld_order_shipment PRIMARY KEY,
    order_id          BIGINT                      NOT NULL CONSTRAINT fk_order_order_shipment references order_service.bld_order(id),
    shipment_id       BIGINT                      NOT NULL ,
    shipment_status   VARCHAR(255)                NOT NULL,
    create_date       TIMESTAMP WITH TIME ZONE    NOT NULL,
    modification_date TIMESTAMP WITH TIME ZONE    NOT NULL
);

CREATE UNIQUE INDEX uq_idx_bld_order_shipment_id ON order_service.bld_order_shipment (order_id,shipment_id);
