CREATE TABLE recoveredplasmashipping.bld_unacceptable_units_report (
    id							BIGSERIAL NOT NULL CONSTRAINT bld_unacceptable_units_report_pkey PRIMARY KEY ,
    shipment_id                 bigint NOT NULL CONSTRAINT fk_unacceptable_units_report_shipment references recoveredplasmashipping.bld_recovered_plasma_shipment,
    unit_number                 VARCHAR(255) NOT NULL,
    product_code                VARCHAR(255) NOT NULL,
    carton_number             	VARCHAR(100) NOT NULL,
    carton_sequence_number      INT NOT NULL,
    failure_reason              TEXT DEFAULT NULL,
    create_date                 TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX uq_idx_bld_unacceptable_units_report_product_shipment ON recoveredplasmashipping.bld_unacceptable_units_report (shipment_id,unit_number,product_code);



INSERT INTO recoveredplasmashipping.lk_system_process_property (system_process_type,property_key,property_value)
VALUES ('RPS_UNACCEPTABLE_UNITS_REPORT','DATE_TIME_FORMAT','MM/dd/yyyy HH:mm z');
