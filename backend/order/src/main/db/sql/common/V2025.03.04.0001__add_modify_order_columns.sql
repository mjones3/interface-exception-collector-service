ALTER TABLE order_service.bld_order ADD COLUMN modify_employee_id VARCHAR(50) DEFAULT NULL;
ALTER TABLE order_service.bld_order ADD COLUMN modify_reason VARCHAR(255) DEFAULT NULL;
ALTER TABLE order_service.bld_order ADD COLUMN modify_by_process VARCHAR(50) DEFAULT NULL;



