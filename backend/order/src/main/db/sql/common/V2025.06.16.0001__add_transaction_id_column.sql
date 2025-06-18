ALTER TABLE order_service.bld_order ADD COLUMN transaction_id uuid NULL;

CREATE UNIQUE INDEX uq_transaction_id ON order_service.bld_order (transaction_id);
