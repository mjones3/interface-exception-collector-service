-- Remove unique constraint on external_id to allow duplicate orders for retries
-- This allows the same external_id to exist multiple times with different transaction_ids

-- Drop the unique index on external_id
DROP INDEX IF EXISTS idx_partner_orders_external_id_unique;

-- Keep the regular index for performance
-- (idx_partner_orders_external_id already exists from V001)

-- Add comment explaining the change
COMMENT ON COLUMN partner_orders.external_id IS 'External order identifier - can have duplicates for retry scenarios';