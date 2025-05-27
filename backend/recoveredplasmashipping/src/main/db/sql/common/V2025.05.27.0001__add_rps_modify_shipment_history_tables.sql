CREATE TABLE recoveredplasmashipping.bld_recovered_plasma_shipment_history (
    id							BIGSERIAL NOT NULL CONSTRAINT bld_recovered_plasma_shipment_history_pkey PRIMARY KEY ,
    shipment_id                 bigint NOT NULL CONSTRAINT fk_bld_recovered_plasma_shipment_history_shipment references recoveredplasmashipping.bld_recovered_plasma_shipment,
    comments                    VARCHAR(255) NOT NULL,
    create_employee_id  		varchar(50) NOT NULL,
    create_date                 TIMESTAMP WITH TIME ZONE NOT NULL
);

COMMENT ON TABLE recoveredplasmashipping.bld_recovered_plasma_shipment_history IS 'Stores the history of changes made on bld_recovered_plasma_shipment table.';
COMMENT ON COLUMN recoveredplasmashipping.bld_recovered_plasma_shipment_history.id IS 'Primary key';
COMMENT ON COLUMN recoveredplasmashipping.bld_recovered_plasma_shipment_history.shipment_id IS 'Shipment ID';
COMMENT ON COLUMN recoveredplasmashipping.bld_recovered_plasma_shipment_history.comments IS 'Comments provided by the user who made the change';
COMMENT ON COLUMN recoveredplasmashipping.bld_recovered_plasma_shipment_history.create_employee_id IS 'Employee ID who made the change';
COMMENT ON COLUMN recoveredplasmashipping.bld_recovered_plasma_shipment_history.create_date IS 'Date the history was created';



