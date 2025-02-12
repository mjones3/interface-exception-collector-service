-- Turn off visual inspection configuration

UPDATE lk_lookup SET  option_value = 'false' WHERE type = 'SHIPPING_VISUAL_INSPECTION_ACTIVE';
