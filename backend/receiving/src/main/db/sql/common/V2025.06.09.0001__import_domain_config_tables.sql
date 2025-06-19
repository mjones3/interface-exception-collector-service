CREATE TABLE receiving.lk_import_fin_number (
    id int NOT NULL,
    fin_number varchar(5) NOT NULL,
    order_number int NOT NULL DEFAULT 1,
    active boolean NOT NULL DEFAULT true,
    create_date timestamp with time zone NOT NULL,
    modification_date timestamp with time zone NOT NULL,
    CONSTRAINT lk_import_facility_identification_pk PRIMARY KEY (id)
);

COMMENT ON TABLE receiving.lk_import_fin_number IS 'Lookup table for import facility identification';
COMMENT ON COLUMN receiving.lk_import_fin_number.id IS 'Primary key';
COMMENT ON COLUMN receiving.lk_import_fin_number.fin_number IS 'Facility identification number';
COMMENT ON COLUMN receiving.lk_import_fin_number.order_number IS 'Display order for this table';
COMMENT ON COLUMN receiving.lk_import_fin_number.active IS 'Is the record active';
COMMENT ON COLUMN receiving.lk_import_fin_number.create_date IS 'Date the record was created';
COMMENT ON COLUMN receiving.lk_import_fin_number.modification_date IS 'Date the record was modified';

CREATE TABLE receiving.lk_product
(
    id                       int4                     NOT NULL,
    product_code             varchar(10) NULL,
    short_description        varchar(255)              NOT NULL,
    product_family           varchar(50)              NOT NULL,
    active                   boolean default true     NOT NULL,
    create_date              timestamp with time zone NOT NULL,
    modification_date        timestamp with time zone NOT NULL,
    CONSTRAINT pk_lk_product
        PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uq_idx_lk_product_code ON receiving.lk_product (product_code);

COMMENT ON TABLE receiving.lk_product IS 'Lookup table for product details';
COMMENT ON COLUMN receiving.lk_product.id IS 'Primary key';
COMMENT ON COLUMN receiving.lk_product.product_code IS 'Product''s Code';
COMMENT ON COLUMN receiving.lk_product.short_description IS 'Product''s Short Description';
COMMENT ON COLUMN receiving.lk_product.product_family IS 'Product''s Family';
COMMENT ON COLUMN receiving.lk_product.active IS 'Is the record active';
COMMENT ON COLUMN receiving.lk_product.create_date IS 'Date the record was created';
COMMENT ON COLUMN receiving.lk_product.modification_date IS 'Date the record was modified';


CREATE TABLE receiving.lk_product_family
(
    id                   int                      NOT NULL,
    temperature_category varchar(100)             NOT NULL,
    product_family       varchar(100)             NOT NULL,
    description          varchar(255)             NOT NULL,
    order_number         int                      NOT NULL DEFAULT 1,
    active               boolean                  NOT NULL DEFAULT true,
    create_date          timestamp with time zone NOT NULL,
    modification_date    timestamp with time zone NOT NULL,
    CONSTRAINT pk_lk_product_family PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uq_idx_product_family_category_family ON receiving.lk_product_family (temperature_category,product_family);

COMMENT ON COLUMN receiving.lk_product_family.id IS 'Primary key';
COMMENT ON COLUMN receiving.lk_product_family.temperature_category IS 'Product category type';
COMMENT ON COLUMN receiving.lk_product_family.product_family IS 'Product Family type';
COMMENT ON COLUMN receiving.lk_product_family.description IS '';
COMMENT ON COLUMN receiving.lk_product_family.order_number IS 'Display order for this table';
COMMENT ON COLUMN receiving.lk_product_family.active IS 'Is the category group active';
COMMENT ON COLUMN receiving.lk_product_family.create_date IS 'Date the category group was created';
COMMENT ON COLUMN receiving.lk_product_family.modification_date IS 'Date the category group was modified';


CREATE TABLE receiving.lk_product_family_product (
    id                bigserial                NOT NULL,
    product_family_id int                      NOT NULL,
    product_code      varchar(10)              NOT NULL,
    CONSTRAINT pk_lk_product_family_product PRIMARY KEY (id),
    CONSTRAINT fk_lk_product_family_product_family_id FOREIGN KEY (product_family_id) REFERENCES receiving.lk_product_family (id)
);

CREATE UNIQUE INDEX uq_idx_lk_product_family_product ON receiving.lk_product_family_product (product_family_id, product_code);

COMMENT ON TABLE receiving.lk_product_family_product IS 'Lookup table for products that are associated with a family';
COMMENT ON COLUMN receiving.lk_product_family_product.id IS 'Primary key';
COMMENT ON COLUMN receiving.lk_product_family_product.product_family_id IS 'FK Product Family ID';
COMMENT ON COLUMN receiving.lk_product_family_product.product_family_id IS 'Product Code';



