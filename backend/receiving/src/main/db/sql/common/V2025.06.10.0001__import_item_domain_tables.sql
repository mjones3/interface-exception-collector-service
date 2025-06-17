CREATE TABLE receiving.bld_import_item (
    id bigserial NOT NULL,
    import_id BIGINT NOT NULL,
    unit_number VARCHAR(36) NOT NULL,
    product_code VARCHAR(255) NOT NULL,
    blood_type VARCHAR(50) NOT NULL,
    product_family VARCHAR(255) NOT NULL,
    short_description VARCHAR(255) NOT NULL,
    expiration_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    create_employee_id varchar(50) NOT NULL,
    delete_date TIMESTAMP WITH TIME ZONE,
    create_date TIMESTAMP WITH TIME ZONE NOT NULL,
    modification_date TIMESTAMP WITH TIME ZONE NOT NULL
);

ALTER TABLE receiving.bld_import_item ADD CONSTRAINT bld_import_item_PK PRIMARY KEY (id);
ALTER TABLE receiving.bld_import_item ADD CONSTRAINT bld_import_item_UK UNIQUE (unit_number, product_code);
ALTER TABLE receiving.bld_import_item ADD CONSTRAINT bld_import_item_FK FOREIGN KEY (import_id) REFERENCES receiving.bld_import (id);
CREATE INDEX bld_import_item_IDX_import_id ON receiving.bld_import_item (import_id);
CREATE INDEX bld_import_item_IDX_unit_number ON receiving.bld_import_item (unit_number);
CREATE INDEX bld_import_item_IDX_product_code ON receiving.bld_import_item (product_code);

COMMENT ON COLUMN receiving.bld_import_item.id IS 'Primary key';
COMMENT ON COLUMN receiving.bld_import_item.import_id IS 'FK to BLD_IMPORT';
COMMENT ON COLUMN receiving.bld_import_item.unit_number IS 'Item''s unit number';
COMMENT ON COLUMN receiving.bld_import_item.product_code IS 'Item''s product code';
COMMENT ON COLUMN receiving.bld_import_item.blood_type IS 'Item''s blood type';
COMMENT ON COLUMN receiving.bld_import_item.product_family IS 'Item''s product family';
COMMENT ON COLUMN receiving.bld_import_item.short_description IS 'Item''s product short description';
COMMENT ON COLUMN receiving.bld_import_item.expiration_date IS 'Date the inventory expires';
COMMENT ON COLUMN receiving.bld_import_item.create_employee_id IS 'Import Item ''s Employee id';
COMMENT ON COLUMN receiving.bld_import_item.delete_date IS 'Date the Item was deleted';
COMMENT ON COLUMN receiving.bld_import_item.create_date IS 'Date the Item was created';
COMMENT ON COLUMN receiving.bld_import_item.modification_date IS 'Date the Item was modified';

CREATE TABLE receiving.bld_import_item_property (
    id bigserial NOT NULL,
    import_item_id BIGINT NOT NULL,
    property_key VARCHAR(255) NOT NULL,
    property_value VARCHAR(255) NOT NULL
);

ALTER TABLE receiving.bld_import_item_property ADD CONSTRAINT bld_import_item_property_PK PRIMARY KEY (id);
ALTER TABLE receiving.bld_import_item_property ADD CONSTRAINT bld_import_item_property_FK FOREIGN KEY (import_item_id) REFERENCES receiving.bld_import_item (id);

COMMENT ON COLUMN receiving.bld_import_item_property.id IS 'Primary key';
COMMENT ON COLUMN receiving.bld_import_item_property.import_item_id IS 'FK to BLD_IMPORT_ITEM';
COMMENT ON COLUMN receiving.bld_import_item_property.property_key IS 'Attribute''s property key';
COMMENT ON COLUMN receiving.bld_import_item_property.property_value IS 'Attribute''s property value';


CREATE TABLE receiving.bld_import_item_consequence (
    id bigserial NOT NULL,
    import_item_id BIGINT NOT NULL,
    item_consequence_type VARCHAR(255) NOT NULL,
    item_consequence_reason VARCHAR(255)
);

ALTER TABLE receiving.bld_import_item_consequence ADD CONSTRAINT bld_import_item_consequence_PK PRIMARY KEY (id);
ALTER TABLE receiving.bld_import_item_consequence ADD CONSTRAINT bld_import_item_consequence_FK FOREIGN KEY (import_item_id) REFERENCES receiving.bld_import_item (id);

COMMENT ON COLUMN receiving.bld_import_item_consequence.id IS 'Primary key';
COMMENT ON COLUMN receiving.bld_import_item_consequence.import_item_id IS 'FK to BLD_IMPORT_ITEM';
COMMENT ON COLUMN receiving.bld_import_item_consequence.item_consequence_type IS 'Item''s consequence type';
COMMENT ON COLUMN receiving.bld_import_item_consequence.item_consequence_reason IS 'Item''s consequence reason';


CREATE TABLE receiving.lk_system_process_property
(
    id           BIGSERIAL       NOT NULL CONSTRAINT pk_lk_system_process_property PRIMARY KEY,
    system_process_type VARCHAR(255)   NOT NULL,
    property_key VARCHAR(255)   NOT NULL,
    property_value VARCHAR(500)   NOT NULL
);

CREATE UNIQUE INDEX uq_idx_lk_system_process_property_process_type_key ON receiving.lk_system_process_property (system_process_type,property_key);

COMMENT ON COLUMN receiving.lk_system_process_property.id IS 'Primary key';
COMMENT ON COLUMN receiving.lk_system_process_property.system_process_type IS 'Process Type';
COMMENT ON COLUMN receiving.lk_system_process_property.property_key IS 'Process''s property key';
COMMENT ON COLUMN receiving.lk_system_process_property.property_value IS 'Process''s property value';
