CREATE TABLE irradiation.bld_device (
    id BIGSERIAL PRIMARY KEY,
    device_id VARCHAR(255) NOT NULL UNIQUE,
    location VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL
);
