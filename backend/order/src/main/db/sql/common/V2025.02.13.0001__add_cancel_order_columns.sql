ALTER TABLE order_service.bld_order ADD COLUMN cancel_employee_id VARCHAR(50) DEFAULT NULL;
ALTER TABLE order_service.bld_order ADD COLUMN cancel_reason VARCHAR(255) DEFAULT NULL;
ALTER TABLE order_service.bld_order ADD COLUMN cancel_date TIMESTAMP WITH TIME ZONE DEFAULT NULL;
