
CREATE TABLE shipping.bld_product_location_history (
    id                         BIGSERIAL NOT NULL
       CONSTRAINT pk_bld_product_location_history PRIMARY KEY,
    customer_code_to          VARCHAR(50) NOT NULL,
    customer_name_to          VARCHAR(255) NOT NULL,
    customer_code_from        VARCHAR(50) NULL,
    customer_name_from        VARCHAR(255) NULL,
    unit_number               VARCHAR(255) NOT NULL,
    product_code              VARCHAR(255) NOT NULL,
    history_type              VARCHAR(50)  NOT NULL,
    created_by_employee_id    VARCHAR(50) NOT NULL,
    create_date                TIMESTAMP WITH TIME ZONE NOT NULL,
    modification_date          TIMESTAMP WITH TIME ZONE NOT NULL,
    delete_date                TIMESTAMP WITH TIME ZONE
);

CREATE TABLE shipping.bld_external_transfer(
    id                         BIGSERIAL NOT NULL
       CONSTRAINT pk_bld_external_transfer PRIMARY KEY,
    hospital_transfer_id      VARCHAR(255) NULL,
    customer_code_to          VARCHAR(50) NOT NULL,
    customer_name_to          VARCHAR(255) NOT NULL,
    customer_code_from        VARCHAR(50)   NULL,
    customer_name_from        VARCHAR(255)  NULL,
    status                    VARCHAR(50)  NOT NULL,
    transfer_date             DATE NOT NULL,
    created_by_employee_id    VARCHAR(50) NOT NULL,
    create_date                TIMESTAMP WITH TIME ZONE NOT NULL,
    modification_date          TIMESTAMP WITH TIME ZONE NOT NULL,
    delete_date                TIMESTAMP WITH TIME ZONE
);


