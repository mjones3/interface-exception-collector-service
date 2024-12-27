CREATE SCHEMA IF NOT EXISTS inventory;

CREATE TABLE inventory.bld_inventory
(
    id                          uuid NOT NULL,
    unit_number                 VARCHAR(13) NOT NULL,
    product_code                VARCHAR(30) NOT NULL,
    short_description           VARCHAR(255) NOT NULL,
    status                      VARCHAR(255) NOT NULL,
    expiration_date             TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    collection_date             TIMESTAMP WITH TIME ZONE NOT NULL,
    location                    VARCHAR(255) NOT NULL,
    product_family              VARCHAR(255) NOT NULL,
    abo_rh                      VARCHAR(3) NOT NULL,
    weight                      INTEGER,
    is_licensed                 BOOLEAN,
    create_date                 TIMESTAMP WITH TIME ZONE NOT NULL,
    modification_date           TIMESTAMP WITH TIME ZONE NOT NULL,
    device_stored               VARCHAR(255),
    storage_location            VARCHAR(255),
    status_reason               VARCHAR(255),
    quarantines                 JSONB,
    histories                   JSONB,
    comments                     VARCHAR(255),
    is_labeled                  BOOLEAN DEFAULT false,
    CONSTRAINT pk_bld_inventory PRIMARY KEY (id)
);

CREATE TABLE inventory.lk_text_config
(
    id                          UUID NOT NULL DEFAULT gen_random_uuid(),
    context                     VARCHAR(255) NOT NULL,
    key_code                    VARCHAR(255) NOT NULL,
    text                        VARCHAR(255) NOT NULL,
    CONSTRAINT pk_lk_text_config PRIMARY KEY (id)
);


CREATE TABLE inventory.lk_product_family
(
    id                          UUID NOT NULL DEFAULT gen_random_uuid(),
    product_family                     VARCHAR(255) NOT NULL,
    time_frame                         INTEGER NOT NULL,
    CONSTRAINT pk_lk_product_family PRIMARY KEY (id)
);
