-- Turn off back order configuration

UPDATE lk_lookup SET  option_value = 'false' WHERE type = 'BACK_ORDER_CREATION';
