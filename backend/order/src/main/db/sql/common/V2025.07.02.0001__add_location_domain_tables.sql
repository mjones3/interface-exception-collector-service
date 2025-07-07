
CREATE TABLE order_service.lk_location (
   id                BIGSERIAL               NOT NULL CONSTRAINT pk_lk_location PRIMARY KEY,
   external_id       VARCHAR(255)            NULL,
   code              VARCHAR(255)            NOT NULL,
   name              VARCHAR(100)            NOT NULL,
   city              VARCHAR(255)            NOT NULL,
   state             VARCHAR(50)             NOT NULL,
   postal_code       VARCHAR(10)             NOT NULL,
   address_line_1    VARCHAR(255)            NOT NULL,
   address_line_2    VARCHAR(255)            NULL,
   active            BOOLEAN                 NOT NULL,
   create_date                TIMESTAMP WITH TIME ZONE           NOT NULL,
   modification_date          TIMESTAMP WITH TIME ZONE           NOT NULL
);

CREATE UNIQUE INDEX uq_idx_lk_location_code ON order_service.lk_location (code);


CREATE TABLE order_service.lk_location_property
(
    id           BIGSERIAL       NOT NULL CONSTRAINT pk_lk_location_property PRIMARY KEY,
    location_id  BIGINT         NOT NULL CONSTRAINT fk_location_location_property REFERENCES order_service.lk_location,
    property_key VARCHAR(100)   NOT NULL,
    property_value VARCHAR(500)   NOT NULL
);

CREATE UNIQUE INDEX uq_idx_lk_location_property_location_id_code ON order_service.lk_location_property (location_id,property_key);

-- Table comments
COMMENT ON TABLE order_service.lk_location IS 'Stores location information including address details';
COMMENT ON TABLE order_service.lk_location_property IS 'Stores additional properties/attributes for locations';

-- Column comments for lk_location table
COMMENT ON COLUMN order_service.lk_location.id IS 'Primary key - unique identifier for the location';
COMMENT ON COLUMN order_service.lk_location.external_id IS 'External system identifier for integration purposes';
COMMENT ON COLUMN order_service.lk_location.code IS 'Unique code identifier for the location';
COMMENT ON COLUMN order_service.lk_location.name IS 'Name of the location';
COMMENT ON COLUMN order_service.lk_location.city IS 'City where the location is situated';
COMMENT ON COLUMN order_service.lk_location.state IS 'State/province where the location is situated';
COMMENT ON COLUMN order_service.lk_location.postal_code IS 'Postal/ZIP code of the location';
COMMENT ON COLUMN order_service.lk_location.address_line_1 IS 'Primary address line';
COMMENT ON COLUMN order_service.lk_location.address_line_2 IS 'Secondary address line (optional)';
COMMENT ON COLUMN order_service.lk_location.active IS 'Flag indicating if the location is currently active';
COMMENT ON COLUMN order_service.lk_location.create_date IS 'Timestamp when the record was created';
COMMENT ON COLUMN order_service.lk_location.modification_date IS 'Timestamp when the record was last modified';

-- Column comments for lk_location_property table
COMMENT ON COLUMN order_service.lk_location_property.id IS 'Primary key - unique identifier for the location property';
COMMENT ON COLUMN order_service.lk_location_property.location_id IS 'Foreign key reference to lk_location table';
COMMENT ON COLUMN order_service.lk_location_property.property_key IS 'Key identifier for the property';
COMMENT ON COLUMN order_service.lk_location_property.property_value IS 'Value associated with the property key';
