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
            | REFRIGERATED         |    1            |  10             |
            | ROOM_TEMPERATURE     |    20           |  24             |
        And The following transit time thresholds are configured:
            | Temperature Category | Min Transit Time | Max Transit Time |
            | ROOM_TEMPERATURE     |    1             |  24              |
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
            | 123456789            | THERM-DST-412 | TEMPERATURE     | THERMOMETER | W036598786805 | E6170V00     | AP         | 2026-12-08T05:22:53.108Z | LICENSED       | SATISFACTORY      |

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
            | REFRIGERATED         |    1            |  10             |
            | ROOM_TEMPERATURE     |    20           |  24             |
        And The following transit time thresholds are configured:
            | Temperature Category | Min Transit Time | Max Transit Time |
            | ROOM_TEMPERATURE     |    1             |  24              |
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
            | 123456789            | THERM-DST-412 | TEMPERATURE     | THERMOMETER | W036598786805 | E6170V00     | AP         | 2026-12-33T05:22:53.108Z | LICENSED       | SATISFACTORY      | ValidationError | is not a valid 'DateTime'            |
            | 123456789            | THERM-DST-412 | TEMPERATURE     | THERMOMETER | W036598786805 | E8696V00     | AP         | 2026-12-08T05:22:53.108Z | LICENSED       | SATISFACTORY      | WARN            | Product type does not match |
            | 123456789            | THERM-DST-412 | TEMPERATURE     | THERMOMETER | W036598786805 | E617         | AP         | 2026-12-08T05:22:53.108Z | LICENSED       | SATISFACTORY      | WARN            | Product type does not match |

    Rule: I should be notified if I am importing an already imported product.
        @api @DIS-412
        Scenario Outline: Entering valid product information twice
            Given I have a thermometer configured as location "<Device Location Code>", Device ID as "<Device ID>", Category as "<Device Category>" and Device Type as "<Device Type>".
            And The following temperature thresholds are configured:
                | Temperature Category | Min Temperature | Max Temperature |
                | REFRIGERATED         |    1            |  10             |
                | ROOM_TEMPERATURE     |    20           |  24             |
            And The following transit time thresholds are configured:
                | Temperature Category | Min Transit Time | Max Transit Time |
                | ROOM_TEMPERATURE     |    1             |  24              |
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
                | 123456789            | THERM-DST-412 | TEMPERATURE     | THERMOMETER | W036598786805 | E6170V00     | AP         | 2026-12-08T05:22:53.108Z | LICENSED       | SATISFACTORY      |

        Rule: I should be able to identify when a product is flagged for quarantine.
        Rule: The system should apply quarantine for all the products in the batch if the temperature is out of the configured range.
        @api @DIS-412
        Scenario Outline: Entering quarantined product information - temperature is out of the configured range
            Given I have a thermometer configured as location "123456789", Device ID as "THERM-DST-412", Category as "TEMPERATURE" and Device Type as "THERMOMETER".
            And The following temperature thresholds are configured:
                | Temperature Category | Min Temperature | Max Temperature |
                | REFRIGERATED         |    1            |  10             |
                | ROOM_TEMPERATURE     |    20           |  24             |
            And The following transit time thresholds are configured:
                | Temperature Category | Min Transit Time | Max Transit Time |
                | ROOM_TEMPERATURE     | 1                | 24               |
            And I have an imported batch created with the following details:
                | Field                | Value                    |
                | temperatureCategory  | ROOM_TEMPERATURE             |
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
                | W036598786805 | E6170V00     | AP         | 2026-12-08T05:22:53.108Z | LICENSED       | SATISFACTORY      |

        Rule: I should be able to identify when a product is flagged for quarantine.
        Rule: The system should apply quarantine for all the products in the batch if the transit time is out of the configured range.
        @api @DIS-412
        Scenario Outline: Entering quarantined product information -  transit time is out of the configured range
            Given I have a thermometer configured as location "123456789", Device ID as "THERM-DST-412", Category as "TEMPERATURE" and Device Type as "THERMOMETER".
            And The following temperature thresholds are configured:
                | Temperature Category | Min Temperature | Max Temperature |
                | REFRIGERATED         |    1            |  10             |
                | ROOM_TEMPERATURE     |    20           |  24             |
            And The following transit time thresholds are configured:
                | Temperature Category | Min Transit Time | Max Transit Time |
                | ROOM_TEMPERATURE     |    1             |  24              |
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
                | W036598786805 | E6170V00     | AP         | 2026-12-08T05:22:53.108Z | LICENSED       | SATISFACTORY      |

        Rule: I should be able to identify when a product is flagged for quarantine.
        Rule: The system should apply quarantine for all the product that failed the visual inspection.
        @api @DIS-412
        Scenario Outline: Entering quarantined product information - failed the visual inspection
            Given I have a thermometer configured as location "123456789", Device ID as "THERM-DST-412", Category as "TEMPERATURE" and Device Type as "THERMOMETER".
            And The following temperature thresholds are configured:
                | Temperature Category | Min Temperature | Max Temperature |
                | REFRIGERATED         |    1            |  10             |
                | ROOM_TEMPERATURE     |    20           |  24             |
            And The following transit time thresholds are configured:
                | Temperature Category | Min Transit Time | Max Transit Time |
                | ROOM_TEMPERATURE     |    1             |  24              |
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
        @ui @DIS-412 @disabled
        Scenario Outline: Successfully entering valid product information - UI
            Given I have a thermometer configured as location "<Device Location Code>", Device ID as "<Device ID>", Category as "<Device Category>" and Device Type as "<Device Type>".
            And The following temperature thresholds are configured:
                | Temperature Category | Min Temperature | Max Temperature |
                | REFRIGERATED         |    1            |  10             |
                | ROOM_TEMPERATURE     |    20           |  24             |
            And The following transit time thresholds are configured:
                | Temperature Category | Min Transit Time | Max Transit Time |
                | ROOM_TEMPERATURE     |    1             |  24              |
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
            And I scan the product information with Unit Number as "<Unit Number>" , Product Code as "<Product Code>", Blood Type as "<Blood Type>" Expiration date as "<Expiration Date>".
            And I select License status as "<License Status>".
            And I select Visual Inspection as "<Visual Inspection>".
            When I choose to add a product
            Then I should see product unit number "<W036898786805>" and product code "<Product Code>" in the list of added products
            Examples:
                | Device Location Code | Device ID     | Device Category | Device Type | Unit Number      | Product Code | Blood Type | Expiration Date | License Status | Visual Inspection |
                | 123456789            | THERM-DST-412 | TEMPERATURE     | THERMOMETER | =W03689878680500 | =<E6170V00   | =%6200     | &>0260422359    | LICENSED       | SATISFACTORY      |


        Rule: I should be notified when I enter an invalid unit number.
        Rule: I should be notified when I enter a unit number with the FIN that is not associated with a registered facility used at the blood center.
        Rule: I should be notified when I enter an invalid ISBT product code.
        Rule: I should be notified when I enter a product code that is not configured in the blood center.
        Rule: I should be notified when I enter a product code that doesn’t match the temperature category.
        Rule: I should be notified when I enter an invalid blood type.
        Rule: I should be notified when I enter an invalid expiration date.
        @ui @DIS-412 @disabled
        Scenario Outline: Validation of invalid product information - UI
            Given I have a thermometer configured as location "<Device Location Code>", Device ID as "<Device ID>", Category as "<Device Category>" and Device Type as "<Device Type>".
            And The following temperature thresholds are configured:
                | Temperature Category | Min Temperature | Max Temperature |
                | REFRIGERATED         |    1            |  10             |
                | ROOM_TEMPERATURE     |    20           |  24             |
            And The following transit time thresholds are configured:
                | Temperature Category | Min Transit Time | Max Transit Time |
                | ROOM_TEMPERATURE     | 1                | 24               |
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
            And I scan the product information with Unit Number as "<Unit Number>" , Product Code as "<Product Code>", Blood Type as "<Blood Type>", Expiration date as "<Expiration Date>" and product category as "<Temperature Category>".
            Then I should not see product unit number "<W036898786805>" and product code "<Product Code>" in the list of added products
            And I should see a "WARN" message "<message>".
            And The add product option should be disabled.
            Examples:
                | Device Location Code | Device ID     | Device Type | Device Category | Temperature Category | Unit Number      | Product Code | Blood Type | Expiration Date | message                    |
                | 123456789            | THERM-DST-412 | THERMOMETER | TEMPERATURE     | ROOM_TEMPERATURE     | =W036880500      | =<E6170V00   | =%6200     | &>0260422359    | Invalid unit number format |
                | 123456789            | THERM-DST-412 | THERMOMETER | TEMPERATURE     | ROOM_TEMPERATURE     | =W03659878680500 | =<E6170V00   | =%6200     | &>0260422359    | Invalid blood type         |
                | 123456789            | THERM-DST-412 | THERMOMETER | TEMPERATURE     | ROOM_TEMPERATURE     | =W03689878680500 | =<E6170V00   | =%6200     | &>0260422359    | Invalid expiration date    |
                | 123456789            | THERM-DST-412 | THERMOMETER | TEMPERATURE     | ROOM_TEMPERATURE     | =W03689878680500 | =<E617       | =%6200     | &>0260422359    | Invalid ISBT product code  |
                | 123456789            | THERM-DST-412 | THERMOMETER | TEMPERATURE     | ROOM_TEMPERATURE     | =W03339878680500 | =<E617       | =%6200     | &>0260422359    | Invalid FIN number         |
                | 123456789            | THERM-DST-412 | THERMOMETER | TEMPERATURE     | REFRIGERATED         | =W03689878680500 | =<E617       | =%6200     | &>0260422359    | Invalid Product Type       |





