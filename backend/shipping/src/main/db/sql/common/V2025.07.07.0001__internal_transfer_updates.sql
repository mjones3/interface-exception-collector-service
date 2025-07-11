ALTER TABLE shipping.bld_shipment ADD COLUMN quarantined_products BOOLEAN DEFAULT false;
ALTER TABLE shipping.bld_shipment ADD COLUMN label_status VARCHAR(50) NOT NULL DEFAULT 'LABELED';
ALTER TABLE shipping.bld_shipment ADD COLUMN shipment_type VARCHAR(100) DEFAULT 'CUSTOMER' NOT NULL;
ALTER TABLE shipping.bld_shipment_item_packed ADD COLUMN product_status VARCHAR(50) NULL;

COMMENT ON COLUMN shipping.bld_shipment.quarantined_products IS 'Indicates if the shipment can be filled with quarantined products or not';
COMMENT ON COLUMN shipping.bld_shipment.shipment_type IS 'Type of Shipment CUSTOMER,INTERNAL_TRANSFER,RESEARCH_PRODUCTS';
COMMENT ON COLUMN shipping.bld_shipment.label_status IS 'Label Status LABELED/UNLABELED';
