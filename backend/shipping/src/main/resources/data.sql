-- Check digit
TRUNCATE TABLE lk_lookup;
INSERT INTO lk_lookup (type, description_key, option_value, order_number, active)
VALUES ('SHIPPING_CHECK_DIGIT_ACTIVE', 'shipping-check-digit-active.label', 'true', 1, true)
ON CONFLICT DO NOTHING;
