@AOA-109
Feature: Enter Imported Products Information

    Background: Clean-up
        Given I have removed all imports using thermometer which code contains "-DST-412".
        And I have removed all created devices which ID contains "-DST-412".

    Rule: I should be required to enter unit number, ABO/Rh, product code, expiration date and license status for all the imported products.
        @api @DIS-412
        Scenario Outline: Successfully entering valid product information
            Given I have a thermometer configured as location "<Device Location Code>", Device ID as "<Device ID>", Category as "<Device Category>" and Device Type as "<Device Type>".
            And The following temperature thresholds are configured:
                | Temperature Category | Min Temperature | Max Temperature |
                | REFRIGERATED         | 1               | 10              |
                | ROOM_TEMPERATURE     | 20              | 24              |
            And The following transit time thresholds are configured:
                | Temperature Category | Min Transit Time | Max Transit Time |
                | ROOM_TEMPERATURE     | (1*60)           | (24*60)          |
            And I have an imported batch created with the following details:
                | Field                | Value                    |
                | temperatureCategory  | ROOM_TEMPERATURE         |
                | transitStartDateTime | 2025-06-08T05:22:53.108Z |
                | transitStartTimeZone | America/New_York         |
                | transitEndDateTime   | 2025-06-08T23:28:53.108Z |
                | transitEndTimeZone   | America/New_York         |
                | temperature          | 23.80                    |
                | thermometerCode      | THERM-DST-412            |
                | locationCode         | 123456789                |
                | comments             | comments                 |
            When I request to enter the product information with Unit Number as "<Unit Number>" , Product Code as "<Product Code>", Blood Type as "<Blood Type>" Expiration date as "<Expiration Date>" , License status as "<License Status>" and Visual Inspection as "<Visual Inspection>".
            Then The product "<Unit Number>" "should" be added into list of added products.
            Examples:
                | Device Location Code | Device ID     | Device Category | Device Type | Unit Number   | Product Code | Blood Type | Expiration Date          | License Status | Visual Inspection |
                | 123456789            | THERM-DST-412 | TEMPERATURE     | THERMOMETER | W036598786805 | EA137V00     | AP         | 2026-12-08T05:22:53.108Z | LICENSED       | SATISFACTORY      |

    Rule: I should be notified when I enter an invalid unit number.
        Rule: I should be notified when I enter a unit number with the FIN that is not associated with a registered facility used at the blood center.
    Rule: I should be notified when I enter an invalid ISBT product code.
        Rule: I should be notified when I enter a product code that is not configured in the blood center.
    Rule: I should be notified when I enter a product code that doesn’t match the temperature category.
        Rule: I should be notified when I enter an invalid blood type.
    Rule: I should be notified when I enter an invalid expiration date.
        @api @DIS-412
        Scenario Outline: Validation of invalid product information
            Given I have a thermometer configured as location "<Device Location Code>", Device ID as "<Device ID>", Category as "<Device Category>" and Device Type as "<Device Type>".
            And The following temperature thresholds are configured:
                | Temperature Category | Min Temperature | Max Temperature |
                | REFRIGERATED         | 1               | 10              |
                | ROOM_TEMPERATURE     | 20              | 24              |
            And The following transit time thresholds are configured:
                | Temperature Category | Min Transit Time | Max Transit Time |
                | ROOM_TEMPERATURE     | (1*60)           | (24*60)          |
            And I have an imported batch created with the following details:
                | Field                | Value                    |
                | temperatureCategory  | ROOM_TEMPERATURE         |
                | transitStartDateTime | 2025-06-08T05:22:53.108Z |
                | transitStartTimeZone | America/New_York         |
                | transitEndDateTime   | 2025-06-08T23:28:53.108Z |
                | transitEndTimeZone   | America/New_York         |
                | temperature          | 23.80                    |
                | thermometerCode      | THERM-DST-412            |
                | locationCode         | 123456789                |
                | comments             | comments                 |
            When I request to enter the product information with Unit Number as "<Unit Number>" , Product Code as "<Product Code>", Blood Type as "<Blood Type>" Expiration date as "<Expiration Date>" , License status as "<License Status>" and Visual Inspection as "<Visual Inspection>".
            Then The product "<Unit Number>" "should not" be added into list of added products.
            And I should receive a "<message_type>" message response "<message>".
            Examples:
                | Device Location Code | Device ID     | Device Category | Device Type | Unit Number   | Product Code | Blood Type | Expiration Date          | License Status | Visual Inspection | message_type    | message                     |
                | 123456789            | THERM-DST-412 | TEMPERATURE     | THERMOMETER | W036598786805 | E6170V00     | XP         | 2026-12-08T05:22:53.108Z | LICENSED       | SATISFACTORY      | WARN            | ABO/RH is Invalid           |
                | 123456789            | THERM-DST-412 | TEMPERATURE     | THERMOMETER | W036598786805 | E6170V00     | AP         | 2026-12-33T05:22:53.108Z | LICENSED       | SATISFACTORY      | ValidationError | is not a valid 'DateTime'   |
                | 123456789            | THERM-DST-412 | TEMPERATURE     | THERMOMETER | W036598786805 | E4701V00     | AP         | 2026-12-08T05:22:53.108Z | LICENSED       | SATISFACTORY      | WARN            | Product type does not match |
                | 123456789            | THERM-DST-412 | TEMPERATURE     | THERMOMETER | W036598786805 | E617         | AP         | 2026-12-08T05:22:53.108Z | LICENSED       | SATISFACTORY      | WARN            | Product type does not match |

    Rule: I should be notified if I am importing an already imported product.
        @api @DIS-412
        Scenario Outline: Entering valid product information twice
            Given I have a thermometer configured as location "<Device Location Code>", Device ID as "<Device ID>", Category as "<Device Category>" and Device Type as "<Device Type>".
            And The following temperature thresholds are configured:
                | Temperature Category | Min Temperature | Max Temperature |
                | REFRIGERATED         | 1               | 10              |
                | ROOM_TEMPERATURE     | 20              | 24              |
            And The following transit time thresholds are configured:
                | Temperature Category | Min Transit Time | Max Transit Time |
                | ROOM_TEMPERATURE     | (1*60)           | (24*60)          |
            And I have an imported batch created with the following details:
                | Field                | Value                    |
                | temperatureCategory  | ROOM_TEMPERATURE         |
                | transitStartDateTime | 2025-06-08T05:22:53.108Z |
                | transitStartTimeZone | America/New_York         |
                | transitEndDateTime   | 2025-06-08T23:28:53.108Z |
                | transitEndTimeZone   | America/New_York         |
                | temperature          | 23.80                    |
                | thermometerCode      | THERM-DST-412            |
                | locationCode         | 123456789                |
                | comments             | comments                 |
            When I request to enter the product information with Unit Number as "<Unit Number>" , Product Code as "<Product Code>", Blood Type as "<Blood Type>" Expiration date as "<Expiration Date>" , License status as "<License Status>" and Visual Inspection as "<Visual Inspection>".
            Then The product "<Unit Number>" "should" be added into list of added products.
            When I request to enter the product information with Unit Number as "<Unit Number>" , Product Code as "<Product Code>", Blood Type as "<Blood Type>" Expiration date as "<Expiration Date>" , License status as "<License Status>" and Visual Inspection as "<Visual Inspection>".
            Then The product "<Unit Number>" "should not" be added into list of added products.
            And I should receive a "BAD_REQUEST" message response "The database returned ROLLBACK".
            Examples:
                | Device Location Code | Device ID     | Device Category | Device Type | Unit Number   | Product Code | Blood Type | Expiration Date          | License Status | Visual Inspection |
                | 123456789            | THERM-DST-412 | TEMPERATURE     | THERMOMETER | W036598786805 | EA138V00     | AP         | 2026-12-08T05:22:53.108Z | LICENSED       | SATISFACTORY      |

    Rule: I should be able to identify when a product is flagged for quarantine.
        Rule: The system should apply quarantine for all the products in the batch if the temperature is out of the configured range.
        @api @DIS-412
        Scenario Outline: Entering quarantined product information - temperature is out of the configured range
            Given I have a thermometer configured as location "123456789", Device ID as "THERM-DST-412", Category as "TEMPERATURE" and Device Type as "THERMOMETER".
            And The following temperature thresholds are configured:
                | Temperature Category | Min Temperature | Max Temperature |
                | REFRIGERATED         | 1               | 10              |
                | ROOM_TEMPERATURE     | 20              | 24              |
            And The following transit time thresholds are configured:
                | Temperature Category | Min Transit Time | Max Transit Time |
                | ROOM_TEMPERATURE     |    (1*60)        |  (24*60)         |
            And I have an imported batch created with the following details:
                | Field                | Value                    |
                | temperatureCategory  | ROOM_TEMPERATURE         |
                | transitStartDateTime | 2025-06-08T05:22:53.108Z |
                | transitStartTimeZone | America/New_York         |
                | transitEndDateTime   | 2025-06-08T23:28:53.108Z |
                | transitEndTimeZone   | America/New_York         |
                | temperature          | 28.80                    |
                | thermometerCode      | THERM-DST-412            |
                | locationCode         | 123456789                |
                | comments             | comments                 |
            When I request to enter the product information with Unit Number as "<Unit Number>" , Product Code as "<Product Code>", Blood Type as "<Blood Type>" Expiration date as "<Expiration Date>" , License status as "<License Status>" and Visual Inspection as "<Visual Inspection>".
            Then The product "<Unit Number>" "should" be added into list of added products.
            And The product "<Unit Number>" "should" be flagged for quarantine.
            Examples:
                | Unit Number   | Product Code | Blood Type | Expiration Date          | License Status | Visual Inspection |
                | W036598786805 | EB311V00     | AP         | 2026-12-08T05:22:53.108Z | LICENSED       | SATISFACTORY      |

    Rule: I should be able to identify when a product is flagged for quarantine.
        Rule: The system should apply quarantine for all the products in the batch if the transit time is out of the configured range.
        @api @DIS-412
        Scenario Outline: Entering quarantined product information -  transit time is out of the configured range
            Given I have a thermometer configured as location "123456789", Device ID as "THERM-DST-412", Category as "TEMPERATURE" and Device Type as "THERMOMETER".
            And The following temperature thresholds are configured:
                | Temperature Category | Min Temperature | Max Temperature |
                | REFRIGERATED         | 1               | 10              |
                | ROOM_TEMPERATURE     | 20              | 24              |
            And The following transit time thresholds are configured:
                | Temperature Category | Min Transit Time | Max Transit Time |
                | ROOM_TEMPERATURE     | (1*60)           | (24*60)          |
            And I have an imported batch created with the following details:
                | Field                | Value                    |
                | temperatureCategory  | ROOM_TEMPERATURE         |
                | locationCode         | 123456789                |
                | temperature          | 22.50                    |
                | thermometerCode      | THERM-DST-412            |
                | transitStartDateTime | 2025-05-08T05:22:53.108Z |
                | transitStartTimeZone | America/New_York         |
                | transitEndDateTime   | 2025-05-13T23:28:53.108Z |
                | transitEndTimeZone   | America/New_York         |
                | comments             | comments                 |
            When I request to enter the product information with Unit Number as "<Unit Number>" , Product Code as "<Product Code>", Blood Type as "<Blood Type>" Expiration date as "<Expiration Date>" , License status as "<License Status>" and Visual Inspection as "<Visual Inspection>".
            Then The product "<Unit Number>" "should" be added into list of added products.
            And The product "<Unit Number>" "should" be flagged for quarantine.
            Examples:
                | Unit Number   | Product Code | Blood Type | Expiration Date          | License Status | Visual Inspection |
                | W036598786805 | E2488V00     | AP         | 2026-12-08T05:22:53.108Z | LICENSED       | SATISFACTORY      |

    Rule: I should be able to identify when a product is flagged for quarantine.
        Rule: The system should apply quarantine for all the product that failed the visual inspection.
        @api @DIS-412
        Scenario Outline: Entering quarantined product information - failed the visual inspection
            Given I have a thermometer configured as location "123456789", Device ID as "THERM-DST-412", Category as "TEMPERATURE" and Device Type as "THERMOMETER".
            And The following temperature thresholds are configured:
                | Temperature Category | Min Temperature | Max Temperature |
                | REFRIGERATED         | 1               | 10              |
                | ROOM_TEMPERATURE     | 20              | 24              |
            And The following transit time thresholds are configured:
                | Temperature Category | Min Transit Time | Max Transit Time |
                | ROOM_TEMPERATURE     | (1*60)           | (24*60)          |
            And I have an imported batch created with the following details:
                | Field               | Value         |
                | temperatureCategory | REFRIGERATED  |
                | locationCode        | 123456789     |
                | temperature         | 23.00         |
                | thermometerCode     | THERM-DST-412 |
            When I request to enter the product information with Unit Number as "<Unit Number>" , Product Code as "<Product Code>", Blood Type as "<Blood Type>" Expiration date as "<Expiration Date>" , License status as "<License Status>" and Visual Inspection as "<Visual Inspection>".
            Then The product "<Unit Number>" "should" be added into list of added products.
            And The product "<Unit Number>" "should" be flagged for quarantine.
            Examples:
                | Unit Number   | Product Code | Blood Type | Expiration Date          | License Status | Visual Inspection |
                | W036598786805 | E4140V00     | AP         | 2026-12-08T05:22:53.108Z | LICENSED       | UNSATISFACTORY    |


    Rule: I should be required to enter unit number, ABO/Rh, product code, expiration date and license status for all the imported products.
        @ui @DIS-412
        Scenario Outline: Successfully entering valid product information - UI
            Given I have a thermometer configured as location "<Device Location Code>", Device ID as "<Device ID>", Category as "<Device Category>" and Device Type as "<Device Type>".
            And The following temperature thresholds are configured:
                | Temperature Category | Min Temperature | Max Temperature |
                | REFRIGERATED         | 1               | 10              |
                | ROOM_TEMPERATURE     | 20              | 24              |
            And The following transit time thresholds are configured:
                | Temperature Category | Min Transit Time | Max Transit Time |
                | ROOM_TEMPERATURE     | (1*60)           | (24*60)          |
            And I have an imported batch created with the following details:
                | Field                | Value                    |
                | temperatureCategory  | ROOM_TEMPERATURE         |
                | locationCode         | 123456789                |
                | temperature          | 20.50                    |
                | thermometerCode      | THERM-DST-412            |
                | transitStartDateTime | 2025-06-08T05:22:53.108Z |
                | transitStartTimeZone | America/New_York         |
                | transitEndDateTime   | 2025-06-08T15:25:53.108Z |
                | transitEndTimeZone   | America/New_York         |
            And I am at the Enter Product Information Page.
            And I scan the product information with Unit Number as "<Unit Number>", Product Code as "<Product Code>", Blood Type as "<Blood Type>", and Expiration date as "<Expiration Date>".
            And I select License status as "<License Status>".
            And I select Visual Inspection as "<Visual Inspection>".
            When I choose to add a product.
            Then I "should" see product unit number "<Unit Number>" and product code "<Product Code>" in the list of added products.
            Examples:
                | Device Location Code | Device ID     | Device Category | Device Type | Unit Number      | Product Code | Blood Type | Expiration Date | License Status | Visual Inspection |
                | 123456789            | THERM-DST-412 | TEMPERATURE     | THERMOMETER | =W03659878680500 | =<E6170V00   | =%6200     | &>0260422359    | LICENSED       | SATISFACTORY      |
                | 123456789            | THERM-DST-412 | TEMPERATURE     | THERMOMETER | =W03659878680500 | =<E6170V00   | =%6200     | &>0251712359    | LICENSED       | SATISFACTORY      |


    Rule: I should be notified when I enter an invalid unit number.
        Rule: I should be notified when I enter a unit number with the FIN that is not associated with a registered facility used at the blood center.
    Rule: I should be notified when I enter an invalid ISBT product code.
        Rule: I should be notified when I enter a product code that is not configured in the blood center.
    Rule: I should be notified when I enter a product code that doesn’t match the temperature category.
        Rule: I should be notified when I enter an invalid blood type.
    Rule: I should be notified when I enter an invalid expiration date.
        @ui @DIS-412
        Scenario Outline: Validation of invalid product information - UI
            Given I have a thermometer configured as location "<Device Location Code>", Device ID as "<Device ID>", Category as "<Device Category>" and Device Type as "<Device Type>".
            And The following temperature thresholds are configured:
                | Temperature Category | Min Temperature | Max Temperature |
                | REFRIGERATED         | 1               | 10              |
                | ROOM_TEMPERATURE     | 20              | 24              |
            And The following transit time thresholds are configured:
                | Temperature Category | Min Transit Time | Max Transit Time |
                | ROOM_TEMPERATURE     |    (1*60)        |  (24*60)         |
            And I have an imported batch created with the following details:
                | Field                | Value                    |
                | temperatureCategory  | ROOM_TEMPERATURE         |
                | transitStartDateTime | 2025-06-08T05:22:53.108Z |
                | transitStartTimeZone | America/New_York         |
                | transitEndDateTime   | 2025-06-08T15:25:53.108Z |
                | transitEndTimeZone   | America/New_York         |
                | temperature          | 20.50                    |
                | thermometerCode      | THERM-DST-412            |
                | locationCode         | 123456789                |
                | comments             | comments                 |
            And I am at the Enter Product Information Page.
            And I scan the product information with Unit Number as "<Unit Number>", Product Code as "<Product Code>", Blood Type as "<Blood Type>", and Expiration date as "<Expiration Date>".
            Then I "should not" see product unit number "<Unit Number>" and product code "<Product Code>" in the list of added products.
            And I should see a "WARNING" message: "<message>".
            And The add product option should be "disabled".
            Examples:
                | Device Location Code | Device ID     | Device Type | Device Category | Unit Number      | Product Code | Blood Type | Expiration Date | message                                          |
                | 123456789            | THERM-DST-412 | THERMOMETER | TEMPERATURE     | =W036880500      | =<E6170V00   | =%6200     | &>0260422359    | Invalid Unit Number                              |
                | 123456789            | THERM-DST-412 | THERMOMETER | TEMPERATURE     | =W03659878680500 | =<E6170V00   | =%0200     | &>0260422359    | Invalid ABO/RH                                   |
                | 123456789            | THERM-DST-412 | THERMOMETER | TEMPERATURE     | =W03659878680500 | =<E6170V00   | =%6200     | &>0200002359    | Invalid expiration date                          |
                | 123456789            | THERM-DST-412 | THERMOMETER | TEMPERATURE     | =W03659878680500 | =<E617       | =%6200     | &>0260422359    | Invalid Product Code                             |
                | 123456789            | THERM-DST-412 | THERMOMETER | TEMPERATURE     | =W03399878680500 | =<E6170V00   | =%6200     | &>0260422359    | FIN is not associated with a registered facility |
                | 123456789            | THERM-DST-412 | THERMOMETER | TEMPERATURE     | =W03659878680500 | =<E0023V00   | =%6200     | &>0260422359    | Product type does not match                      |


        @api @DIS-412
        Scenario Outline: Validating label barcode product information
            Given I want to validate the product details printed in the label.
            When I request to validate the scanned product information for the Temperature Category as "<Temperature Category>" barcode type as "<Barcode Type>" and the scanned value as "<Scan Value>".
            Then I should receive the barcode validation result as "<IsValid>" and the result value should be "<Result>" and result description as "<Result Description>".
            And Then system "<Should_shouldNot>" return the result message as "<message>".
            Examples:
                | Temperature Category | Barcode Type            | Scan Value       | IsValid | Result               | Result Description | Should_shouldNot | message                                          |
                | REFRIGERATED         | BARCODE_BLOOD_GROUP     | =%9500           | true    | ON                   | O Negative         | should not       |                                                  |
                | REFRIGERATED         | BARCODE_BLOOD_GROUP     | =%5100           | true    | OP                   | O Positive         | should not       |                                                  |
                | REFRIGERATED         | BARCODE_BLOOD_GROUP     | =%0600           | true    | AN                   | A Negative         | should not       |                                                  |
                | REFRIGERATED         | BARCODE_BLOOD_GROUP     | =%6200           | true    | AP                   | A Positive        | should not       |                                                  |
                | REFRIGERATED         | BARCODE_BLOOD_GROUP     | =%1700           | true    | BN                   | B Negative         | should not       |                                                  |
                | REFRIGERATED         | BARCODE_BLOOD_GROUP     | =%7300           | true    | BP                   | B Positive         | should not       |                                                  |
                | REFRIGERATED         | BARCODE_BLOOD_GROUP     | =%2800           | true    | ABN                  | AB Negative        | should not       |                                                  |
                | REFRIGERATED         | BARCODE_BLOOD_GROUP     | =%8400           | true    | ABP                  | AB Positive        | should not       |                                                  |
                | REFRIGERATED         | BARCODE_EXPIRATION_DATE | &>0260422359     | true    | 2026-02-11T23:59:59Z | Feb 11, 2026       | should not       |                                                  |
                | REFRIGERATED         | BARCODE_EXPIRATION_DATE | &>0320422362     | false   |                      |                    | should           | Invalid Expiration Date                          |
                | REFRIGERATED         | BARCODE_UNIT_NUMBER     | =W03659878680500 | true    | W036598786805        | W036598786805      | should not       |                                                  |
                | REFRIGERATED         | BARCODE_UNIT_NUMBER     | =W0365987        | false   | W036598786805        | W036598786805      | should           | Invalid Unit Number                              |
                | REFRIGERATED         | BARCODE_UNIT_NUMBER     | =W03259878680500 | false   | W032598786805        | W032598786805      | should           | FIN is not associated with a registered facility |
                | REFRIGERATED         | BARCODE_PRODUCT_CODE    | =<E00V00         | false   |                      |                    | should           | Invalid Product Code                             |
                | REFRIGERATED         | BARCODE_PRODUCT_CODE    | =<E8340V00       | false   |                      |                    | should           | Product type does not match                      |
                | REFRIGERATED         | BARCODE_PRODUCT_CODE    | =<E0023V00       | true    | E0023V00             | E0023V00           | should not       |                                                  |
                | REFRIGERATED         | BARCODE_PRODUCT_CODE    | =<E0033V00       | true    | E0033V00             | E0033V00           | should not       |                                                  |
                | REFRIGERATED         | BARCODE_PRODUCT_CODE    | =<E0112V00       | true    | E0112V00             | E0112V00           | should not       |                                                  |
                | REFRIGERATED         | BARCODE_PRODUCT_CODE    | =<E0167V00       | true    | E0167V00             | E0167V00           | should not       |                                                  |
                | REFRIGERATED         | BARCODE_PRODUCT_CODE    | =<E0181V00       | true    | E0181V00             | E0181V00           | should not       |                                                  |
                | REFRIGERATED         | BARCODE_PRODUCT_CODE    | =<E0262V00       | true    | E0262V00             | E0262V00           | should not       |                                                  |
                | REFRIGERATED         | BARCODE_PRODUCT_CODE    | =<E0316V00       | true    | E0316V00             | E0316V00           | should not       |                                                  |
                | REFRIGERATED         | BARCODE_PRODUCT_CODE    | =<E0336V00       | true    | E0336V00             | E0336V00           | should not       |                                                  |
                | REFRIGERATED         | BARCODE_PRODUCT_CODE    | =<E0366V00       | true    | E0366V00             | E0366V00           | should not       |                                                  |
                | REFRIGERATED         | BARCODE_PRODUCT_CODE    | =<E0382V00       | true    | E0382V00             | E0382V00           | should not       |                                                  |
                | REFRIGERATED         | BARCODE_PRODUCT_CODE    | =<E0404V00       | true    | E0404V00             | E0404V00           | should not       |                                                  |
                | REFRIGERATED         | BARCODE_PRODUCT_CODE    | =<E0424V00       | true    | E0424V00             | E0424V00           | should not       |                                                  |
                | REFRIGERATED         | BARCODE_PRODUCT_CODE    | =<E0678V00       | true    | E0678V00             | E0678V00           | should not       |                                                  |
                | REFRIGERATED         | BARCODE_PRODUCT_CODE    | =<E0685V00       | true    | E0685V00             | E0685V00           | should not       |                                                  |
                | REFRIGERATED         | BARCODE_PRODUCT_CODE    | =<E0686V00       | true    | E0686V00             | E0686V00           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E0701V00       | true    | E0701V00             | E0701V00           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E0701V00       | true    | E0701V00             | E0701V00           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E0713V00       | true    | E0713V00             | E0713V00           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E0869V00       | true    | E0869V00             | E0869V00           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E0869VA0       | true    | E0869VA0             | E0869VA0           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E0869VB0       | true    | E0869VB0             | E0869VB0           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E0869VC0       | true    | E0869VC0             | E0869VC0           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E0869VD0       | true    | E0869VD0             | E0869VD0           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E1624V00       | true    | E1624V00             | E1624V00           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E1624VA0       | true    | E1624VA0             | E1624VA0           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E1624VB0       | true    | E1624VB0             | E1624VB0           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E1624VC0       | true    | E1624VC0             | E1624VC0           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E1624VD0       | true    | E1624VD0             | E1624VD0           | should not       |                                                  |
                | REFRIGERATED         | BARCODE_PRODUCT_CODE    | =<E2457V00       | true    | E2457V00             | E2457V00           | should not       |                                                  |
                | REFRIGERATED         | BARCODE_PRODUCT_CODE    | =<E2469V00       | true    | E2469V00             | E2469V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<E2488V00       | true    | E2488V00             | E2488V00           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E2534V00       | true    | E2534V00             | E2534V00           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E2555V00       | true    | E2555V00             | E2555V00           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E2603V00       | true    | E2603V00             | E2603V00           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E2619V00       | true    | E2619V00             | E2619V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<E3556V00       | true    | E3556V00             | E3556V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<E3558V00       | true    | E3558V00             | E3558V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<E3559V00       | true    | E3559V00             | E3559V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<E3560V00       | true    | E3560V00             | E3560V00           | should not       |                                                  |
                | REFRIGERATED         | BARCODE_PRODUCT_CODE    | =<E4140V00       | true    | E4140V00             | E4140V00           | should not       |                                                  |
                | REFRIGERATED         | BARCODE_PRODUCT_CODE    | =<E4531V00       | true    | E4531V00             | E4531V00           | should not       |                                                  |
                | REFRIGERATED         | BARCODE_PRODUCT_CODE    | =<E4532V00       | true    | E4532V00             | E4532V00           | should not       |                                                  |
                | REFRIGERATED         | BARCODE_PRODUCT_CODE    | =<E4533V00       | true    | E4533V00             | E4533V00           | should not       |                                                  |
                | REFRIGERATED         | BARCODE_PRODUCT_CODE    | =<E4566V00       | true    | E4566V00             | E4566V00           | should not       |                                                  |
                | REFRIGERATED         | BARCODE_PRODUCT_CODE    | =<E4567V00       | true    | E4567V00             | E4567V00           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E4689V00       | true    | E4689V00             | E4689V00           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E4693V00       | true    | E4693V00             | E4693V00           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E4697V00       | true    | E4697V00             | E4697V00           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E4701V00       | true    | E4701V00             | E4701V00           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E5085V00       | true    | E5085V00             | E5085V00           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E5105V00       | true    | E5105V00             | E5105V00           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E5106V00       | true    | E5106V00             | E5106V00           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E5107V00       | true    | E5107V00             | E5107V00           | should not       |                                                  |
                | REFRIGERATED         | BARCODE_PRODUCT_CODE    | =<E5160V00       | true    | E5160V00             | E5160V00           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E5622V00       | true    | E5622V00             | E5622V00           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E5880V00       | true    | E5880V00             | E5880V00           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E6022V00       | true    | E6022V00             | E6022V00           | should not       |                                                  |
                | REFRIGERATED         | BARCODE_PRODUCT_CODE    | =<E6170V00       | true    | E6170V00             | E6170V00           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E7607V00       | true    | E7607V00             | E7607V00           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E7637V00       | true    | E7637V00             | E7637V00           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E7639V00       | true    | E7639V00             | E7639V00           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E7641V00       | true    | E7641V00             | E7641V00           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E7643V00       | true    | E7643V00             | E7643V00           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E7644V00       | true    | E7644V00             | E7644V00           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E7644VA0       | true    | E7644VA0             | E7644VA0           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E7644VB0       | true    | E7644VB0             | E7644VB0           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E7644VC0       | true    | E7644VC0             | E7644VC0           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E7644VD0       | true    | E7644VD0             | E7644VD0           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E7646V00       | true    | E7646V00             | E7646V00           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E7648V00       | true    | E7648V00             | E7648V00           | should not       |                                                  |
                | FROZEN               | BARCODE_PRODUCT_CODE    | =<E7650V00       | true    | E7650V00             | E7650V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<E8340V00       | true    | E8340V00             | E8340V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<E8341V00       | true    | E8341V00             | E8341V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<E8342V00       | true    | E8342V00             | E8342V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<E8343V00       | true    | E8343V00             | E8343V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<E8344V00       | true    | E8344V00             | E8344V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<E8695V00       | true    | E8695V00             | E8695V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<E8696V00       | true    | E8696V00             | E8696V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<E8697V00       | true    | E8697V00             | E8697V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<E8698V00       | true    | E8698V00             | E8698V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<E9138V00       | true    | E9138V00             | E9138V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<E9139V00       | true    | E9139V00             | E9139V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<E9140V00       | true    | E9140V00             | E9140V00           | should not       |                                                  |
                | REFRIGERATED         | BARCODE_PRODUCT_CODE    | =<E9431V00       | true    | E9431V00             | E9431V00           | should not       |                                                  |
                | REFRIGERATED         | BARCODE_PRODUCT_CODE    | =<E9432V00       | true    | E9432V00             | E9432V00           | should not       |                                                  |
                | REFRIGERATED         | BARCODE_PRODUCT_CODE    | =<E9433V00       | true    | E9433V00             | E9433V00           | should not       |                                                  |
                | REFRIGERATED         | BARCODE_PRODUCT_CODE    | =<E9434V00       | true    | E9434V00             | E9434V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<EA007V00       | true    | EA007V00             | EA007V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<EA008V00       | true    | EA008V00             | EA008V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<EA009V00       | true    | EA009V00             | EA009V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<EA010V00       | true    | EA010V00             | EA010V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<EA011V00       | true    | EA011V00             | EA011V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<EA012V00       | true    | EA012V00             | EA012V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<EA013V00       | true    | EA013V00             | EA013V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<EA014V00       | true    | EA014V00             | EA014V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<EA136V00       | true    | EA136V00             | EA136V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<EA137V00       | true    | EA137V00             | EA137V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<EA138V00       | true    | EA138V00             | EA138V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<EA139V00       | true    | EA139V00             | EA139V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<EA140V00       | true    | EA140V00             | EA140V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<EA141V00       | true    | EA141V00             | EA141V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<EA142V00       | true    | EA142V00             | EA142V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<EA143V00       | true    | EA143V00             | EA143V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<EB311V00       | true    | EB311V00             | EB311V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<EB314V00       | true    | EB314V00             | EB314V00           | should not       |                                                  |
                | REFRIGERATED         | BARCODE_PRODUCT_CODE    | =<EB317V00       | true    | EB317V00             | EB317V00           | should not       |                                                  |
                | ROOM_TEMPERATURE     | BARCODE_PRODUCT_CODE    | =<EB318V00       | true    | EB318V00             | EB318V00           | should not       |                                                  |






