ALTER TABLE order_service.bld_order ADD COLUMN ship_to_location_code VARCHAR(255) NULL;
ALTER TABLE order_service.bld_order ADD COLUMN quarantined_products BOOLEAN DEFAULT false;
ALTER TABLE order_service.bld_order ADD COLUMN label_status VARCHAR(50) NOT NULL DEFAULT 'LABELED';

ALTER TABLE order_service.bld_order ALTER COLUMN shipping_customer_name drop not null;
ALTER TABLE order_service.bld_order ALTER COLUMN shipping_customer_code drop not null;
ALTER TABLE order_service.bld_order ALTER COLUMN billing_customer_name drop not null;
ALTER TABLE order_service.bld_order ALTER COLUMN billing_customer_code drop not null;


COMMENT ON COLUMN order_service.bld_order.ship_to_location_code IS 'Ship to Location Code';
COMMENT ON COLUMN order_service.bld_order.quarantined_products IS 'Indicates if the order can be filled with quarantined products or not';
COMMENT ON COLUMN order_service.bld_order.label_status IS 'Label Status LABELED/UNLABELED';

INSERT INTO lk_lookup (type, description_key, option_value, order_number, active)
VALUES ('ORDER_SHIPMENT_TYPE', 'order-shipment-type.internal-transfer.label', 'INTERNAL_TRANSFER', 2, true);
