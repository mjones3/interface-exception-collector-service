@AOA-89
Feature: Shipment Creation


    Background:
        Given I have removed from the database all shipments which code contains with "DIS33300".

    @ui @DIS-333
    Scenario: Successful shipment creation
        Given The location "ABC1" is configured with "PRE1", "DIS333001", "0", and "YES".
        And I am on the Shipment Create Page.
        When I choose to create a shipment.
        And I have entered all required fields:
            | Field                           | Value      |
            | Customer                        | CUST001    |
            | Shipment Product Type           | PLASMA     |
            | Carton Tare Weight              | 1000g      |
            | Scheduled Ship Date             | <tomorrow> |
            | Transportation Reference Number | 111222333  |
        When I choose to submit the shipment.
        Then I should see a "SUCCESS" message: "Shipment created successfully".
        And I should be redirected to the Shipment Details page.

    Rule: I should be able to enter Transportation Reference Number.
        Rule: I should be able to receive a success message after a shipment is created successfully.
        @api @DIS-333
        Scenario: Successful shipment creation
            Given The location "ABC1" is configured with "PRE1", "DIS333002", "0", and "YES".
            When I request to create a new shipment with the values:
                | Field                           | Value      |
                | Customer                        | CUST001    |
                | Product Type                    | PLASMA     |
                | Carton Tare Weight              | 1000g      |
                | Shipment Date                   | <tomorrow> |
                | Transportation Reference Number | 111222333  |
            Then I should receive a "SUCCESS" message response "Shipment created with AI".
            And The shipment should be created with the following information:
                | Field                  | Value      |
                | shipping_customer_code | CUST001    |
                | product_category       | PLASMA     |
                | status                 | OPEN       |
                | create_date            | <not_null> |
                | shipmentNumber         | DIS3330021 |

    Rule: I should not be able to create a recovered plasma shipment for a location that is not configured for the recovered plasma shipping.
        Rule: The shipment date cannot be in the past.
        @api @DIS-333
        Scenario Outline: Cannot create recovered plasma shipment for invalid <Attribute>
            Given I attempt to create a shipment with the attribute "<Attribute>" as "<Attribute Value>".
            Then I should receive a "WARN" message response "<Error Message>".
            And The shipment "should not" be created.
            Examples:
                | Attribute    | Attribute Value | Error Message          |
                | locationCode | ABCDZ           | Location error message |
                | customerCode | 11111           | Customer code error    |
                | productType  | TYPE000         | Product type error     |
                | shipmentDate | 2020-01-01      | Past date error        |

    Rule: I should be required to choose a customer.
        Rule: I should be required to choose Shipment Product Type.
    Rule: I should be required to enter Carton Tare Weight. (weight should be in gram (gram) g)
        Rule: I should be required to enter Shipment Date.
        @api @DIS-333
        Scenario Outline: Required fields validation
            Given I attempt to create a shipment without filling the field "<Required Field>".
            Then I should receive a "WARN" message response "<Error Message>".
            Examples:
                | Required Field        | Error Message             |
                | Customer              | Customer is required      |
                | Shipment Product Type | Product type is required  |
                | Carton Tare Weight    | Weight is required        |
                | Shipment Date         | Shipment date is required |


    Rule: The system should generate a Unique Location Specific Shipment Number that contains the following based on the configuration.
    - Partner Prefix, if configured
    - Location Shipment Code
    - Shipment Sequence Number
        @api @DIS-333
        Scenario Outline: Unique shipment number generation
            Given The location "<Location Code>" is configured with "<Partner Prefix>", "<Shipment Code>", "<Amount of Shipments>", and "<Partner Prefix Active>".
            When I create a new shipment for this location.
            Then The generated shipment number should be "<Expected Shipment Number>"
            Examples:
                | Location Code | Partner Prefix | Shipment Code | Amount of Shipments | Partner Prefix Active | Expected Shipment Number |
                | WAR01         | ABC            | DIS333003     | 1                   | YES                   | ABCDIS3330032            |
                | WAR02         | ABC            | DIS333004     | 10                  | NO                    | DIS33300411              |

