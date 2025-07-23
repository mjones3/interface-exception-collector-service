@AOA-19
Feature: Transfer Receipt

    Background: Clean-up
        Given I have removed all imports using thermometer which code contains "-DST-456".
        And I have removed all created devices which ID contains "-DST-456".
        And I have removed all internal transfers which order number contains "45600001,45600002,45600003,45600004"

        Rule: The system should show the appropriate fields based on the given internal transfer number.
        Rule: The system should define the time zone based of the user location for the “Transit Start Time” field based on the internal transfer information.
        Rule: I should be notified when internal transfer order assigned to different facility.
        Rule: The comments field is required if internal transfer order received in a different facility.


        Rule: I should be able to input transfer details like transfer order number, transit date and time, temperature, thermometer ID and comments as necessary.
        Rule: I should be able to see the total transit time of the internal transfer if applicable.
        Rule: The system should define the time zone based of the user location for the “Transit End Time” field.
        Rule: I should be notified if the transit time is out of configured range.
        Rule: I should be able to enter temperature information of the internal transfer products.
        Rule: I should be notified when I enter a temperature that is out of the configured range.
        Rule: I should be able to enter the temperature in decimals.
        Rule: I should be able to enter the thermometer ID configured for location where internal transfer products are received.

        Rule: I should be notified when I enter a thermometer ID that is not configured for the location.
        Rule: I should not be able to proceed if the thermometer ID that is not configured for the location.
        Rule: I should not be able to enter the temperature information if the thermometer is not valid.
        Rule: I should not be able to proceed to the product information until all the required packing information is entered.
        Rule: The transit start date cannot be before transit end date.
        Rule: The transit end date cannot be in the future.



        Rule: The system should show the appropriate fields based on the given internal transfer number.
        Rule: I should be able to input transfer details like transfer order number, transit date and time, temperature, thermometer ID and comments as necessary.
        @api @DIS-456
        Scenario Outline: Request to enter Transfer receipt shipping information
        Given A Internal Transfer shipment is completed with the following details:
            | Order_Number  | Customer_ID  | Customer_Name  | Product_Family   | Temperature_Category  | Label_Status  | Quarantined_Products  |
            | <OrderNumber> | <CustomerId> | <CustomerName> | <Product_Family> | <TemperatureCategory> | <LabelStatus> | <QuarantinedProducts> |
        Given I request to validate the internal transfer order number "<OrderNumber>" from the location code "<Location_Code_From>".
        Then I should be able to enter information for the following attributes: "<Attributes to Fill>".
        And I should receive the order number as "<OrderNumber>" and temperature category as "<TemperatureCategory>"
        Examples:
            | OrderNumber | CustomerId | CustomerName      | Product_Family                    | TemperatureCategory | LabelStatus | QuarantinedProducts |Location_Code_From | TemperatureCategory | Attributes to Fill                                                                      |
            | 45600001    | DO1        | Distribution Only | PLASMA_TRANSFUSABLE               | FROZEN               | UNLABELED   | false               |123456789          | FROZEN             | displayTemperature:false,displayTransitInformation:false                                |
            | 45600002    | DO1        | Distribution Only | APHERESIS_PLATELETS_LEUKOREDUCED  | ROOM_TEMPERATURE     | LABELED     | false               |123456789          | ROOM_TEMPERATURE   | displayTemperature:true,displayTransitInformation:true,defaultTimeZone:America/New_York |
            | 45600003    | DO1        | Distribution Only | PLASMA_TRANSFUSABLE               | REFRIGERATED         | UNLABELED   | true                |123456789          | REFRIGERATED       | displayTemperature:true,displayTransitInformation:false                                 |


        Rule: Rule: I should be able to input transfer details like transfer order number, transit date and time, temperature, thermometer ID and comments as necessary.
        @api @DIS-456
        Scenario Outline: Request to enter invalid Transfer receipt shipping information
            Given A Internal Transfer shipment is completed with the following details:
                | Order_Number  | Customer_ID  | Customer_Name  | Product_Family   | Temperature_Category  | Label_Status  | Quarantined_Products  |
                | <OrderNumber> | <CustomerId> | <CustomerName> | <Product_Family> | <TemperatureCategory> | <LabelStatus> | <QuarantinedProducts> |
            Given I request to validate the internal transfer order number "<InvalidOrderNumber>" from the location code "<Location_Code_From>".
            Then I should receive a "<MessageType>" message response "<Message>".

            Examples:
                | OrderNumber | CustomerId | CustomerName      | Product_Family      | TemperatureCategory | LabelStatus | QuarantinedProducts | Location_Code_From | TemperatureCategory | InvalidOrderNumber | MessageType | Message                          |
                | 45600004    | DO1        | Distribution Only | PLASMA_TRANSFUSABLE | FROZEN              | UNLABELED   | false               | 123456789          | FROZEN              | 456000099          | WARN        | Internal Transfer Does not exist |


        Rule: I should be able to enter the thermometer ID configured for location where internal transfer products are received.
        Rule: I should be able to enter temperature information of the internal transfer products.
        Rule: The system should show the appropriate fields based on the given internal transfer number.
        Rule: I should be notified when I enter a temperature that is out of the configured range.
        Rule: I should be able to enter the temperature in decimals.
        Rule: I should not be able to enter the temperature information if the thermometer is not valid.
        Rule: I should be notified when I enter a thermometer ID that is not configured for the location.
        Rule: I should not be able to proceed if the thermometer ID that is not configured for the location.
        Rule: I should be able to input transfer details like transfer order number, transit date and time, temperature, thermometer ID and comments as necessary.
        @ui @DIS-456
        Scenario Outline: Successfully enter a valid thermometer ID for the location and Temperature
            Given A Internal Transfer shipment is completed with the following details:
                | Order_Number  | Customer_ID  | Customer_Name  | Product_Family   | Temperature_Category  | Label_Status  | Quarantined_Products  |
                | <OrderNumber> | <CustomerId> | <CustomerName> | <Product_Family> | <TemperatureCategory> | <LabelStatus> | <QuarantinedProducts> |
            And I have a thermometer configured as location "<Device Location Code>", Device ID as "<Device ID>", Category as "<Device Category>" and Device Type as "<Device Type>".
            And The following temperature thresholds are configured:
                | Temperature Category | Min Temperature | Max Temperature |
                | REFRIGERATED         |    1            |  10             |
                | ROOM_TEMPERATURE     |    20           |  24             |
            And The user location is "<Location_Code_From>".
            And I am at the Transfer Receipt Page.
            When I enter internal transfer order number "<OrderNumber>".
            Then The temperature field should be "disabled".
            When I enter thermometer ID "<Invalid Thermometer ID>".   (do we have to specify invalid thermometer id variable here?????)
            Then I should receive a "ERROR" message response "Thermometer ID is not configured for this location".
            And The temperature field should be "disabled".
            When I enter thermometer ID "<thermometer ID>".
            Then The temperature field should be "<Temperature Field Status>".
            Then The temperature field should be "enabled".
            When I enter the temperature "<Temperature>".
            Then The continue option should be "<continue_status>".
            And  I "<should_should_not>" see a "Caution" alert: "Temperature does not meet thresholds. All products will be quarantined.".
            Examples:
                | OrderNumber | CustomerId | CustomerName      | Product_Family      | TemperatureCategory | LabelStatus | QuarantinedProducts | Location_Code_From | Device Location Code | thermometer ID | Device ID     | Device Type | Device Category  | Temperature | Temperature Field Status | continue_status | should_should_not |
                | 45600004    | DO1        | Distribution Only | PLASMA_TRANSFUSABLE | REFRIGERATED        | LABELED     | false               | 123456789          | 123456789            | THERM-DST-409  | THERM-DST-409 | THERMOMETER | ROOM_TEMPERATURE | 9.10        | enabled                  | enabled         | should not        |
                | 45600005    | DO1        | Distribution Only | PLASMA_TRANSFUSABLE | REFRIGERATED        | UNLABELED   | false               | 123456789          | 123456789            | THERM-DST-409  | THERM-DST-409 | THERMOMETER | FROZEN           | 15.25       | disabled                 | enabled         | should            |


        Rule: I should be able to see the total transit time of the internal transfer if applicable.
        Rule: The system should define the time zone based of the user location for the “Transit End Time” field.
        Rule: I should be notified if the transit time is out of configured range.
        Rule: The system should define the time zone based of the user location for the “Transit Start Time” field based on the internal transfer information.

        @ui @DIS-456
            Scenario Outline: Enter transit time within different ranges
                Given A Internal Transfer shipment is completed with the following details:
                    | Order_Number  | Customer_ID  | Customer_Name  | Product_Family   | Temperature_Category  | Label_Status  | Quarantined_Products  |
                    | <OrderNumber> | <CustomerId> | <CustomerName> | <Product_Family> | <TemperatureCategory> | <LabelStatus> | <QuarantinedProducts> |
                And The following transit time thresholds are configured:
                    | Temperature Category | Min Transit Time | Max Transit Time |
                    | ROOM_TEMPERATURE     | 0                | (23.99 * 60)     |
                And I have a thermometer configured as location "123456789", Device ID as "THERM-DST-411", Category as "TEMPERATURE" and Device Type as "THERMOMETER".
                And The user location is "<Transfer Receipt Location Code>".
                And The location default timezone is configured as "<defaultLocationTimeZone>"
                And I am at the Transfer Receipt Page.
                When I enter internal transfer order number "<OrderNumber>".
                Then The start time zone field should be pre defined as "<defaultLocationTimeZoneSelected>".
                Then The end time zone field should be pre defined as "<defaultLocationTimeZoneSelected>".
                And I enter the Stat date time as "<StartDateTime>", Start Time Zone as "<StartTimeZone>", End date time as "<EndDateTime>".
                And I enter thermometer ID "THERM-DST-411".
                And I enter the temperature "20".
                When I choose calculate total transit time.
                Then The continue option should be "enabled".
                And I "<should_should_not_transit>" see the total transit time as "<totalTransitTime>".
                And  I "<should_should_not_caution>" see a "Caution" alert: "Total Transit Time does not meet thresholds. All products will be quarantined.".
                Examples:
                    | Transfer Receipt Location Code | defaultLocationTimeZone | StartDateTime       | StartTimeZone | EndDateTime         | defaultLocationTimeZoneSelected | totalTransitTime | should_should_not_transit | should_should_not_caution |
                    | 123456789                      | America/New_York        | 06/08/2025 14:00 AM | ET            | 06/08/2025 15:10 AM | ET                              | 1h 10m           | should                    | should not                |
                    | 123456789                      | America/New_York        | 06/08/2025 14:00 AM | ET            | 06/10/2025 14:00 AM | ET                              | 48h 0m           | should                    | should                    |


        #####can be removed if ui scenario cover this
        Rule: I should be notified when I enter a thermometer ID that is not configured for the location.
        Rule: I should not be able to proceed if the thermometer ID that is not configured for the location.
        @api @DIS-456
        Scenario Outline: Request to enter an invalid thermometer ID for the location
            Given A Internal Transfer shipment is completed with the following details:
                | Order_Number  | Customer_ID  | Customer_Name  | Product_Family   | Temperature_Category  | Label_Status  | Quarantined_Products  |
                | <OrderNumber> | <CustomerId> | <CustomerName> | <Product_Family> | <TemperatureCategory> | <LabelStatus> | <QuarantinedProducts> |
            And I request to validate the thermometer ID "<Invalid Thermometer ID>".
            And The user location is "<Location_Code_From>".
            Then I should receive a "ERROR" message response "Thermometer ID is not configured for this location".
            Examples:
                | OrderNumber | CustomerId | CustomerName      | Product_Family      | TemperatureCategory | LabelStatus | QuarantinedProducts | Location_Code_From | Invalid Thermometer ID |
                | 45600004    | DO1        | Distribution Only | PLASMA_TRANSFUSABLE | REFRIGERATED        | UNLABELED   | false               | 123456789          | THERM-DST-456          |




        ###### is this can be covered as ui unit test????
        Rule: The transit start date cannot be before transit end date.
        @ui @DIS-456
        Scenario Outline: Validate transit start date is not before transit end date
            Given A Internal Transfer shipment is completed with the following details:
                | Order_Number  | Customer_ID  | Customer_Name  | Product_Family   | Temperature_Category  | Label_Status  | Quarantined_Products  |
                | <OrderNumber> | <CustomerId> | <CustomerName> | <Product_Family> | <TemperatureCategory> | <LabelStatus> | <QuarantinedProducts> |
            And I have a thermometer configured as location "<Location_Code_From>", Device ID as "<Device ID>", Category as "<Device Category>" and Device Type as "<Device Type>".
            And The user location is "<Location_Code_From>".
            And I am at the Transfer Receipt Page.
            When I enter internal transfer order number "<OrderNumber>".
            And I enter transit start date as "<Transit Start Date>".
            And I enter transit end date as "<Transit End Date>".
            Then I should receive a "ERROR" message response "Transit start date cannot be after transit end date".
            Examples:
                | OrderNumber | CustomerId | CustomerName      | Product_Family      | TemperatureCategory | LabelStatus | QuarantinedProducts | Location_Code_From | Device ID     | Device Type | Device Category | Transit Start Date | Transit End Date |
                | 45600004    | DO1        | Distribution Only | PLASMA_TRANSFUSABLE | REFRIGERATED        | UNLABELED   | false               | 123456789          | THERM-DST-409 | THERMOMETER | TEMPERATURE     | 2023-06-15         | 2023-06-14       |



        ###### is this can be covered as ui unit test?????
        Rule: The transit end date cannot be in the future.
        @ui @DIS-456
        Scenario Outline: Validate transit end date is not in the future
            Given A Internal Transfer shipment is completed with the following details:
                | Order_Number  | Customer_ID  | Customer_Name  | Product_Family   | Temperature_Category  | Label_Status  | Quarantined_Products  |
                | <OrderNumber> | <CustomerId> | <CustomerName> | <Product_Family> | <TemperatureCategory> | <LabelStatus> | <QuarantinedProducts> |
            And I have a thermometer configured as location "<Location_Code_From>", Device ID as "<Device ID>", Category as "<Device Category>" and Device Type as "<Device Type>".
            And The user location is "<Location_Code_From>".
            And I am at the Transfer Receipt Page.
            And The current date is "<Current Date>".
            When I enter internal transfer order number "<OrderNumber>".
            And I enter transit start date as "<Transit Start Date>".
            And I enter transit end date as "<Transit End Date>".
            Then I should receive a "ERROR" message response "Transit end date cannot be in the future".
            Examples:
                | OrderNumber | CustomerId | CustomerName      | Product_Family      | TemperatureCategory | LabelStatus | QuarantinedProducts | Location_Code_From | thermometer ID | Device ID     | Device Type | Device Category | Temperature | Current Date | Transit Start Date | Transit End Date |
                | 45600004    | DO1        | Distribution Only | PLASMA_TRANSFUSABLE | REFRIGERATED        | UNLABELED   | false               | 123456789          | THERM-DST-409  | THERM-DST-409 | THERMOMETER | TEMPERATURE     | 4.0         | 2023-06-14   | 2023-06-13         | 2025-06-15       |


        Rule: I should be notified when internal transfer order assigned to different facility.
        Rule: The comments field is required if internal transfer order received in a different facility.
        Rule: I should not be able to proceed to the product information until all the required packing information is entered.
        @ui @DIS-456
        Scenario Outline: Request to enter Transfer receipt shipping information
            Given A Internal Transfer shipment is completed with the following details:
                | Order_Number  | Customer_ID  | Customer_Name  | Product_Family   | Temperature_Category  | Label_Status  | Quarantined_Products  |
                | <OrderNumber> | <CustomerId> | <CustomerName> | <Product_Family> | <TemperatureCategory> | <LabelStatus> | <QuarantinedProducts> |
            And The user location is "<User_Facility>".
            When I request to validate the internal transfer order number "<OrderNumber>" from the location code "<Location_Code_From>".
            Then I should receive a "WARN" message response "This transfer order is assigned to facility <Assigned_Facility> but is being received at <User_Facility>".
            And The continue option should be "<continue_status>".
            Examples:
                | OrderNumber | CustomerId | CustomerName      | Product_Family      | TemperatureCategory | LabelStatus | QuarantinedProducts | Location_Code_From | TemperatureCategory | continue_status |
                | 45600001    | DO1        | Distribution Only | PLASMA_TRANSFUSABLE | FROZEN              | UNLABELED   | false               | 123456789          | FROZEN              | disabled        |

