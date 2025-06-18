
Feature: Cancel Imports Process

    Background: Clean-up
        Given I have removed all imports using thermometer which code contains "-DST-414".
        And I have removed all created devices which ID contains "-DST-414".

    Rule: I should be able to cancel the process at any point after entering the shipping information.
    Rule: The data entered must not be saved after canceling the process.
    @api @DIS-414
    Scenario Outline: Successfully cancel import process with products
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
            | temperature          | 23.00                    |
            | thermometerCode      | THERM-DST-414            |
        And I have the products added in the batch with Unit Number as "<Unit Number>" , Product Code as "<Product Code>", Blood Type as "<Blood Type>", Expiration date as "<Expiration Date>", License status as "<License Status>" and Visual Inspection as "<Visual Inspection>".
        When I request to cancel the last import batch created.
        Then I should receive a "SUCCESS" message response "Import canceled successfully.".
        And  The last import batch created should be removed from the system.
        And The products with unit number "<unit_numbers>" and product codes "<product_codes>" should not be imported.
        Examples:
            | Device Location Code | Device ID     | Device Category | Device Type | Unit Number   | Product Code | Blood Type | Expiration Date      | License Status | Visual Inspection |
            | 123456789            | THERM-DST-414 | TEMPERATURE     | THERMOMETER | W036541186805 | E0181V00     | AP         | 2026-12-08T23:59:59Z |  LICENSED      |  SATISFACTORY     |



        Rule: I should be able to cancel the process at any point after entering the shipping information.
        Rule: The data entered must not be saved after canceling the process.
        Rule: I should receive a confirmation message stating that all imports information will be removed.
        Rule: I should be able to abort the cancellation process and resume the imports process.
        Rule: I should be able to initiate a new imports process after cancelation is completed.
        @ui @DIS-414
        Scenario Outline: Successfully canceling import process with products - UI
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
                | temperature          | 23.00                    |
                | thermometerCode      | THERM-DST-414            |
            And I have the products added in the batch with Unit Number as "<Unit Number>" , Product Code as "<Product Code>", Blood Type as "<Blood Type>", Expiration date as "<Expiration Date>", License status as "<License Status>" and Visual Inspection as "<Visual Inspection>".
            And I am at the Enter Product Information Page.
            And I "should" see product unit number "<Unit Number>" and product code "<Product Code>" in the list of added products.
            When I choose to cancel the imports process.
            Then I should see a "Cancel Confirmation" message: "Cancelling will remove all import details. Are you sure you want to Continue?".
            When I choose to cancel the cancellation of the imports process.
            Then I "should" see product unit number "<Unit Number>" and product code "<Product Code>" in the list of added products.
            When I choose to cancel the imports process.
            Then I should see a "Cancel Confirmation" message: "Cancelling will remove all import details. Are you sure you want to Continue?".
            When I choose confirm cancellation of the imports process.
            And I should see a "SUCCESS" message: "Import canceled successfully.".
            And I should be redirect to the Enter Shipping Information Page.
            Examples:
                | Device Location Code | Device ID     | Device Category | Device Type | Unit Number   | Product Code | Blood Type | Expiration Date      | License Status | Visual Inspection |
                | 123456789            | THERM-DST-414 | TEMPERATURE     | THERMOMETER | W036541186805 | E0181V00     | AP         | 2026-12-08T23:59:59Z |  LICENSED      |  SATISFACTORY     |
