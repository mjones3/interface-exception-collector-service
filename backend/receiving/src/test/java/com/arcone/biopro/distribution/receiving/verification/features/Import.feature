@AOA-109
Feature: Import products

    Rule: I should be able to input shipping details like product category, transit date and time, temperature, thermometer ID and comments as necessary.
    Rule: The system should show the appropriate fields based on the selected product category.
    @api @DIS-406
    Scenario Outline: Request to enter shipping information
        Given I request to enter shipping data for a "<Temperature Category>" product category.
        Then I should be able to enter information for the following attributes: "<Attributes to Fill>".
        Examples:
            | Temperature Category | Attributes to Fill                                       |
            | FROZEN               | displayTemperature:false,displayTransitInformation:false |
            | ROOM_TEMPERATURE     | displayTemperature:true,displayTransitInformation:true   |
            | REFRIGERATED         | displayTemperature:true,displayTransitInformation:false  |

        Rule: I should be able to input shipping details like product category, transit date and time, temperature, thermometer ID and comments as necessary.
        Rule: The system should show the appropriate fields based on the selected product category.
        @ui @DIS-406
        Scenario Outline: Enter shipping information
            Given I am at the Enter Shipping Information Page.
            When I select to enter information for a "<Temperature Category>" product category.
            Then I "should" be able to fill the following fields: "<Attributes to Fill>".
            And  I "should not" be able to fill the following fields: "<Attributes do not Fill>".
            Examples:
                | Temperature Category | Attributes to Fill                                                                                                                             | Attributes do not Fill                                                                                                               |
                | FROZEN               | Comments                                                                                                                                       | Transit Start Date, Transit Start Time,Start Time Zone,Transit End Date, Transit End Time, End Time Zone , Temperature , Thermometer |
                | ROOM_TEMPERATURE     | Transit Start Date, Transit Start Time,Start Time Zone,Transit End Date, Transit End Time, End Time Zone , Temperature , Thermometer, Comments |                                                                                                                                      |
                | REFRIGERATED         | Temperature , Thermometer, Comments                                                                                                            | Transit Start Date, Transit Start Time,Start Time Zone,Transit End Date, Transit End Time, End Time Zone                             |



    Rule: I should be able to enter the thermometer ID configured for location where products are imported.
    Rule: I should be notified when I enter a thermometer ID that is not configured for the location.
    Rule: I should not be able to proceed if the thermometer ID that is not configured for the location.
    Rule: I should be notified when I enter a device that is not a “Thermometer” and its category is not “Temperature”.
    Rule: I should not be able to enter the temperature information if the thermometer is not valid.
    @api @DIS-409
    Scenario Outline: Validate different thermometer scenarios
        Given I have a thermometer configured as location "<Device Location Code>", Device ID as "<Device ID>", Category as "<Device Category>" and Device Type as "<Device Type>".
        When I enter thermometer ID "<thermometer ID>" for the location code "<Imports Location Code>" and Temperature Category "<Temperature Category>".
        Then I should receive a "<error_type>" message response "<error_message>".
        Examples:
        |Imports Location Code | Device Location Code | thermometer ID | Device ID | Temperature Category | Device Type | Device Category   | error_type | error_message                    |
        | 123456789            |   123456789          | THERM-001      | THERM-001 |  ROOM_TEMPERATURE    | THERMOMETER | TEMPERATURE       | SUCCESS    | Valid Device                     |
        | 5678910              |   123456789          | THERM-001      | THERM-001 |  ROOM_TEMPERATURE    | THERMOMETER | TEMPERATURE       | WARN       | Device different location error  |
        | 123456789            |   123456789          | THERM-001      | THERM-002 |  ROOM_TEMPERATURE    | THERMOMETER | TEMPERATURE       | WARN       | Device does not exist error      |
        | 123456789            |   123456789          | THERM-001      | THERM-001 |  ROOM_TEMPERATURE    | FREEZER     | SECONDARY_STORAGE | WARN       | Device different category error  |


    Rule: I should be able to enter the thermometer ID configured for location where products are imported.
    @ui @DIS-409
    Scenario Outline: Successfully enter a valid thermometer ID for the location
        Given I have a thermometer configured as location "<Device Location Code>", Device ID as "<Device ID>", Category as "<Device Category>" and Device Type as "<Device Type>".
        And The user location is "<Imports Location Code>".
        And I am at the Enter Shipping Information Page.
        And I select to enter information for a "<Temperature Category>" product category.
        Then The temperature field should be "disabled"
        When I enter thermometer ID "<thermometer ID>"
        Then The temperature field should be "<Temperature Field Status>"
        Examples:
            |Imports Location Code | Device Location Code | thermometer ID | Device ID | Temperature Category | Device Type | Device Category | Temperature Field Status |
            | 123456789            |   123456789          | THERM-001      | THERM-001 |  ROOM_TEMPERATURE    | THERMOMETER | TEMPERATURE     | enabled                  |


    Rule: I should not be able to enter the temperature information if the thermometer is not valid.
    @ui @DIS-409
    Scenario Outline: Attempt to enter an invalid thermometer ID
        Given I have a thermometer configured as location "<Device Location Code>", Device ID as "<Device ID>", Category as "<Device Category>" and Device Type as "<Device Type>".
        And The user location is "<Imports Location Code>".
        And I am at the Enter Shipping Information Page.
        And I select to enter information for a "<Temperature Category>" product category.
        Then The temperature field should be "disabled"
        When I enter thermometer ID "<thermometer ID>"
        Then I should see a "<message_type>" message: "<message>".
        And The temperature field should be "<Temperature Field Status>"
        Examples:
            |Imports Location Code | Device Location Code | thermometer ID | Device ID | Temperature Category | Device Type | Device Category | message_type | message       | Temperature Field Status |
            | 123456789            |   123456789          | THERM-001      | THERM-001 |  ROOM_TEMPERATURE    | THERMOMETER | TEMPERATURE     | SUCCESS      | Valid Device  | disabled                 |




