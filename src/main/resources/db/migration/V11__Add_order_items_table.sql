-- Add order_items table to store order item details from OrderRejected events
-- This supports the new schema structure with bloodType and productFamily

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    interface_exception_id BIGINT NOT NULL,
    blood_type VARCHAR(10) NOT NULL,
    product_family VARCHAR(50) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    CONSTRAINT fk_order_items_interface_exception 
        FOREIGN KEY (interface_exception_id) 
        REFERENCES interface_exceptions(id) 
        ON DELETE CASCADE
);

-- Add indexes for performance
CREATE INDEX idx_order_items_interface_exception_id ON order_items(interface_exception_id);
CREATE INDEX idx_order_items_blood_type ON order_items(blood_type);
CREATE INDEX idx_order_items_product_family ON order_items(product_family);

-- Add comments for documentation
COMMENT ON TABLE order_items IS 'Stores order item details from OrderRejected events';
COMMENT ON COLUMN order_items.interface_exception_id IS 'Foreign key to interface_exceptions table';
COMMENT ON COLUMN order_items.blood_type IS 'Blood type for the order item (e.g., O+, A-, AB+)';
COMMENT ON COLUMN order_items.product_family IS 'Product family classification (e.g., WHOLE_BLOOD, PLATELETS)';
COMMENT ON COLUMN order_items.quantity IS 'Quantity requested for this item';

-- Add trigger for updated_at timestamp
CREATE OR REPLACE FUNCTION update_order_items_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_order_items_updated_at
    BEFORE UPDATE ON order_items
    FOR EACH ROW
    EXECUTE FUNCTION update_order_items_updated_at();