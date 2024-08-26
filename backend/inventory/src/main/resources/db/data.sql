truncate lk_text_config;

insert into lk_text_config (context, key_code, text)
values ('INVENTORY_VALIDATION', 'INVENTORY_NOT_FOUND_IN_LOCATION', 'This product is not in this location and cannot be shipped.');

insert into lk_text_config (context, key_code, text)
values ('INVENTORY_VALIDATION', 'INVENTORY_IS_EXPIRED', 'This product is expired and cannot be shipped.');

insert into lk_text_config (context, key_code, text)
values ('INVENTORY_VALIDATION', 'INVENTORY_IS_UNSUITABLE', '');

insert into lk_text_config (context, key_code, text)
values ('INVENTORY_VALIDATION', 'INVENTORY_IS_QUARANTINED', '');

insert into lk_text_config (context, key_code, text)
values ('INVENTORY_VALIDATION', 'INVENTORY_IS_DISCARDED', '');

insert into lk_text_config (context, key_code, text)
values ('INVENTORY_VALIDATION', 'INVENTORY_NOT_EXIST', 'This product not exist and cannot be shipped.');

insert into lk_text_config (context, key_code, text)
values ('INVENTORY_VALIDATION', 'INVENTORY_IS_SHIPPED', 'This product was shipped and cannot be shipped.');

insert into lk_text_config (context, key_code, text)
values ('DISCARD_REASON', 'POSITIVE_REACTIVE_TEST_RESULTS', 'This product has been discarded for test results. Place in biohazard container.');

insert into lk_text_config (context, key_code, text)
values ('DISCARD_REASON', 'ACTIVE_DEFERRAL', 'This product has an active deferral with a discard consequence and has been discarded. Place in biohazard container.');

insert into lk_text_config (context, key_code, text)
values ('DISCARD_REASON', 'MEDICATION_INDICATORS', 'This product has been discarded for medication indicators. Place in biohazard container.');

insert into lk_text_config (context, key_code, text)
values ('DISCARD_REASON', 'TIMING_RULES', 'This product has been discarded for timing rules. Place in biohazard container.');

insert into lk_text_config (context, key_code, text)
values ('DISCARD_REASON', 'ADDITIVE_SOLUTION_ISSUES', 'This product has already been discarded for Additive Solution Issues in the system. Place in biohazard container.');

insert into lk_text_config (context, key_code, text)
values ('DISCARD_REASON', 'DEFECTIVE_BAG', 'This product has already been discarded for Defective Bag in the system. Place in biohazard container.');

insert into lk_text_config (context, key_code, text)
values ('QUARANTINE_REASON', 'ABS_POSITIVE', 'This product is currently in quarantine for ''ABS POSITIVE'' and needs to be returned to storage.');
insert into lk_text_config (context, key_code, text)
values ('QUARANTINE_REASON', 'BCA_UNIT_NEEDED', 'This product is currently in quarantine for ''BCA UNIT NEEDED'' and needs to be returned to storage.');
insert into lk_text_config (context, key_code, text)
values ('QUARANTINE_REASON', 'CCP_ELIGIBLE', 'This product is currently in quarantine for ''CCP ELIGIBLE'' and needs to be returned to storage.');
insert into lk_text_config (context, key_code, text)
values ('QUARANTINE_REASON', 'FAILED_VISUAL_INSPECTION', 'This product is currently in quarantine for ''FAILED VISUAL INSPECTION'' and needs to be returned to storage.');
insert into lk_text_config (context, key_code, text)
values ('QUARANTINE_REASON', 'HOLD_UNTIL_EXPIRATION', 'This product is currently in quarantine for ''HOLD UNTIL EXPIRATION'' and needs to be returned to storage.');
insert into lk_text_config (context, key_code, text)
values ('QUARANTINE_REASON', 'IN_PROCESS_HOLD', 'This product is currently in quarantine for ''IN PROCESS HOLD'' and needs to be returned to storage.');
insert into lk_text_config (context, key_code, text)
values ('QUARANTINE_REASON', 'PENDING_FURTHER_REVIEW_INSPECTION', 'This product is currently in quarantine for ''PENDING FURTHER REVIEW INSPECTION'' and needs to be returned to storage.');
insert into lk_text_config (context, key_code, text)
values ('QUARANTINE_REASON', 'SAVE_PLASMA_FOR_CTS', 'This product is currently in quarantine for ''SAVE PLASMA FOR CTS'' and needs to be returned to storage.');
insert into lk_text_config (context, key_code, text)
values ('QUARANTINE_REASON', 'OTHER_SEE_COMMENTS', 'This product is currently in quarantine for ''OTHER SEE COMMENTS'' and needs to be returned to storage.');
insert into lk_text_config (context, key_code, text)
values ('QUARANTINE_REASON', 'UNDER_INVESTIGATION', 'This product is currently in quarantine for ''UNDER INVESTIGATION'' and needs to be returned to storage.');
