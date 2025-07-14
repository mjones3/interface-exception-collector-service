-- Adding missing audit fields to BLD tables as per database standards

ALTER TABLE irradiation.bld_device
ALTER COLUMN create_date TYPE TIMESTAMP WITH TIME ZONE,
ALTER COLUMN modification_date TYPE TIMESTAMP WITH TIME ZONE;

-- Add delete_date and user_time_zone to bld_batch table
ALTER TABLE irradiation.bld_batch
ADD COLUMN delete_date TIMESTAMP WITH TIME ZONE,
ALTER COLUMN create_date TYPE TIMESTAMP WITH TIME ZONE,
ALTER COLUMN modification_date TYPE TIMESTAMP WITH TIME ZONE;

-- Add delete_date and user_time_zone to bld_batch_item table
ALTER TABLE irradiation.bld_batch_item
ADD COLUMN delete_date TIMESTAMP WITH TIME ZONE,
ALTER COLUMN create_date TYPE TIMESTAMP WITH TIME ZONE,
ALTER COLUMN modification_date TYPE TIMESTAMP WITH TIME ZONE;


