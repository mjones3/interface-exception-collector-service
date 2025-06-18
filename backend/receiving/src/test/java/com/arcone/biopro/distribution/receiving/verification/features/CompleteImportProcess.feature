@AOA-109
Feature: Complete Imports Process

    Background: Clean-up
        Given I have removed all imports using thermometer which code contains "-DST-413".
        And I have removed all created devices which ID contains "-DST-413".

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
            | ROOM_TEMPERATURE     |    (1*60)        |  (23.99*60)      |
        And I have an imported batch created with the following details:
            | Field                | Value                    |
            | temperatureCategory  | REFRIGERATED             |
            | locationCode         | 123456789                |
            | temperature          | 23.50                    |
            | thermometerCode      | THERM-DST-413            |
        And I have the products added in the batch with Unit Number as "<Unit Number>" , Product Code as "<Product Code>", Blood Type as "<Blood Type>", Expiration date as "<Expiration Date>", License status as "<License Status>" and Visual Inspection as "<Visual Inspection>".
        When I request to complete the last import batch created.
        Then I should receive a "SUCCESS" message response "Import completed successfully.".
        Then The status of the import batch should be "COMPLETED"
        Examples:
            | Device Location Code | Device ID     | Device Category | Device Type | Unit Number   | Product Code | Blood Type | Expiration Date      | License Status | Visual Inspection |
            | 123456789            | THERM-DST-413 | TEMPERATURE     | THERMOMETER | W036541186805 | E0181V00     | AP         | 2026-12-08T23:59:59Z |  LICENSED      |  SATISFACTORY     |



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
                | ROOM_TEMPERATURE     |    (1*60)        |  (23.99*60)      |
            And I have an imported batch created with the following details:
                | Field                | Value                    |
                | temperatureCategory  | REFRIGERATED             |
                | locationCode         | 123456789                |
                | temperature          | 23.55                    |
                | thermometerCode      | THERM-DST-413            |
            And I have the products added in the batch with Unit Number as "<Unit Number>" , Product Code as "<Product Code>", Blood Type as "<Blood Type>", Expiration date as "<Expiration Date>", License status as "<License Status>" and Visual Inspection as "<Visual Inspection>".
            And The status of the import batch is "<batch_status>"
            When I request to complete the last import batch created.
            Then I should receive a "WARN" message response "<Message>".
            And The status of the import batch should be "<batch_status>"
            Examples:
                | Device Location Code | Device ID     | Device Category  | Device Type | batch_status |Message                                            |
                | 123456789            | THERM-DST-413 | TEMPERATURE      | THERMOMETER | PENDING      |Import must have at least one product in the batch |
                | 123456789            | THERM-DST-413 | TEMPERATURE      | THERMOMETER | COMPLETED    |Import is already completed                        |

        Rule: I should receive a success message when the imports process is completed.
        @ui @DIS-413
        Scenario Outline: Successfully entering valid product information - UI
            Given I have a thermometer configured as location "<Device Location Code>", Device ID as "<Device ID>", Category as "<Device Category>" and Device Type as "<Device Type>".
            And The following temperature thresholds are configured:
                | Temperature Category | Min Temperature | Max Temperature |
                | REFRIGERATED         |    1            |  10             |
                | ROOM_TEMPERATURE     |    20           |  24             |
            And The following transit time thresholds are configured:
                | Temperature Category | Min Transit Time | Max Transit Time |
                | ROOM_TEMPERATURE     |    (1*60)        |  (23.99*60)      |
            And I have an imported batch created with the following details:
                | Field                | Value                    |
                | temperatureCategory  | REFRIGERATED             |
                | locationCode         | 123456789                |
                | temperature          | 23.15                    |
                | thermometerCode      | THERM-DST-413            |
            And I have the products added in the batch with Unit Number as "<Unit Number>" , Product Code as "<Product Code>", Blood Type as "<Blood Type>", Expiration date as "<Expiration Date>", License status as "<License Status>" and Visual Inspection as "<Visual Inspection>".
            And I am at the Enter Product Information Page.
            And The complete import process option should be "enabled"
            When I choose to complete the import process.
            And I should see a "SUCCESS" message: "Import completed successfully.".
            And I should be redirect to the Enter Shipping Information Page.
            Examples:
                | Device Location Code | Device ID     | Device Category | Device Type | Unit Number   | Product Code | Blood Type | Expiration Date      | License Status | Visual Inspection |
                | 123456789            | THERM-DST-413 | TEMPERATURE     | THERMOMETER | W036541186805 | E0181V00     | AP         | 2026-12-08T23:59:59Z |  LICENSED      |  SATISFACTORY     |
