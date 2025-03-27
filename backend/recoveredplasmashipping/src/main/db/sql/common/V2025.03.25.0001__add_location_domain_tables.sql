
CREATE TABLE recoveredplasmashipping.lk_location (
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

CREATE UNIQUE INDEX uq_idx_lk_location_code ON recoveredplasmashipping.lk_location (code);


CREATE TABLE recoveredplasmashipping.lk_location_property
(
    id           BIGSERIAL       NOT NULL CONSTRAINT pk_lk_location_property PRIMARY KEY,
    location_id  BIGINT         NOT NULL CONSTRAINT fk_location_location_property REFERENCES recoveredplasmashipping.lk_location,
    property_key VARCHAR(100)   NOT NULL,
    property_value VARCHAR(500)   NOT NULL
);

CREATE UNIQUE INDEX uq_idx_lk_location_property_location_id_code ON recoveredplasmashipping.lk_location_property (location_id,property_key);
