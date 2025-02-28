
CREATE TABLE shipping.bld_external_transfer_item (
    id                         BIGSERIAL NOT NULL
       CONSTRAINT pk_bld_external_transfer_item PRIMARY KEY,
    external_transfer_id BIGINT NOT NULL
        CONSTRAINT fk_external_transfer_item REFERENCES shipping.bld_external_transfer,
    unit_number               VARCHAR(255) NOT NULL,
    product_code              VARCHAR(255) NOT NULL,
    product_family            VARCHAR(255) NOT NULL,
    created_by_employee_id    VARCHAR(50) NOT NULL,
    create_date                TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX idx_bld_external_transfer_item ON shipping.bld_external_transfer_item (unit_number, product_code ,external_transfer_id);


ALTER TABLE shipping.bld_product_location_history ADD column  product_family VARCHAR(255) NULL;

