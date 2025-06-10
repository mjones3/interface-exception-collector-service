@AOA-109
Feature: Import products

    Background: Clean-up
        Given I have removed all created devices which ID contains "-DST-".

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

        Rule: I should be able to enter temperature information of the imported products.
        @api @DIS-410
        Scenario Outline: Successfully record temperature within acceptable range
            Given The following temperature thresholds are configured:
                | Temperature Category | Min Temperature | Max Temperature |
                | REFRIGERATED         |    1            |  10             |
                | ROOM_TEMPERATURE     |    20           |  24             |
            When I request to validate the temperature of "<Temperature>" for the Temperature Category "<Temperature Category>".
            Then The system "should" accept the temperature.
            Examples:
                |Temperature Category | Temperature |
                | REFRIGERATED        |  9          |
                | ROOM_TEMPERATURE    |  22         |

        Rule: I should be notified when I enter a temperature that is out of the configured range.
        @api @DIS-410
        Scenario Outline: Notification for out-of-range temperatures
            Given The following temperature thresholds are configured:
                | Temperature Category | Min Temperature | Max Temperature |
                | REFRIGERATED         |    1            |  10             |
                | ROOM_TEMPERATURE     |    20           |  24             |
            When I request to validate the temperature of "<Temperature>" for the Temperature Category "<Temperature Category>".
            Then I should receive a "<message_type>" message response "<message>".
            Examples:
                |Temperature Category | Temperature | message_type | message                                                               |
                | REFRIGERATED        |  12         | CAUTION      | Temperature does not meet thresholds all products will be quarantined |
                | ROOM_TEMPERATURE    |  25         | CAUTION      | Temperature does not meet thresholds all products will be quarantined |

        Rule: I should be able to enter temperature information of the imported products.
        Rule: I should be notified when I enter a temperature that is out of the configured range.
        @ui @DIS-410
        Scenario Outline: Enter temperature within acceptable different ranges
            Given I have a thermometer configured as location "<Device Location Code>", Device ID as "<Device ID>", Category as "<Device Category>" and Device Type as "<Device Type>".
            And The following temperature thresholds are configured:
                | Temperature Category | Min Temperature | Max Temperature |
                | REFRIGERATED         |    1            |  10             |
                | ROOM_TEMPERATURE     |    20           |  24             |
            And The user location is "<Imports Location Code>".
            And I am at the Enter Shipping Information Page.
            And I select to enter information for a "<Temperature Category>" product category.
            Then The temperature field should be "disabled".
            When I enter thermometer ID "<thermometer ID>".
            Then The temperature field should be "<Temperature Field Status>".
            When I enter the temperature "<Temperature>".
            Then The continue option should be "<continue_status>".
            And  I "<should_should_not>" see a "CAUTION" message: "Temperature does not meet thresholds all products will be quarantined".
            Examples:
                | Imports Location Code | Device Location Code | thermometer ID | Device ID     | Temperature Category | Device Type | Device Category | Temperature Field Status | Temperature | continue_status | should_should_not |
                | 123456789             | 123456789            | THERM-DST-001  | THERM-DST-001 | REFRIGERATED         | THERMOMETER | TEMPERATURE     | enabled                  | 9           | enabled         | should not        |
                | 123456789             | 123456789            | THERM-DST-001  | THERM-DST-001 | REFRIGERATED         | THERMOMETER | TEMPERATURE     | enabled                  | 15          | disabled        | should            |
