-- Add new columns to bld_batch_item table
ALTER TABLE irradiation.bld_batch_item
ADD COLUMN IF NOT EXISTS new_product_code VARCHAR(100),
ADD COLUMN IF NOT EXISTS expiration_date TIMESTAMP WITHOUT TIME ZONE,
ADD COLUMN IF NOT EXISTS product_family VARCHAR(255);
