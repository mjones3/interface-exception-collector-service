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


        @api @DIS-456
        Scenario Outline: Request to enter invalid Transfer receipt shipping information
            Given A Internal Transfer shipment is completed with the following details:
                | Order_Number  | Customer_ID  | Customer_Name  | Product_Family   | Temperature_Category  | Label_Status  | Quarantined_Products  |
                | <OrderNumber> | <CustomerId> | <CustomerName> | <Product_Family> | <TemperatureCategory> | <LabelStatus> | <QuarantinedProducts> |
            Given I request to validate the internal transfer order number "<InvalidOrderNumber>" from the location code "<Location_Code_From>".
            Then I should receive a "<MessageType>" message response "<Message>".

            Examples:
                | OrderNumber| CustomerId | CustomerName      | Product_Family       | TemperatureCategory | LabelStatus | QuarantinedProducts |Location_Code_From | TemperatureCategory|InvalidOrderNumber| MessageType | Message                           |
                | 45600004   | DO1        | Distribution Only | PLASMA_TRANSFUSABLE  | FROZEN              | UNLABELED   | false              |123456789          | FROZEN             | 456000099        | WARN        | Internal Transfer Does not exist  |



            @ui @DIS-456
            Scenario Outline: Successfully enter a valid thermometer ID for the location
                Given A Internal Transfer shipment is completed with the following details:
                    | Order_Number  | Customer_ID  | Customer_Name  | Product_Family   | Temperature_Category  | Label_Status  | Quarantined_Products  |
                    | <OrderNumber> | <CustomerId> | <CustomerName> | <Product_Family> | <TemperatureCategory> | <LabelStatus> | <QuarantinedProducts> |
                And I have a thermometer configured as location "<Device Location Code>", Device ID as "<Device ID>", Category as "<Device Category>" and Device Type as "<Device Type>".
                And The user location is "<Location_Code_From>".
                And I am at the Transfer Receipt Page.
                When I enter internal transfer order number "<OrderNumber>".
                Then The temperature field should be "disabled".
                When I enter thermometer ID "<thermometer ID>".
                Then The temperature field should be "enabled".
                Examples:
                Examples:
                    | OrderNumber| CustomerId | CustomerName      | Product_Family       | TemperatureCategory | LabelStatus | QuarantinedProducts |Location_Code_From | TemperatureCategory| thermometer ID | Device ID     | Device Type | Device Category |
                    | 45600004   | DO1        | Distribution Only | PLASMA_TRANSFUSABLE  | FROZEN              | UNLABELED   | false              |123456789          | FROZEN              | THERM-DST-409  | THERM-DST-409 |  THERMOMETER | TEMPERATURE     |



