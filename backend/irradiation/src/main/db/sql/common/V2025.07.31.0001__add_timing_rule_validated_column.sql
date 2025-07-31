-- Add is_timing_rule_validated column to bld_batch_item table
ALTER TABLE bld_batch_item
ADD COLUMN is_timing_rule_validated BOOLEAN NOT NULL DEFAULT FALSE;
