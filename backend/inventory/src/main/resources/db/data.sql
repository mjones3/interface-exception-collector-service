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
values ('UNSUITABLE_REASON', 'POSITIVE_REACTIVE_TEST_RESULTS', 'This product has been discarded for test results. Place in biohazard container.');
insert into lk_text_config (context, key_code, text)
values ('UNSUITABLE_REASON', 'ACTIVE_DEFERRAL', 'This product has an active deferral with a discard consequence and has been discarded. Place in biohazard container.');
insert into lk_text_config (context, key_code, text)
values ('UNSUITABLE_REASON', 'MISSING_TEST_RESULTS', 'This product is missing test results and has been discarded. Place in biohazard container.');
insert into lk_text_config (context, key_code, text)
values ('UNSUITABLE_REASON', 'MEDICATION_INDICATORS', 'This product has been discarded for medication indicators. Place in biohazard container.');
insert into lk_text_config (context, key_code, text)
values ('UNSUITABLE_REASON', 'TIMING_RULES', 'This product has been discarded for timing rules. Place in biohazard container.');


insert into lk_text_config (context, key_code, text)
values ('DISCARD_REASON', 'ADDITIVE_SOLUTION_ISSUES', 'This product has already been discarded for Additive Solution Issues in the system. Place in biohazard container.');
insert into lk_text_config (context, key_code, text)
values ('DISCARD_REASON', 'DEFECTIVE_BAG', 'This product has already been discarded for Defective Bag in the system. Place in biohazard container.');
insert into lk_text_config (context, key_code, text)
values ('DISCARD_REASON', 'AGGREGATES', 'This product has already been discarded for Aggregates in the system. Place in biohazard container.');
insert into lk_text_config (context, key_code, text)
values ('DISCARD_REASON', 'BROKEN', 'This product has already been discarded for Broken in the system. Place in biohazard container.');
insert into lk_text_config (context, key_code, text)
values ('DISCARD_REASON', 'DESTROYED_BY_CONSIGNEE', 'This product has already been discarded for Destroyed By Consignee in the system. Place in biohazard container.');
insert into lk_text_config (context, key_code, text)
values ('DISCARD_REASON', 'DONOR_SERVICES_PRODCEDURAL_FAILURE', 'This product has already been discarded for Donor Services Procedural Failure in the system. Place in biohazard container.');
insert into lk_text_config (context, key_code, text)
values ('DISCARD_REASON', 'EXCEEDED_COLLECTION_TIME', 'This product has already been discarded for Exceeded Collection Time in the system. Place in biohazard container.');
insert into lk_text_config (context, key_code, text)
values ('DISCARD_REASON', 'EXPIRED', 'This product has already been discarded for Expired in the system. Place in biohazard container.');
insert into lk_text_config (context, key_code, text)
values ('DISCARD_REASON', 'INSUFFICIENT_VOLUME', 'This product has already been discarded for Insufficient Volume in the system. Place in biohazard container.');
insert into lk_text_config (context, key_code, text)
values ('DISCARD_REASON', 'OUT_OF_TEMPERATURE', 'This product has already been discarded for Ouf Of Temperature in the system. Place in biohazard container.');
insert into lk_text_config (context, key_code, text)
values ('DISCARD_REASON', 'POST_DONATION_INFORMATION', 'This product has already been discarded for Post Donation Information in the system. Place in biohazard container.');
insert into lk_text_config (context, key_code, text)
values ('DISCARD_REASON', 'PROCESSING_ERROR', 'This product has already been discarded for Processing Error in the system. Place in biohazard container.');
insert into lk_text_config (context, key_code, text)
values ('DISCARD_REASON', 'PRODUCT_NOT_FOUND', 'This product has already been discarded for Product Not Found in the system. Place in biohazard container.');
insert into lk_text_config (context, key_code, text)
values ('DISCARD_REASON', 'VISUAL_INSPECTION_FAILURE', 'This product has already been discarded for Visual Inspection Failure in the system. Place in biohazard container.');
insert into lk_text_config (context, key_code, text)
values ('DISCARD_REASON', 'OUT_OF_TIME', 'This product has already been discarded for Out Of Time in the system. Place in biohazard container.');
insert into lk_text_config (context, key_code, text)
values ('DISCARD_REASON', 'THERAPEUTIC_DONOR', 'This product has already been discarded for Therapeutic Donor in the system. Place in biohazard container.');
insert into lk_text_config (context, key_code, text)
values ('DISCARD_REASON', 'UNDER_18_DONOR', 'This product has already been discarded for Under 18 Donor in the system. Place in biohazard container.');
insert into lk_text_config (context, key_code, text)
values ('DISCARD_REASON', 'STERILE_CONNECTION_FAILURE', 'This product has already been discarded for Sterile Connection Failure in the system. Place in biohazard container.');
insert into lk_text_config (context, key_code, text)
values ('DISCARD_REASON', 'UNSUCCESSFUL_COLLECTION', 'This product has already been discarded for Unsuccessful Collection in the system. Place in biohazard container.');

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
