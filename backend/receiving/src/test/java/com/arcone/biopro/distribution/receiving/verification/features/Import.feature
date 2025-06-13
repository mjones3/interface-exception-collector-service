@AOA-109
Feature: Import products

    Background: Clean-up
        Given I have removed all created devices which ID contains "-DST-410".
        And I have removed all created devices which ID contains "-DST-411".

    Rule: I should be able to input shipping details like product category, transit date and time, temperature, thermometer ID and comments as necessary.
    Rule: The system should show the appropriate fields based on the selected product category.
    Rule: The system should define the time zone based on the user location for the “Transit Time Zone to” field.
    Rule: The system should pre-populate the current user location date and time by default.
    @api @DIS-406 @DIS-411
    Scenario Outline: Request to enter shipping information
        Given I request to enter shipping data for a "<Temperature Category>" product category and location code "<Location Code>".
        Then I should be able to enter information for the following attributes: "<Attributes to Fill>".
        Examples:
            |Location Code | Temperature Category | Attributes to Fill                                                                      |
            |123456789     | FROZEN               | displayTemperature:false,displayTransitInformation:false                                |
            |123456789     | ROOM_TEMPERATURE     | displayTemperature:true,displayTransitInformation:true,defaultTimeZone:America/New_York |
            |123456789     | REFRIGERATED         | displayTemperature:true,displayTransitInformation:false                                 |
            |DO1           | ROOM_TEMPERATURE     | displayTemperature:true,displayTransitInformation:true,defaultTimeZone:America/Chicago  |

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
                | Temperature Category | Temperature | message_type | message                                                                 |
                | REFRIGERATED         | 12          | CAUTION      | Temperature does not meet thresholds. All products will be quarantined. |
                | ROOM_TEMPERATURE     | 25          | CAUTION      | Temperature does not meet thresholds. All products will be quarantined. |

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
            And  I "<should_should_not>" see a "Caution" alert: "Temperature does not meet thresholds all products will be quarantined".
            Examples:
                | Imports Location Code | Device Location Code | thermometer ID | Device ID     | Temperature Category | Device Type | Device Category | Temperature Field Status | Temperature | continue_status | should_should_not |
                | 123456789             | 123456789            | THERM-DST-410  | THERM-DST-410 | REFRIGERATED         | THERMOMETER | TEMPERATURE     | enabled                  | 9           | enabled         | should not        |
                | 123456789             | 123456789            | THERM-DST-410  | THERM-DST-410 | REFRIGERATED         | THERMOMETER | TEMPERATURE     | enabled                  | 15          | enabled         | should            |

    Rule: I should be able to see the total transit time of the imported products.
        @api @DIS-411
        Scenario Outline: Successfully record transit time within acceptable range
            Given The following transit time thresholds are configured:
                  | Temperature Category | Min Transit Time | Max Transit Time |
                  | ROOM_TEMPERATURE     |    1             |  24              |
            When I request to validate the total transit time of Stat date time as "<StartDateTime>", Start Time Zone as "<StartTimeZone>", End date time as "<EndDateTime>" and End Time Zone as "<EndTimeZone>"  for the Temperature Category "<Temperature Category>".
            Then The system "should" accept the transit time.
            And I should receive the total transit time as "<totalTransitTime>".
            Examples:
                |Temperature Category | StartDateTime              | StartTimeZone    | EndDateTime              | EndTimeZone      | totalTransitTime |
                | ROOM_TEMPERATURE    |  2025-06-08T05:22:53.108Z  | America/New_York | 2025-06-08T13:28:53.108Z | America/New_York | 8h 6m            |

        Rule: I should be notified if the transit time is out of configured range.
        @api @DIS-411
        Scenario Outline: Notification for out-of-range transit time
            Given The following transit time thresholds are configured:
                | Temperature Category | Min Transit Time | Max Transit Time |
                | ROOM_TEMPERATURE     |    1             |  24              |
            When I request to validate the total transit time of Stat date time as "<StartDateTime>", Start Time Zone as "<StartTimeZone>", End date time as "<EndDateTime>" and End Time Zone as "<EndTimeZone>"  for the Temperature Category "<Temperature Category>".
            Then I should receive a "<message_type>" message response "<message>".
            Examples:
                | Temperature Category | StartDateTime            | StartTimeZone    | EndDateTime              | EndTimeZone       | message_type | message                                                                        |
                | ROOM_TEMPERATURE     | 2025-06-02T05:22:53.108Z | America/New_York | 2025-06-08T13:28:53.108Z | America/New_York  | CAUTION      | Total Transit Time does not meet thresholds. All products will be quarantined. |
                | FROZEN               | 2025-06-02T05:22:53.108Z | America/New_York | 2025-06-08T13:28:53.108Z | America/New_York  | SYSTEM       | Not able to validate transit time. Contact Support.                            |
                | ROOM_TEMPERATURE     | 2025-06-02T05:22:53.108Z | America/New_York | 2025-06-08T13:28:53.108Z | INVALID_TIME_ZONE | SYSTEM       | Not able to validate transit time. Contact Support.                            |


        Rule: I should be able to see the total transit time of the imported products.
        Rule: The system should define the time zone based on the user location for the “Transit Time Zone to” field.
        Rule: The system should pre-populate the current user location date and time by default.
        Rule: I should be notified if the transit time is out of configured range.
        @ui @DIS-411
        Scenario Outline: Enter transit time within different ranges
            Given The following transit time thresholds are configured:
                | Temperature Category | Min Transit Time | Max Transit Time |
                | ROOM_TEMPERATURE     | 1                | 24               |
            And I have a thermometer configured as location "123456789", Device ID as "THERM-DST-411", Category as "TEMPERATURE" and Device Type as "THERMOMETER".
            And The user location is "<Imports Location Code>".
            And The location default timezone is configured as "<defaultLocationTimeZone>"
            And I am at the Enter Shipping Information Page.
            And I select to enter information for a "<Temperature Category>" product category.
            Then The end time zone field should be pre defined as "<defaultLocationTimeZoneSelected>".
            And I enter the Stat date time as "<StartDateTime>", Start Time Zone as "<StartTimeZone>", End date time as "<EndDateTime>".
            And I enter thermometer ID "THERM-DST-411".
            And I enter the temperature "20".
            When I choose calculate total transit time.
            Then The continue option should be "<continue_status>".
            And I "<should_should_not_transit>" see the total transit time as "<totalTransitTime>".
            And  I "<should_should_not_caution>" see a "Caution" alert: "Total Transit Time does not meet thresholds. All products will be quarantined.".
            Examples:
                | Imports Location Code | defaultLocationTimeZone | Temperature Category | StartDateTime       | StartTimeZone    | EndDateTime         | defaultLocationTimeZoneSelected | totalTransitTime | continue_status | should_should_not_transit | should_should_not_caution |
                | 123456789             | America/New_York        | ROOM_TEMPERATURE     | 06/08/2025 14:00 AM | America/New_York | 06/08/2025 15:10 AM | ET                              | 1h 10m           | enabled         | should                    | should not                |
                | 123456789             | America/New_York        | ROOM_TEMPERATURE     | 06/08/2025 14:00 AM | America/New_York | 06/10/2025 14:00 AM | ET                              |                  | disable         | should not                | should                    |




