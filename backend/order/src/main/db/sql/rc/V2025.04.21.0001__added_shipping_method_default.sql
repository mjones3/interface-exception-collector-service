UPDATE order_service.lk_lookup SET order_number = order_number + 1;

INSERT INTO order_service.lk_lookup (type, description_key, option_value, order_number, active)
    VALUES ('ORDER_SHIPPING_METHOD', 'order-shipping-method.default.label', 'DEFAULT', 1, true)
    ON CONFLICT DO NOTHING;
