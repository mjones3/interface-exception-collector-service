CREATE TABLE receiving.bld_import (
     id bigserial NOT NULL,
     temperature_category varchar(100) NOT NULL,
     transit_start_date_time TIMESTAMP WITHOUT TIME ZONE NULL,
     transit_start_time_zone varchar(50),
     transit_end_date_time TIMESTAMP WITHOUT TIME ZONE NULL,
     transit_end_time_zone varchar(50),
     total_transit_time varchar(50),
     transit_time_result varchar(100),
     temperature  numeric,
     thermometer_code varchar(50),
     temperature_result varchar(100),
     location_code varchar(100) NOT NULL,
     comments varchar(255),
     status varchar(50) NOT NULL,
     employee_id varchar(50) NOT NULL,
     delete_date TIMESTAMP WITH TIME ZONE,
     create_date TIMESTAMP WITH TIME ZONE NOT NULL,
     modification_date TIMESTAMP WITH TIME ZONE NOT NULL,
     PRIMARY KEY (id)
);

COMMENT ON TABLE receiving.bld_import IS 'Table that stores the transactional data for the imports process.';

COMMENT ON COLUMN receiving.bld_import.id IS 'Primary key';
COMMENT ON COLUMN receiving.bld_import.temperature_category IS 'Import''s temperature category - FROZEN/REFRIGERATED/ROOM_TEMPERATURE';
COMMENT ON COLUMN receiving.bld_import.transit_start_date_time IS 'Import''s start transit date';
COMMENT ON COLUMN receiving.bld_import.transit_start_time_zone IS 'Import''s start transit time zone';
COMMENT ON COLUMN receiving.bld_import.transit_end_date_time IS 'Import''s end transit date';
COMMENT ON COLUMN receiving.bld_import.transit_end_time_zone IS 'Import''s end transit time zone';
COMMENT ON COLUMN receiving.bld_import.total_transit_time IS 'Import''s total transit time';
COMMENT ON COLUMN receiving.bld_import.transit_time_result IS 'Import''s transit time result - ACCEPTABLE/UNACCEPTABLE';
COMMENT ON COLUMN receiving.bld_import.temperature IS 'Import''s temperature';
COMMENT ON COLUMN receiving.bld_import.temperature_result IS 'Import''s temperature result - ACCEPTABLE/UNACCEPTABLE';
COMMENT ON COLUMN receiving.bld_import.thermometer_code IS 'Import''s Thermometer Code';
COMMENT ON COLUMN receiving.bld_import.location_code IS 'Import''s Location Code';
COMMENT ON COLUMN receiving.bld_import.comments IS 'Import''s comments';
COMMENT ON COLUMN receiving.bld_import.status IS 'Import''s status - PENDING/COMPLETE';
COMMENT ON COLUMN receiving.bld_import.employee_id IS 'Import''s Employee id';
COMMENT ON COLUMN receiving.bld_import.delete_date IS 'Date the import was deleted';
COMMENT ON COLUMN receiving.bld_import.create_date IS 'Date the import was created';
COMMENT ON COLUMN receiving.bld_import.modification_date IS 'Date the import was modified';

