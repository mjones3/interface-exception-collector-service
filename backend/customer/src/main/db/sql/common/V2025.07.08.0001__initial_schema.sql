CREATE SCHEMA IF NOT EXISTS customer;

CREATE TABLE customer.bld_customer
(
    id                BIGSERIAL                NOT NULL
        CONSTRAINT pk_bld_customer PRIMARY KEY,
    external_id       character varying(255)   NOT NULL
        CONSTRAINT un_bld_customer_id UNIQUE,
    name              character varying(50)    NOT NULL,
    code              character varying(50)    NOT NULL,
    department_code    character varying(50),
    department_name    character varying(50),
    phone_number       character varying(15),
    foreign_flag       character varying(2),
    customer_type              character varying(10),
    active            character varying(25)
);

CREATE TABLE customer.bld_customer_address
(
    id                BIGSERIAL                NOT NULL
        CONSTRAINT pk_bld_customer_address PRIMARY KEY,
    customer_id          bigint                   NOT NULL
        CONSTRAINT fk_bld_customer_address_customer REFERENCES customer.bld_customer (id),
    contact_name       character varying(50),
    address_type       character varying(50),
    address_line1      character varying(255),
    address_line2      character varying(255),
    city               character varying(50),
    state              character varying(50),
    postal_code        character varying(20),
    district           character varying(20),
    country            character varying(50),
    country_code       character varying(20),
    active             character varying(20),
    create_date       timestamp with time zone NOT NULL,
    modification_date timestamp with time zone NOT NULL,
    delete_date       timestamp with time zone
);
CREATE INDEX idx_bld_customer_address_customer_id ON customer.bld_customer_address(customer_id);
