-- Partner Orders table for storing order submissions and payloads
CREATE TABLE IF NOT EXISTS partner_orders (
    id BIGSERIAL PRIMARY KEY,
    transaction_id UUID NOT NULL UNIQUE,
    external_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'RECEIVED',
    original_payload JSONB NOT NULL,
    location_code VARCHAR(255),
    product_category VARCHAR(255),
    submitted_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_partner_orders_transaction_id ON partner_orders(transaction_id);
CREATE INDEX IF NOT EXISTS idx_partner_orders_external_id ON partner_orders(external_id);
CREATE INDEX IF NOT EXISTS idx_partner_orders_status ON partner_orders(status);
CREATE INDEX IF NOT EXISTS idx_partner_orders_submitted_at ON partner_orders(submitted_at);
CREATE INDEX IF NOT EXISTS idx_partner_orders_location_code ON partner_orders(location_code);

-- Unique constraint on external_id to prevent duplicates
CREATE UNIQUE INDEX IF NOT EXISTS idx_partner_orders_external_id_unique ON partner_orders(external_id);

-- Order Items table for storing individual order line items
CREATE TABLE IF NOT EXISTS partner_order_items (
    id BIGSERIAL PRIMARY KEY,
    partner_order_id BIGINT NOT NULL REFERENCES partner_orders(id) ON DELETE CASCADE,
    product_family VARCHAR(255) NOT NULL,
    blood_type VARCHAR(50) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    comments TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for order items
CREATE INDEX IF NOT EXISTS idx_partner_order_items_partner_order_id ON partner_order_items(partner_order_id);
CREATE INDEX IF NOT EXISTS idx_partner_order_items_product_family ON partner_order_items(product_family);
CREATE INDEX IF NOT EXISTS idx_partner_order_items_blood_type ON partner_order_items(blood_type);

-- Event Log table for tracking all events published
CREATE TABLE IF NOT EXISTS partner_order_events (
    id BIGSERIAL PRIMARY KEY,
    event_id UUID NOT NULL UNIQUE,
    event_type VARCHAR(100) NOT NULL,
    event_version VARCHAR(10) NOT NULL DEFAULT '1.0',
    transaction_id UUID NOT NULL,
    correlation_id UUID NOT NULL,
    source VARCHAR(100) NOT NULL DEFAULT 'partner-order-service',
    topic VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for event log
CREATE INDEX IF NOT EXISTS idx_partner_order_events_event_id ON partner_order_events(event_id);
CREATE INDEX IF NOT EXISTS idx_partner_order_events_transaction_id ON partner_order_events(transaction_id);
CREATE INDEX IF NOT EXISTS idx_partner_order_events_event_type ON partner_order_events(event_type);
CREATE INDEX IF NOT EXISTS idx_partner_order_events_published_at ON partner_order_events(published_at);