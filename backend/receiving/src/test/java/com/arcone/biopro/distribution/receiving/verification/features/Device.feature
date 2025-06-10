Feature: Device

    Background: Clean-up
        Given I have removed all created devices which ID contains "-DST-".


        Rule: I should be able to enter the thermometer ID configured for location where products are imported.
        @api @DIS-409
        Scenario Outline: Validate valid thermometer
            Given I have a thermometer configured as location "<Device Location Code>", Device ID as "<Device ID>", Category as "<Device Category>" and Device Type as "<Device Type>".
            When I enter thermometer ID "<Device ID>" for the location code "<Imports Location Code>" and Temperature Category "<Temperature Category>".
            Then I should receive the device details containing Device ID as "<Device ID>", Category as "<Device Category>" and Device Type as "<Device Type>".
            Examples:
                | Imports Location Code | Device Location Code | Device ID     | Device Type | Device Category | Temperature Category |
                | 123456789             | 123456789            | THERM-DST-001 | THERMOMETER | TEMPERATURE     |FROZEN                |

        Rule: I should be notified when I enter a thermometer ID that is not configured for the location.
        Rule: I should not be able to proceed if the thermometer ID that is not configured for the location.
        Rule: I should be notified when I enter a device that is not a “Thermometer” and its category is not “Temperature”.
        Rule: I should not be able to enter the temperature information if the thermometer is not valid.
        @api @DIS-409
        Scenario Outline: Validate invalid thermometer scenarios
            Given I have a thermometer configured as location "<Device Location Code>", Device ID as "<Device ID>", Category as "<Device Category>" and Device Type as "<Device Type>".
            When I enter thermometer ID "<thermometer ID>" for the location code "<Imports Location Code>" and Temperature Category "<Temperature Category>".
            Then I should receive a "<message_type>" message response "<message>".
            Examples:
                | Imports Location Code | Device Location Code | thermometer ID | Device ID     | Temperature Category | Device Type | Device Category   | message_type | message                     |
                | 5678910               | 123456789            | THERM-DST-001  | THERM-DST-001 | ROOM_TEMPERATURE     | THERMOMETER | TEMPERATURE       | WARN         | Thermometer does not exist. |
                | 123456789             | 123456789            | THERM-DST-001  | THERM-DST-002 | ROOM_TEMPERATURE     | THERMOMETER | TEMPERATURE       | WARN         | Thermometer does not exist. |
                | 123456789             | 123456789            | THERM-DST-001  | THERM-DST-001 | ROOM_TEMPERATURE     | FREEZER     | SECONDARY_STORAGE | WARN         | Thermometer does not exist. |


        Rule: I should be able to enter the thermometer ID configured for location where products are imported.
        @ui @DIS-409
        Scenario Outline: Successfully enter a valid thermometer ID for the location
            Given I have a thermometer configured as location "<Device Location Code>", Device ID as "<Device ID>", Category as "<Device Category>" and Device Type as "<Device Type>".
            And The user location is "<Imports Location Code>".
            And I am at the Enter Shipping Information Page.
            And I select to enter information for a "<Temperature Category>" product category.
            Then The temperature field should be "disabled".
            When I enter thermometer ID "<thermometer ID>".
            Then The temperature field should be "enabled".
            Examples:
                | Imports Location Code | Device Location Code | thermometer ID | Device ID     | Temperature Category | Device Type | Device Category |
                | 123456789             | 123456789            | THERM-DST-001  | THERM-DST-001 | ROOM_TEMPERATURE     | THERMOMETER | TEMPERATURE     |


        Rule: I should not be able to enter the temperature information if the thermometer is not valid.
        @ui @DIS-409
        Scenario Outline: Attempt to enter an invalid thermometer ID
            Given I have a thermometer configured as location "<Device Location Code>", Device ID as "<Device ID>", Category as "<Device Category>" and Device Type as "<Device Type>".
            And The user location is "<Imports Location Code>".
            And I am at the Enter Shipping Information Page.
            And I select to enter information for a "<Temperature Category>" product category.
            Then The temperature field should be "disabled".
            When I enter thermometer ID "<thermometer ID>".
            Then I should see a "<message_type>" message: "<message>".
            And The temperature field should be "disabled".
            Examples:
                | Imports Location Code | Device Location Code | thermometer ID | Device ID     | Temperature Category | Device Type | Device Category | message_type | message                     |
                | 123456789             | 123456789            | THERM-DST-002  | THERM-DST-001 | ROOM_TEMPERATURE     | THERMOMETER | TEMPERATURE     | WARN         | Thermometer does not exist. |
