@AOA-89
Feature: Create Carton

    Background:
        Given I have removed from the database all the configurations for the location "123456789_DIS338".
        And I have removed from the database all shipments which code contains with "DIS33800".
        And The location "123456789_DIS338" is configured with prefix "DIS_338", shipping code "DIS33800", and prefix configuration "Y".

        Rule: I should be able to generate a Unique Location Specific Carton Number that contains the following: Partner Prefix + Location Carton Code + Carton Sequence Number
        Rule: I should be able to view the carton details.
        Rule: I should be able to view the shipment details.
        Rule: I should be notified when the carton is added to the shipment.
        Rule: I should be able to view the list of cartons added in the shipment.
        @ui @DIS-338
        Scenario: Successfully add carton to shipment through UI
            Given I request to create a new shipment with the values:
                | Field                           | Value                         |
                | Customer Code                   | 410                           |
                | Product Type                    | RP_NONINJECTABLE_REFRIGERATED |
                | Carton Tare Weight              | 1000                          |
                | Shipment Date                   | <tomorrow>                    |
                | Transportation Reference Number | DIS338                        |
                | Location Code                   | 123456789                     |
            And I navigate to the shipment details page for the last shipment created.
            When I choose to add a carton to the shipment.
            Then I should be redirected to the Add Carton Products page.
            And I should see a "SUCCESS" message: "Carton created successfully".
            And I close the acknowledgment message.
            And I should see the carton details:
                | Field                | Value |
                | Carton Number Prefix | BPMH1 |
                | Carton Sequence      | 1     |
                | Tare Weight          | 1000  |
                | Total Volume         | 0     |
                | Minimum Products     |       |
                | Maximum Products     |       |
            And I should see the following shipment information:
                | Field                      | Value                         |
                | Shipment Number Prefix     | BPM2765                       |
                | Customer Code              | 410                           |
                | Customer Name              | BIO PRODUCTS                  |
                | Product Type               | RP NONINJECTABLE REFRIGERATED |
                | Shipment Status            | OPEN                          |
                | Shipment Date              | <tomorrow>                    |
                | Transportation Ref. Number | DIS338                        |
                | Total Products             | 0                             |
                | Total Cartons              | 1                             |
            When I click to go back to Shipment Details page.
            Then I should see the list of cartons added to the shipment containing:
                | Carton Number Prefix | Sequence | Status |
                | BPMMH1               | 1        | OPEN   |

        Rule: I should not be able to add cartons in a shipment from a different location that the user is logged in.
        @ui @DIS-338
        Scenario: Unable to add carton to a shipment from a different location
            Given The location "123456789_DIS338" is configured with prefix "DIS_338", shipping code "DIS33800", and prefix configuration "Y".
            And I request to create a new shipment with the values:
                | Field                           | Value                         |
                | Customer Code                   | 410                           |
                | Product Type                    | RP_NONINJECTABLE_REFRIGERATED |
                | Carton Tare Weight              | 1000                          |
                | Shipment Date                   | <tomorrow>                    |
                | Transportation Reference Number | DIS338                        |
                | Location Code                   | DIS_338                       |
            When I navigate to the shipment details page for the last shipment created.
            Then The Add Carton button should be "disabled".

    Rule: The carton number must be unique for the blood center.
        Rule: I should be able to view a sequence number for every carton generated in the shipment in a sequential order.
        @api @DIS-338
        Scenario: Verify unique carton number generation
            Given I request to create a new shipment with the values:
                | Field                           | Value                         |
                | Customer Code                   | 410                           |
                | Product Type                    | RP_NONINJECTABLE_REFRIGERATED |
                | Carton Tare Weight              | 1000                          |
                | Shipment Date                   | <tomorrow>                    |
                | Transportation Reference Number | DIS338                        |
                | Location Code                   | 123456789_DIS338              |
            And I request to add 2 cartons to the shipment.
            When I request the last created shipment data.
            Then The find shipment response should have the following information:
                | Information          | Value                 |
                | Total Cartons        | 2                     |
                | Carton Number Prefix | DIS_338MH1,DIS_338MH1 |
                | Sequence Number      | 1,2                   |

        Rule: I should be notified when the carton is not added to the shipment.
        @api @DIS-338
        Scenario: Verify unique carton number generation
            Given I request to create a new shipment with the values:
                | Field                           | Value                         |
                | Customer Code                   | 410                           |
                | Product Type                    | RP_NONINJECTABLE_REFRIGERATED |
                | Carton Tare Weight              | 1000                          |
                | Shipment Date                   | <tomorrow>                    |
                | Transportation Reference Number | DIS338                        |
                | Location Code                   | 123456789                     |
            And I request to add 1 carton to the shipment 1111.
            Then I should receive a "SYSTEM" message response "Carton generation error. Contact Support.".
