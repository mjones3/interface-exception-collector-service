
CREATE TABLE recoveredplasmashipping.bld_recovered_plasma_shipment_carton (
    id										        BIGSERIAL NOT NULL CONSTRAINT bld_recovered_plasma_shipment_carton_pkey PRIMARY KEY ,
    recovered_plasma_shipment_id                    bigint NOT NULL CONSTRAINT fk_bld_recovered_plasma_shipment_carton references recoveredplasmashipping.bld_recovered_plasma_shipment,
    carton_number             				        varchar(100) NOT NULL,
    carton_sequence_number             				int NOT NULL,
    status                           				varchar(100) NOT NULL,
    create_employee_id 									varchar(50) NOT NULL,
    close_employee_id 						        varchar(50) NULL,
    close_date 							            timestamptz NULL,
    delete_date 									timestamptz NULL,
    create_date 									timestamptz NOT NULL,
    modification_date 								timestamptz NOT NULL
);

CREATE UNIQUE INDEX uq_idx_bld_recovered_plasma_shipment_carton_number ON recoveredplasmashipping.bld_recovered_plasma_shipment_carton (carton_number);

CREATE UNIQUE INDEX uq_idx_bld_recovered_plasma_shipment_carton_sequence ON recoveredplasmashipping.bld_recovered_plasma_shipment_carton (recovered_plasma_shipment_id,carton_sequence_number) WHERE (delete_date is null);

CREATE SEQUENCE recoveredplasmashipping.bld_recovered_plasma_shipment_carton_number_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;
