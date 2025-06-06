CREATE TABLE receiving.bld_device
(
    id                bigserial                NOT NULL,
    type              varchar(50)              NOT NULL,
    category          varchar(50)              NOT NULL,
    serial_number     varchar(50)              NOT NULL,
    location          varchar(50)              NOT NULL,
    name              varchar(50)              NOT NULL,
    active            boolean                  NOT NULL,
    create_date       TIMESTAMP WITH TIME ZONE NOT NULL,
    modification_date TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_bld_device
        PRIMARY KEY (id),
    CONSTRAINT uk_bld_device_type_serial_number
        UNIQUE (type, serial_number)
);

COMMENT ON TABLE receiving.bld_device IS 'Stores the devices to be used in the receiving process.';
COMMENT ON COLUMN receiving.bld_device.id IS 'The unique identifier of the device.';
COMMENT ON COLUMN receiving.bld_device.category IS 'The Category of the device.';
COMMENT ON COLUMN receiving.bld_device.type IS 'The type of the device.';
COMMENT ON COLUMN receiving.bld_device.serial_number IS 'The serial number of the device.';
COMMENT ON COLUMN receiving.bld_device.location IS 'The location of the device.';
COMMENT ON COLUMN receiving.bld_device.name IS 'The name of the device.';
COMMENT ON COLUMN receiving.bld_device.active IS 'Indicates whether the device is active or not.';
COMMENT ON COLUMN receiving.bld_device.create_date IS 'The date and time when the device was created.';
COMMENT ON COLUMN receiving.bld_device.modification_date IS 'The date and time when the device was last modified.';
COMMENT ON CONSTRAINT pk_bld_device ON receiving.bld_device IS 'Primary key constraint for the bld_device table.';
COMMENT ON CONSTRAINT uk_bld_device_type_serial_number ON receiving.bld_device IS 'Unique constraint for the type and serial number of the device.';






