@AOA-89
Feature: Shipment Creation


    Background:
        Given I have removed from the database all the configurations for the location "123456789_TEST".
        And I have removed from the database all shipments which code contains with "DIS33300".

    @ui @DIS-333
    Scenario: Successful shipment creation
        Given The location "123456789_TEST" is configured with prefix "BPM_TEST", shipping code "DIS333001", and prefix configuration "Y".
        And I am on the Shipment Create Page.
        When I choose to create a shipment.
        And I have entered all the fields:
            | Field                           | Value                         |
            | Customer                        | Bio Products                  |
            | Product Type                    | RP NONINJECTABLE REFRIGERATED |
            | Carton Tare Weight              | 1000                          |
            | Shipment Date                   | <tomorrow>                    |
            | Transportation Reference Number | 111222333                     |
        When I choose to submit the shipment.
        Then I should see a "SUCCESS" message: "Shipment created successfully".
        And I should be redirected to the Shipment Details page.

        Rule: I should be able to enter Transportation Reference Number.
        Rule: I should be able to receive a success message after a shipment is created successfully.
        @api @DIS-333
        Scenario: Successful shipment creation
            Given The location "123456789_TEST" is configured with prefix "BPM_TEST", shipping code "DIS333002", and prefix configuration "Y".
            When I request to create a new shipment with the values:
                | Field                           | Value                      |
                | Customer Code                   | 408                        |
                | Product Type                    | RP_FROZEN_WITHIN_120_HOURS |
                | Carton Tare Weight              | 1000                       |
                | Shipment Date                   | <tomorrow>                 |
                | Transportation Reference Number | <null>                     |
                | Location Code                   | 123456789_TEST             |
            Then I should receive a "SUCCESS" message response "Shipment created successfully".
            And The shipment should be created with the following information:
                | Field                           | Value                      |
                | customer_code                   | 408                        |
                | product_type                    | RP_FROZEN_WITHIN_120_HOURS |
                | status                          | OPEN                       |
                | carton_tare_weight              | 1000                       |
                | create_date                     | <not_null>                 |
                | transportation_reference_number | <null>                     |
                | location_code                   | 123456789_TEST             |
                | shipment_date                   | <not_null>                 |

        Rule: I should be required to enter Carton Tare Weight. (weight should be in gram (g))
        Rule: I should not be able to create a recovered plasma shipment for a location that is not configured for the recovered plasma shipping.
        @api @DIS-333
        Scenario Outline: Cannot create recovered plasma shipment for invalid <Attribute>
            Given I attempt to create a shipment with the attribute "<Attribute>" as "<Attribute Value>".
            Then I should receive a "WARN" message response "<Error Message>".
            And The shipment "should not" be created.
            Examples:
                | Attribute             | Attribute Value | Error Message                  |
                | cartonTareWeight      | <null>          | Carton tare weight is required |
                | locationCode          | DL1             | Location is required           |
                | customerCode          | 11111           | Domain not found for key 11111 |
                | productType           | TYPE000         | Product type is required       |


        Rule: I should be required to choose Product Type.
        Rule: I should be required to choose a customer.
        Rule: I should be required to enter Scheduled Shipment Date.
        Rule: The shipment date cannot be in the past.
        @api @DIS-333
        Scenario Outline: Cannot create recovered plasma shipment for invalid <Attribute>
            Given I attempt to create a shipment with the attribute "<Attribute>" as "<Attribute Value>".
            Then I should receive a "BAD_REQUEST" error message response "<Error Message>".
            And The shipment "should not" be created.
            Examples:
                | Attribute             | Attribute Value | Error Message                       |
                | shipmentDate          | <null>          | Shipment date is required           |
                | customerCode          | <null>          | Customer code is required           |
                | productType           | <null>          | Product type is required            |
                | shipmentDate          | 2020-01-01      | Shipment date must be in the future |


    Rule: The system should generate a Unique Location Specific Shipment Number that contains the following based on the configuration.
    - Partner Prefix, if configured
    - Location Shipment Code
    - Shipment Sequence Number
        @api @DIS-333
        Scenario Outline: Unique shipment number generation
            Given The location "123456789_TEST" is configured with prefix "BPM_TEST", shipping code "DIS333003", and prefix configuration "<Prefix Configuration>".
            When I request to create a new shipment with the values:
                | Field                           | Value                      |
                | Customer Code                   | 408                        |
                | Product Type                    | RP_FROZEN_WITHIN_120_HOURS |
                | Carton Tare Weight              | 1000                       |
                | Shipment Date                   | <tomorrow>                 |
                | Transportation Reference Number | <null>                     |
                | Location Code                   | 123456789_TEST             |
            Then The generated shipment number should starts with "<Expected Shipment Number>" and ends with the next shipment count number.
            Examples:
                | Prefix Configuration | Expected Shipment Number |
                | Y                    | BPM_TESTDIS333003        |
                | N                    | DIS333003                |

