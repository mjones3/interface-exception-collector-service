-- Check digit
TRUNCATE TABLE lk_lookup;
INSERT INTO lk_lookup (type, description_key, option_value, order_number, active)
VALUES ('SHIPPING_CHECK_DIGIT_ACTIVE', 'shipping-check-digit-active.label', 'true', 1, true),
       ('SHIPPING_VISUAL_INSPECTION_ACTIVE', 'shipping-visual-inspection-active.label', 'true', 1, true),
       ('SHIPPING_SECOND_VERIFICATION_ACTIVE', 'shipping-second-verification-active.label', 'true', 1, true)
    ON CONFLICT DO NOTHING;


-- Discard Reasons
TRUNCATE TABLE lk_reason;
INSERT INTO lk_reason (type, reason_key, require_comments, order_number, active)
VALUES ('VISUAL_INSPECTION_FAILED', 'BROKEN', false, 2, true),
       ('VISUAL_INSPECTION_FAILED', 'AGGREGATES', false, 1, true),
       ('VISUAL_INSPECTION_FAILED', 'DEFECTIVE_BAG', false, 4, true),
       ('VISUAL_INSPECTION_FAILED', 'CLOTTED', false, 3, true),
       ('VISUAL_INSPECTION_FAILED', 'OTHER', true, 5, true)
    ON CONFLICT DO NOTHING;
