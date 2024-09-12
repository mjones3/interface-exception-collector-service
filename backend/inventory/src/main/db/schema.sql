CREATE TABLE bld_inventory
(
    id                          uuid NOT NULL,
    unit_number                 VARCHAR(13) NOT NULL,
    product_code                VARCHAR(8) NOT NULL CHECK (LENGTH(product_code) >= 7),
    short_description           VARCHAR(255) NOT NULL,
    status                      VARCHAR(255) NOT NULL,
    expiration_date             TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    collection_date             VARCHAR(255) NOT NULL,
    location                    VARCHAR(255) NOT NULL,
    product_family              VARCHAR(255) NOT NULL,
    abo_rh                      VARCHAR(3) NOT NULL,
    create_date                 TIMESTAMP WITH TIME ZONE NOT NULL,
    modification_date           TIMESTAMP WITH TIME ZONE NOT NULL,
    device_stored               VARCHAR(255),
    storage_location            VARCHAR(255),
    status_reason               VARCHAR(255),
    quarantines                 JSONB,
    histories                   JSONB,
    comments                     VARCHAR(255),
    CONSTRAINT pk_bld_inventory PRIMARY KEY (id)
);

CREATE TABLE lk_text_config
(
    id                          UUID NOT NULL DEFAULT gen_random_uuid(),
    context                     VARCHAR(255) NOT NULL,
    key_code                         VARCHAR(255) NOT NULL,
    text                        VARCHAR(255) NOT NULL,
    CONSTRAINT pk_lk_text_config PRIMARY KEY (id)
);
