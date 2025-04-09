DROP TABLE recoveredplasmashipping.bld_recovered_plasma_shipment CASCADE ;

DROP SEQUENCE recoveredplasmashipping.bld_recovered_plasma_shipment_number_seq ;

CREATE TABLE recoveredplasmashipping.bld_recovered_plasma_shipment (
        id										        BIGSERIAL               NOT NULL CONSTRAINT pk_bld_recovered_plasma_shipment PRIMARY KEY,
        customer_code   						        VARCHAR(50)             NOT NULL,
        location_code									VARCHAR(255)            NOT NULL,
        product_type                                    VARCHAR(255)            NOT NULL,
        shipment_number 							    varchar(100) NOT NULL,
        status                           				varchar(100) NOT NULL,
        create_employee_id  							varchar(50) NOT NULL,
        close_employee_id 						        varchar(50) NULL,
        close_date 							            TIMESTAMP WITH TIME ZONE NULL,
        transportation_reference_number 				varchar(100) NULL,
        shipment_date 									date NOT NULL,
        carton_tare_weight                              numeric NOT NULL,
        unsuitable_unit_report_document_status          varchar(50) NULL,
        customer_name     						        VARCHAR(255)             NOT NULL,
        customer_state                                  varchar(50) NOT NULL,
        customer_postal_code                            varchar(10) NOT NULL,
        customer_country                                varchar(100) NOT NULL,
        customer_country_code                           varchar(10) NOT NULL,
        customer_city                                   varchar(255) NOT NULL,
        customer_district                               varchar(50) NULL,
        customer_address_line1                          varchar(255) NOT NULL,
        customer_address_line2                          varchar(255) NULL,
        customer_address_contact_name                   varchar(255) NULL,
        customer_address_phone_number                   varchar(255) NULL,
        customer_address_department_name                varchar(255) NULL,
        create_date 									timestamptz NOT NULL,
        modification_date 								timestamptz NOT NULL,
        delete_date 									timestamptz NULL
);

CREATE UNIQUE INDEX uq_idx_bld_recovered_plasma_shipment_number ON recoveredplasmashipping.bld_recovered_plasma_shipment (shipment_number);

CREATE SEQUENCE recoveredplasmashipping.bld_recovered_plasma_shipment_number_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;


