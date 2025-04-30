CREATE TABLE recoveredplasmashipping.lk_system_process_property
(
    id           BIGSERIAL       NOT NULL CONSTRAINT pk_lk_system_process_property PRIMARY KEY,
    system_process_type VARCHAR(255)   NOT NULL,
    property_key VARCHAR(255)   NOT NULL,
    property_value VARCHAR(500)   NOT NULL
);

CREATE UNIQUE INDEX uq_idx_lk_system_process_property_process_type_key ON recoveredplasmashipping.lk_location_property (system_process_type,property_key);
