-- Create new  the bld batch table
CREATE TABLE irradiation.bld_batch (
    id BIGSERIAL PRIMARY KEY,
    device_id VARCHAR(200) NOT NULL REFERENCES irradiation.bld_device(device_id),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP
);

-- Create new  the bld batch items
CREATE TABLE irradiation.bld_batch_item (
    id BIGSERIAL PRIMARY KEY,
    batch_id BIGINT NOT NULL REFERENCES irradiation.bld_batch(id),
    unit_number VARCHAR(100) NOT NULL,
    product_code VARCHAR(100) NOT NULL,
    lot_number VARCHAR(100) NOT NULL
);
