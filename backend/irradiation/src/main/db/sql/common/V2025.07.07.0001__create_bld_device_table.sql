CREATE TABLE irradiation.bld_device (
    id BIGSERIAL PRIMARY KEY,
    device_id VARCHAR(200) NOT NULL UNIQUE,
    location VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL
);

CREATE TABLE irradiation.bld_batch (
    id BIGSERIAL PRIMARY KEY,
    device_id VARCHAR(200) NOT NULL REFERENCES irradiation.bld_device(device_id),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP
);

CREATE TABLE irradiation.bld_batch_item (
    id BIGSERIAL PRIMARY KEY,
    batch_id BIGINT NOT NULL REFERENCES irradiation.bld_batch(id),
    unit_number VARCHAR(100) NOT NULL,
    product_code VARCHAR(100) NOT NULL,
    lot_number VARCHAR(100) NOT NULL
);
