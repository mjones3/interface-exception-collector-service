INSERT INTO irradiation.lk_configuration (key, value, active, create_date, modification_date)
VALUES ('IRRADIATION_EXPIRATION_DAYS', '28', true, now(), now())
ON CONFLICT (key) DO NOTHING;