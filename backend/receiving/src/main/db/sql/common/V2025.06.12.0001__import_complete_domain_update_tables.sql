ALTER TABLE receiving.bld_import ADD COLUMN complete_employee_id varchar(50) NULL;
ALTER TABLE receiving.bld_import ADD COLUMN complete_date TIMESTAMP WITH TIME ZONE NULL;

COMMENT ON COLUMN receiving.bld_import.complete_date IS 'Date the import was completed';
COMMENT ON COLUMN receiving.bld_import.complete_employee_id IS 'Employee ID that completed the import';


