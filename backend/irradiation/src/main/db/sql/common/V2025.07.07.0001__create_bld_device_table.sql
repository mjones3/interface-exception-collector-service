CREATE TABLE irradiation.bld_device (
    id BIGSERIAL PRIMARY KEY,
    device_id VARCHAR(200) NOT NULL UNIQUE,
    location VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL
);
