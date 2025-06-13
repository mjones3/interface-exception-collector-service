CREATE TABLE order_service.lk_customer (
     id                 BIGSERIAL               NOT NULL CONSTRAINT pk_lk_customer PRIMARY KEY,
     external_id        VARCHAR(50)             NOT NULL,
     customer_type      VARCHAR(255)            NOT NULL,
     name               VARCHAR(255)            NOT NULL,
     code               VARCHAR(50)             NOT NULL,
     department_code    VARCHAR(50)             NULL,
     department_name    VARCHAR(255)            NULL,
     foreign_flag       CHAR(1)                 NULL,
     phone_number       VARCHAR(255)            NULL,
     active             BOOLEAN                NOT NULL,
     delete_date        TIMESTAMP WITH TIME ZONE NULL,
     create_date        TIMESTAMP WITH TIME ZONE NOT NULL,
     modification_date  TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX uq_idx_lk_customer_code ON order_service.lk_customer (code);

CREATE TABLE order_service.lk_customer_address (
    id                 BIGSERIAL             NOT NULL CONSTRAINT pk_lk_customer_address PRIMARY KEY,
    customer_id        BIGINT                NOT NULL CONSTRAINT fk_lk_customer_address_customer REFERENCES order_service.lk_customer,
    address_type       VARCHAR(50)           NOT NULL,
    contact_name      VARCHAR(255)           NULL,
    state             VARCHAR(50)            NOT NULL,
    postal_code       VARCHAR(10)            NOT NULL,
    country           VARCHAR(100)           NOT NULL,
    country_code      VARCHAR(10)            NOT NULL,
    city              VARCHAR(255)           NOT NULL,
    district          VARCHAR(50)            NULL,
    address_line1     VARCHAR(255)           NOT NULL,
    address_line2     VARCHAR(255)           NULL,
    active             BOOLEAN               NOT NULL,
    delete_date        TIMESTAMP WITH TIME ZONE NULL,
    create_date        TIMESTAMP WITH TIME ZONE NOT NULL,
    modification_date  TIMESTAMP WITH TIME ZONE NOT NULL
);





