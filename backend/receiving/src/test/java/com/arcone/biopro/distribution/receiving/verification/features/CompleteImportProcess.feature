@AOA-109
Feature: Complete Imports Process

    Rule: I should be able to complete the import process once I have at least one product in the batch.
    Rule: I should receive a success message when the imports process is completed.
    @api @DIS-413
    Scenario Outline: Successfully complete import process with products
        Given I have a thermometer configured as location "<Device Location Code>", Device ID as "<Device ID>", Category as "<Device Category>" and Device Type as "<Device Type>".
        And The following temperature thresholds are configured:
            | Temperature Category | Min Temperature | Max Temperature |
            | REFRIGERATED         |    1            |  10             |
            | ROOM_TEMPERATURE     |    20           |  24             |
        And The following transit time thresholds are configured:
            | Temperature Category | Min Transit Time | Max Transit Time |
            | ROOM_TEMPERATURE     |    1             |  24              |
        And I have a "PENDING" imported batch created with the following details:
            | Field                | Value                    |
            | temperatureCategory  | ROOM_TEMPERATURE         |
            | locationCode         | 123456789                |
            | temperature          | 26.80                    |
            | thermometerCode      | THERM-001                |
            | transitStartDateTime | 2025-06-08T05:22:53.108Z |
            | transitStartTimeZone | America/New_York         |
            | transitEndDateTime   | 2025-06-08T23:28:53.108Z |
            | transitEndTimeZone   | America/New_York         |
        And I have the following products added in the batch:
            |Unit Number    | Product Code | Blood Type | Expiration Date | License Status | Visual Inspection |
            | W036898786805 | E6170V00     | AP         | 2025-12-31      |  LICENSED      |  SATISFACTORY     |
        When I request to complete the last import batch created.
        Then I should receive a "SUCCESS" message response "Import batch completed successfully".
        Then The status of the import batch should be "COMPLETED"
        Examples:
        | Device Location Code | Device ID | Device Category  | Device Type |
        | 123456789            | THERM-001 | THERMOMETER      | TEMPERATURE |

        Rule: I should not be able to complete the import process without products in the batch.
        @api @DIS-413
        Scenario Outline: Attempt to complete import process with empty batch
            Given I have a thermometer configured as location "<Device Location Code>", Device ID as "<Device ID>", Category as "<Device Category>" and Device Type as "<Device Type>".
            And The following temperature thresholds are configured:
                | Temperature Category | Min Temperature | Max Temperature |
                | REFRIGERATED         |    1            |  10             |
                | ROOM_TEMPERATURE     |    20           |  24             |
            And The following transit time thresholds are configured:
                | Temperature Category | Min Transit Time | Max Transit Time |
                | ROOM_TEMPERATURE     |    1             |  24              |
            And I have a "<batch_status>" imported batch created with the following details:
                | Field                | Value                    |
                | temperatureCategory  | ROOM_TEMPERATURE         |
                | locationCode         | 123456789                |
                | temperature          | 26.80                    |
                | thermometerCode      | THERM-001                |
                | transitStartDateTime | 2025-06-08T05:22:53.108Z |
                | transitStartTimeZone | America/New_York         |
                | transitEndDateTime   | 2025-06-08T23:28:53.108Z |
                | transitEndTimeZone   | America/New_York         |
            When I request to completed the last import batch created.
            Then I should receive a "WARN" message response "Import batch cannot be completed".
            Then The status of the import batch should be "<batch_status>"
            Examples:
                | Device Location Code | Device ID | Device Category  | Device Type |batch_status |
                | 123456789            | THERM-001 | THERMOMETER      | TEMPERATURE |PENDING      |
                | 123456789            | THERM-001 | THERMOMETER      | TEMPERATURE |COMPLETED    |
