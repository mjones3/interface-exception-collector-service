DELETE FROM recoveredplasmashipping.lk_label_template where template_type = 'RPS_CARTON_LABEL';
DELETE FROM recoveredplasmashipping.lk_system_process_property WHERE system_process_type = 'RPS_CARTON_LABEL';

INSERT INTO recoveredplasmashipping.lk_system_process_property (system_process_type,property_key,property_value)
VALUES ('RPS_CARTON_LABEL','USE_TRANSPORTATION_NUMBER','Y'),
       ('RPS_CARTON_LABEL','BLOOD_CENTER_NAME','ARC-One Solutions'),
       ('RPS_CARTON_LABEL','USE_TOTAL_CARTONS','N');

INSERT INTO recoveredplasmashipping.lk_label_template (id, template_type, template, order_number, create_date, modification_date, active) VALUES (1, 'RPS_CARTON_LABEL', e'
^XA
^PQ1
^CI28

^FX Horizontal Label Divider
^FO20,598^GB1160,4,4^FS
^FX Vertical Label Divider
^FO598,20^GB4,580,4^FS

^FX UPPER LEFT QUADRANT

^FO20,40^A0N,30,30^FDTo:^FS
^FO80,40
^A0N,30,30
^FB580,5,,L,0
^FD${CUSTOMER_CODE}\\&${CUSTOMER_NAME}\\&${CUSTOMER_ADDRESS}\\&${CUSTOMER_CITY}, ${CUSTOMER_STATE}, ${CUSTOMER_ZIP_CODE}\\&${CUSTOMER_COUNTRY}^FS
^FO60,300^A0N,40,40^FD${CARTON_NUMBER}^FS
^FO40,360
^BY3,2.0,100
^BCN,140,N,N,N
^FD${CARTON_NUMBER}^FS
^FO60,530^A0N,30,30^FDDate Prepared    ${CLOSE_DATE}^FS

^FX UPPER RIGHT QUADRANT

^FO620,40^A0N,30,30^FDFrom:^FS
^FO620,120^A0N,44,44
^FB560,360,20,C,0
^FD${BLOOD_CENTER_NAME}\\&${ADDRESS_LINE}\\&${CITY}, ${STATE},${ZIPCODE}\\& ${COUNTRY}\\&^FS
<#if DISPLAY_TRANSPORTATION_NUMBER??>
^FO620,530^A0N,30,30^FDTransportation Reference Number: ${TRANSPORTATION_NUMBER}^FS
</#if>
^FX LOWER QUADRANT

^FO360,620
^BY4,2.0,100
^BCN,140,N,N,N
^FD${PRODUCT_CODE}^FS
^FO500,780^A0N,56,48^FD${PRODUCT_CODE}^FS
^FO240,860^A0N,40,40^FD${CARTON_SEQUENCE}^FS
^FO240,920
^BY4,2.0,100
^BCN,140,N,N,N
^FD${SHIPMENT_NUMBER}^FS
^FO240,1080^A0N,56,48^FDShipment Number: ${SHIPMENT_NUMBER}^FS

^XZ
', 1, now(), now(), true);






