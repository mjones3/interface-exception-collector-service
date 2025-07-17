CREATE TABLE receiving.bld_internal_transfer (
    id bigserial NOT NULL,
    order_number bigint NOT NULL,
    external_order_id varchar(255) NOT NULL,
    temperature_category varchar(100) NOT NULL,
    location_code_from varchar(100) NOT NULL,
    location_code_to varchar(100) NOT NULL,
    label_status varchar(50) NOT NULL DEFAULT 'LABELED',
    quarantined_products boolean NOT NULL DEFAULT false,
    employee_id varchar(50) NOT NULL,
    create_date timestamp with time zone NOT NULL,
    modification_date timestamp with time zone NOT NULL,
    delete_date timestamp with time zone,
    CONSTRAINT bld_internal_transfer_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE receiving.bld_internal_transfer IS 'Table that will store the transactional data for the Internal Transfers';
COMMENT ON COLUMN receiving.bld_internal_transfer.id IS 'Primary key';
COMMENT ON COLUMN receiving.bld_internal_transfer.order_number IS 'Internal Transfer''s order number';
COMMENT ON COLUMN receiving.bld_internal_transfer.temperature_category IS 'Internal Transfer''s product temperature category';
COMMENT ON COLUMN receiving.bld_internal_transfer.external_order_id IS 'Internal Transfer''s External Order ID';
COMMENT ON COLUMN receiving.bld_internal_transfer.location_code_from IS 'Location Code From';
COMMENT ON COLUMN receiving.bld_internal_transfer.location_code_to IS 'Location Code To';
COMMENT ON COLUMN receiving.bld_internal_transfer.label_status IS 'Internal Transfer''s Label Status LABELED/UNLABELED';
COMMENT ON COLUMN receiving.bld_internal_transfer.quarantined_products IS 'Has Quarantined Products true/false';
COMMENT ON COLUMN receiving.bld_internal_transfer.employee_id IS 'Internal Transfer''s Employee id';
COMMENT ON COLUMN receiving.bld_internal_transfer.create_date IS 'Date the shipment was created';
COMMENT ON COLUMN receiving.bld_internal_transfer.modification_date IS 'Date the shipment was modified';
COMMENT ON COLUMN receiving.bld_internal_transfer.delete_date IS 'Date the shipment was deleted';

CREATE TABLE receiving.bld_internal_transfer_item (
    id bigserial NOT NULL,
    internal_transfer_id bigint NOT NULL,
    unit_number varchar(36) NOT NULL,
    product_code varchar(255) NOT NULL,
    product_description varchar(255) NOT NULL,
    create_date timestamp with time zone NOT NULL,
    modification_date timestamp with time zone NOT NULL,
    CONSTRAINT bld_internal_transfer_item_pkey PRIMARY KEY (id),
    CONSTRAINT fk_internal_transfer FOREIGN KEY (internal_transfer_id)
        REFERENCES receiving.bld_internal_transfer(id)
);

COMMENT ON TABLE receiving.bld_internal_transfer_item IS 'Table that will store the transactional data for the items from internal transfers';
COMMENT ON COLUMN receiving.bld_internal_transfer_item.id IS 'Primary key';
COMMENT ON COLUMN receiving.bld_internal_transfer_item.internal_transfer_id IS 'Item''s Internal Transfer ID - FK to bld_internal_transfer';
COMMENT ON COLUMN receiving.bld_internal_transfer_item.unit_number IS 'Item''s unit number';
COMMENT ON COLUMN receiving.bld_internal_transfer_item.product_code IS 'Item''s product code';
COMMENT ON COLUMN receiving.bld_internal_transfer_item.product_description IS 'Item''s product description';
COMMENT ON COLUMN receiving.bld_internal_transfer_item.create_date IS 'Date the item was created';
COMMENT ON COLUMN receiving.bld_internal_transfer_item.modification_date IS 'Date the item was modified';

CREATE TABLE receiving.bld_transfer_receipt (
    id bigserial NOT NULL,
    order_number varchar(255) NOT NULL,
    external_order_id varchar(255) NOT NULL,
    temperature_category varchar(255) NOT NULL,
    transit_start_date_time timestamp,
    transit_end_date_time timestamp,
    transit_time_zone varchar(50),
    total_transit_time varchar(50),
    transit_time_result varchar(255),
    temperature  numeric,
    thermometer_code varchar(50),
    location_code varchar(100) NOT NULL,
    received_different_location boolean NOT NULL DEFAULT false,
    status varchar(50) NOT NULL,
    comments varchar(250),
    employee_id varchar(50) NOT NULL,
    delete_date timestamp with time zone,
    create_date timestamp with time zone NOT NULL,
    modification_date timestamp with time zone NOT NULL,
    CONSTRAINT bld_transfer_receipt_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE receiving.bld_transfer_receipt IS 'Table that will store the transactional data for the Transfer Receipt';
COMMENT ON COLUMN receiving.bld_transfer_receipt.id IS 'Primary key';
COMMENT ON COLUMN receiving.bld_transfer_receipt.order_number IS 'Transfer Receipt''s order number';
COMMENT ON COLUMN receiving.bld_transfer_receipt.external_order_id IS 'Transfer Receipt''s External Order ID';
COMMENT ON COLUMN receiving.bld_transfer_receipt.temperature_category IS 'Transfer Receipt''s temperature category - FROZEN/REFRIGERATED/ROOM_TEMPERATURE';
COMMENT ON COLUMN receiving.bld_transfer_receipt.transit_start_date_time IS 'Transfer Receipt''s start transit time';
COMMENT ON COLUMN receiving.bld_transfer_receipt.transit_end_date_time IS 'Transfer Receipt''s end transit time';
COMMENT ON COLUMN receiving.bld_transfer_receipt.transit_time_zone IS 'Transfer Receipt''s time zone';
COMMENT ON COLUMN receiving.bld_transfer_receipt.total_transit_time IS 'Transfer Receipt''s total transit time';
COMMENT ON COLUMN receiving.bld_transfer_receipt.transit_time_result IS 'Transfer Receipt''s transit time result - ACCEPTABLE/UNACCEPTABLE';
COMMENT ON COLUMN receiving.bld_transfer_receipt.temperature IS 'Transfer Receipt''s temperature';
COMMENT ON COLUMN receiving.bld_transfer_receipt.thermometer_code IS 'Transfer Receipt''s Thermometer Code';
COMMENT ON COLUMN receiving.bld_transfer_receipt.location_code IS 'Transfer Receipt''s Location Code';
COMMENT ON COLUMN receiving.bld_transfer_receipt.received_different_location IS 'Transfer Receipt''s Transfer was received in different location true/false';
COMMENT ON COLUMN receiving.bld_transfer_receipt.status IS 'Transfer Receipt''s status COMPLETE/PENDING';
COMMENT ON COLUMN receiving.bld_transfer_receipt.comments IS 'Transfer Receipt''s comments';
COMMENT ON COLUMN receiving.bld_transfer_receipt.employee_id IS 'Transfer Receipt''s Employee id';
COMMENT ON COLUMN receiving.bld_transfer_receipt.delete_date IS 'Date the Transfer Receipt was deleted';
COMMENT ON COLUMN receiving.bld_transfer_receipt.create_date IS 'Date the Transfer Receipt was created';
COMMENT ON COLUMN receiving.bld_transfer_receipt.modification_date IS 'Date the Transfer Receipt was modified';


CREATE TABLE receiving.bld_transfer_receipt_item (
    id bigserial NOT NULL,
    transfer_receipt_id bigint NOT NULL,
    unit_number varchar(36) NOT NULL,
    product_code varchar(255) NOT NULL,
    visual_inspection varchar(50) NOT NULL DEFAULT 'SATISFACTORY',
    delete_date timestamp with time zone,
    create_date timestamp with time zone NOT NULL,
    modification_date timestamp with time zone NOT NULL,
    CONSTRAINT bld_transfer_receipt_item_pkey PRIMARY KEY (id),
    CONSTRAINT fk_transfer_receipt FOREIGN KEY (transfer_receipt_id)
        REFERENCES receiving.bld_transfer_receipt(id)
);


COMMENT ON TABLE receiving.bld_transfer_receipt_item IS 'Table that will store the transactional data for the Transfer Receipt''s items';
COMMENT ON COLUMN receiving.bld_transfer_receipt_item.id IS 'Primary key';
COMMENT ON COLUMN receiving.bld_transfer_receipt_item.transfer_receipt_id IS 'Item''s Transfer Receipt ID - FK to bld_transfer_receipt';
COMMENT ON COLUMN receiving.bld_transfer_receipt_item.unit_number IS 'Item''s unit number';
COMMENT ON COLUMN receiving.bld_transfer_receipt_item.product_code IS 'Item''s product code';
COMMENT ON COLUMN receiving.bld_transfer_receipt_item.visual_inspection IS 'Item''s visual inspection - SATISFACTORY/UNSATISFACTORY';
COMMENT ON COLUMN receiving.bld_transfer_receipt_item.delete_date IS 'Date the item was deleted';
COMMENT ON COLUMN receiving.bld_transfer_receipt_item.create_date IS 'Date the item was created';
COMMENT ON COLUMN receiving.bld_transfer_receipt_item.modification_date IS 'Date the item was modified';


CREATE TABLE receiving.bld_transfer_receipt_item_consequence (
    id bigserial NOT NULL,
    internal_transfer_item_id bigint NOT NULL,
    item_consequence_type varchar(255) NOT NULL,
    item_consequence_reason_key varchar(255),
    create_date timestamp with time zone NOT NULL,
    modification_date timestamp with time zone NOT NULL,
    CONSTRAINT bld_transfer_receipt_item_consequence_pkey PRIMARY KEY (id),
    CONSTRAINT fk_transfer_receipt_item FOREIGN KEY (internal_transfer_item_id)
        REFERENCES receiving.bld_transfer_receipt_item(id)
);

COMMENT ON TABLE receiving.bld_transfer_receipt_item_consequence IS 'Table that will store the transactional data for the Transfer Receipt''s items consequences';
COMMENT ON COLUMN receiving.bld_transfer_receipt_item_consequence.id IS 'Primary key';
COMMENT ON COLUMN receiving.bld_transfer_receipt_item_consequence.internal_transfer_item_id IS 'FK to BLD_TRANSFER_RECEIPT_ITEM';
COMMENT ON COLUMN receiving.bld_transfer_receipt_item_consequence.item_consequence_type IS 'Item''s consequence type';
COMMENT ON COLUMN receiving.bld_transfer_receipt_item_consequence.item_consequence_reason_key IS 'Item''s consequence reason key';
COMMENT ON COLUMN receiving.bld_transfer_receipt_item_consequence.create_date IS 'Date the consequence was created';
COMMENT ON COLUMN receiving.bld_transfer_receipt_item_consequence.modification_date IS 'Date the consequence was modified';


