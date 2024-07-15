-- Shipping Methods
INSERT INTO lk_lookup (type, description_key, option_value, order_number, active) VALUES
  ('ORDER_SHIPPING_METHOD', 'order-shipping-method.shuttle.label', 'SHUTTLE', 1, true),
  ('ORDER_SHIPPING_METHOD', 'order-shipping-method.fedex.label', 'FEDEX', 2, true),
  ('ORDER_SHIPPING_METHOD', 'order-shipping-method.courier.label', 'COURIER', 3, true)
  ON CONFLICT DO NOTHING;

-- Priority
INSERT INTO lk_lookup (type, description_key, option_value, order_number, active) VALUES
  ('ORDER_PRIORITY', 'order-priority.stat.label',      'STAT',      1, true),
  ('ORDER_PRIORITY', 'order-priority.asap.label',      'ASAP',      2, true),
  ('ORDER_PRIORITY', 'order-priority.routine.label',   'ROUTINE',   3, true),
  ('ORDER_PRIORITY', 'order-priority.scheduled.label', 'SCHEDULED', 4, true)
  ON CONFLICT DO NOTHING;

-- Order Statuses
INSERT INTO lk_lookup (type, description_key, option_value, order_number, active) VALUES
  ('ORDER_STATUS', 'order-status.all.label',         'ALL',         1, true),
  ('ORDER_STATUS', 'order-status.created.label',     'CREATED',     2, true),
  ('ORDER_STATUS', 'order-status.shipped.label',     'SHIPPED',     3, true),
  ('ORDER_STATUS', 'order-status.in-progress.label', 'IN_PROGRESS', 4, true)
  ON CONFLICT DO NOTHING;

-- Shipment Statuses
INSERT INTO lk_lookup (type, description_key, option_value, order_number, active) VALUES
  ('SHIPMENT_STATUS', 'shipment-status.open.label',      'OPEN',      1, true),
  ('SHIPMENT_STATUS', 'shipment-status.completed.label', 'COMPLETED', 2, true)
  ON CONFLICT DO NOTHING;

-- Product Category
INSERT INTO lk_lookup (type, description_key, option_value, order_number, active) VALUES
  ('PRODUCT_CATEGORY', 'product-category.frozen.label',           'ORDER_PRODUCT_CATEGORY_FROZEN',           1, true),
  ('PRODUCT_CATEGORY', 'product-category.refrigerated.label',     'ORDER_PRODUCT_CATEGORY_REFRIGERATED',     2, true),
  ('PRODUCT_CATEGORY', 'product-category.room-temperature.label', 'ORDER_PRODUCT_CATEGORY_ROOM_TEMPERATURE', 3, true)
  ON CONFLICT DO NOTHING;

-- Blood Types
INSERT INTO lk_lookup (type, description_key, option_value, order_number, active) VALUES
  ('BLOOD_TYPE', 'blood-type.a-positive.label',  'AP',  1, true),
  ('BLOOD_TYPE', 'blood-type.a-negative.label',  'AN',  2, true),
  ('BLOOD_TYPE', 'blood-type.b-positive.label',  'BP',  3, true),
  ('BLOOD_TYPE', 'blood-type.b-negative.label',  'BN',  4, true),
  ('BLOOD_TYPE', 'blood-type.o-positive.label',  'OP',  5, true),
  ('BLOOD_TYPE', 'blood-type.o-negative.label',  'ON',  6, true),
  ('BLOOD_TYPE', 'blood-type.ab-positive.label', 'ABP', 7, true),
  ('BLOOD_TYPE', 'blood-type.ab-negative.label', 'ABN', 8, true)
  ON CONFLICT DO NOTHING;
