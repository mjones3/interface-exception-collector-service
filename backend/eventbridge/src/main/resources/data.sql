-- Events
TRUNCATE TABLE eventbridge.lk_event;

INSERT INTO eventbridge.lk_event (id,event_type,order_number, active)
VALUES (1,'ShipmentCompleted', 1, true);
