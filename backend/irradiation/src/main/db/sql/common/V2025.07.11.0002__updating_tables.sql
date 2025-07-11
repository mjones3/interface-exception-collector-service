-- Adding the active, create_date and modification_date into the lk_configuration table

ALTER TABLE irradiation.lk_configuration
ADD COLUMN active BOOLEAN NOT NULL DEFAULT true,
ADD COLUMN create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN modification_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Adding the create_date and modification_date into the bld_device table

ALTER TABLE irradiation.bld_device
ADD COLUMN create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN modification_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Adding the create_date and modification_date into the bld_batch table
ALTER TABLE irradiation.bld_batch
ADD COLUMN create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN modification_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Adding the create_date and modification_date into the bld_batch_item table
ALTER TABLE irradiation.bld_batch_item
ADD COLUMN create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN modification_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

