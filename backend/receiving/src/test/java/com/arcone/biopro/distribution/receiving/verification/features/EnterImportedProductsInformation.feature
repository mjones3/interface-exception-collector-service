@AOA-109
Feature: Enter Imported Products Information

    Rule: I should be required to enter unit number, ABO/Rh, product code, expiration date and license status for all the imported products.
    Rule: I should be notified when I enter an invalid unit number.
    Rule: I should be notified when I enter a unit number with the FIN that is not associated with a registered facility used at the blood center.
    Rule: I should be notified if I am importing an already imported product.
    Rule: I should be notified when I enter an invalid ISBT product code.
    Rule: I should be notified when I enter a product code that is not configured in the blood center.
    Rule: I should be notified when I enter a product code that doesn’t match the temperature category.
    Rule: I should be notified when I enter an invalid blood type.
    Rule: I should be notified when I enter an invalid expiration date.
    Rule: I should be able to identify when a product is flagged for quarantine.
    Rule: The system should apply quarantine for all the products in the batch if the transit time is out of the configured range.
    Rule: The system should apply quarantine for all the products in the batch if the temperature is out of the configured range.
    Rule: The system should apply quarantine for all the product that failed the visual inspection.
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
            | locationCode         | 123456789                |
            | temperature          | 26.80                    |
            | thermometerCode      | THERM-001                |
            | transitStartDateTime | 2025-06-08T05:22:53.108Z |
            | transitStartTimeZone | America/New_York         |
            | transitEndDateTime   | 2025-06-08T23:28:53.108Z |
            | transitEndTimeZone   | America/New_York         |
        When I request to enter the product information with Unit Number as "<Unit Number>" , Product Code as "<Product Code>", Blood Type as "<Blood Type>" Expiration date as "<Expiration Date>" , License status as "<License Status>" and Visual Inspection as "<Visual Inspection>"
        Then The product should be add into list of added products
        Examples:
        | Device Location Code | Device ID | Device Category  | Device Type | Unit Number    | Product Code | Blood Type | Expiration Date | License Status | Visual Inspection |
        | 123456789            | THERM-001 | THERMOMETER      | TEMPERATURE |  W036898786805 | E6170V00     | AP         | 2025-12-31      |  LICENSED      |  SATISFACTORY     |

    Rule: I should be required to enter unit number, ABO/Rh, product code, expiration date and license status for all the imported products.
    Rule: I should be notified when I enter an invalid unit number.
    Rule: I should be notified when I enter a unit number with the FIN that is not associated with a registered facility used at the blood center.
    #Rule: I should be notified if I am importing an already imported product.
    Rule: I should be notified when I enter an invalid ISBT product code.
    Rule: I should be notified when I enter a product code that is not configured in the blood center.
    Rule: I should be notified when I enter a product code that doesn’t match the temperature category.
    Rule: I should be notified when I enter an invalid blood type.
    Rule: I should be notified when I enter an invalid expiration date.
    Rule: I should be able to identify when a product is flagged for quarantine.
    Rule: The system should apply quarantine for all the products in the batch if the transit time is out of the configured range.
    Rule: The system should apply quarantine for all the products in the batch if the temperature is out of the configured range.
    Rule: The system should apply quarantine for all the product that failed the visual inspection.
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
            | locationCode         | 123456789                |
            | temperature          | 26.80                    |
            | thermometerCode      | THERM-001                |
            | transitStartDateTime | 2025-06-08T05:22:53.108Z |
            | transitStartTimeZone | America/New_York         |
            | transitEndDateTime   | 2025-06-08T23:28:53.108Z |
            | transitEndTimeZone   | America/New_York         |
        When I request to enter the product information with Unit Number as "<Unit Number>" , Product Code as "<Product Code>", Blood Type as "<Blood Type>" Expiration date as "<Expiration Date>" , License status as "<License Status>" and Visual Inspection as "<Visual Inspection>"
        Then The product should not be add into list of added products
        And I should receive a "<message_type>" message response "<message>".
        Examples:
            | Device Location Code | Device ID | Device Category  | Device Type | Unit Number    | Product Code | Blood Type | Expiration Date | License Status | Visual Inspection | message_type | message                    |
            | 123456789            | THERM-001 | THERMOMETER      | TEMPERATURE |  W036898786805 | E6170V00     | AP         | 2025-12-31      |  LICENSED      |  SATISFACTORY     | WARN         | Invalid unit number format |
            | 123456789            | THERM-001 | THERMOMETER      | TEMPERATURE |  W036898786805 | E6170V00     | XP         | 2025-12-31      |  LICENSED      |  SATISFACTORY     | WARN         | Invalid blood type         |
            | 123456789            | THERM-001 | THERMOMETER      | TEMPERATURE |  W036898786805 | E6170V00     | AP         | 2025-31-31      |  LICENSED      |  SATISFACTORY     | WARN         | Invalid expiration date    |
            | 123456789            | THERM-001 | THERMOMETER      | TEMPERATURE |  W036898786805 | E6170V00     | AP         | 2025-31-31      |  LICENSED      |  SATISFACTORY     | WARN         | Invalid product type       |
            | 123456789            | THERM-001 | THERMOMETER      | TEMPERATURE |  W036898786805 | E617         | AP         | 2025-31-31      |  LICENSED      |  SATISFACTORY     | WARN         | Invalid ISBT product code  |
