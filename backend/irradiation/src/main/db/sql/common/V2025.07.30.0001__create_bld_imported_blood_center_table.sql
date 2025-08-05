-- Create table for imported blood center information
CREATE TABLE IF NOT EXISTS irradiation.bld_imported_blood_center (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES irradiation.bld_batch_item(id) ON DELETE CASCADE,
    name VARCHAR(70) NOT NULL,
    address VARCHAR(300) NOT NULL,
    registration_number VARCHAR(50) NOT NULL,
    license_number VARCHAR(50),
    create_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modification_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_imported_blood_center_product_id UNIQUE (product_id)
    );

