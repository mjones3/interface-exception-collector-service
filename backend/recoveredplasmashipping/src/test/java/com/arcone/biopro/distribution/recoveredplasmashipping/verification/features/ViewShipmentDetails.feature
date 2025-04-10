@AOA-89
Feature: View Shipment Details

    Background:
        Given I have removed from the database all the configurations for the location "DIS_335".
        And I have removed from the database all shipments which code contains with "DIS33500".
        And I have removed from the database all shipments from location "123456789" with transportation ref number "DIS335".
        And The location "DIS_335" is configured with prefix "DIS_335", shipping code "DIS33500", and prefix configuration "Y".

        Rule: I should be able to view Shipment Information (Shipment Number, Customer Code, Customer Name, Product Type, Shipment Status, Shipment Date, Transportation Reference Number).
        Rule: I should be able to view the total number of the products (Total Products) in the shipment.
        Rule: I should be able to view the total number of cartons (Total Cartons) in the shipment.
        Rule: I should have an option to add carton to the shipment.
        Rule: I should have an option to go back to Search Shipments list.
        @ui @DIS-335
        Scenario: View complete shipment information
            Given I request to create a new shipment with the values:
                | Field                           | Value                         |
                | Customer Code                   | 410                           |
                | Product Type                    | RP_NONINJECTABLE_REFRIGERATED |
                | Carton Tare Weight              | 1000                          |
                | Shipment Date                   | <tomorrow>                    |
                | Transportation Reference Number | DIS335                        |
                | Location Code                   | 123456789                     |
            When I navigate to the shipment details page for the last shipment created.
            Then I should see the following shipment information:
                | Field                      | Value                         |
                | Shipment Number Prefix     | BPM2765                       |
                | Customer Code              | 410                           |
                | Customer Name              | BIO PRODUCTS                  |
                | Product Type               | RP NONINJECTABLE REFRIGERATED |
                | Shipment Status            | OPEN                          |
                | Shipment Date              | <tomorrow>                    |
                | Transportation Ref. Number | DIS335                        |
                | Total Products             | 0                             |
                | Total Cartons              | 0                             |
            And I should have an option to add a carton to the shipment.
            And It should be possible to navigate back to the Search page.


    Rule: I should be able to view Shipment Information (Shipment Number, Customer Code, Customer Name, Product Type, Shipment Status, Shipment Date, Transportation Reference Number).
    Rule: I should be able to view the total number of the products (Total Products) in the shipment.
    Rule: I should be able to view the total number of cartons (Total Cartons) in the shipment.
    @api @DIS-335
    Scenario: Successfully retrieve shipment details via API
        Given I request to create a new shipment with the values:
            | Field                           | Value                     |
            | Customer Code                   | 409                       |
            | Product Type                    | RP_FROZEN_WITHIN_72_HOURS |
            | Carton Tare Weight              | 600                       |
            | Shipment Date                   | <tomorrow>                |
            | Transportation Reference Number | DIS335                    |
            | Location Code                   | DIS_335                   |
        When I request the last created shipment data.
        Then The find shipment response should contain the following information:
            | field                         | value                     |
            | shipmentNumber                | DIS_335DIS33500           |
            | customerCode                  | 409                       |
            | customerName                  | Southern Biologics        |
            | locationCode                  | DIS_335                   |
            | productType                   | RP_FROZEN_WITHIN_72_HOURS |
            | shipmentStatus                | OPEN                      |
            | transportationReferenceNumber | DIS335                    |
            | totalProducts                 | 0                         |
            | totalCartons                  | 0                         |
            | canAddCartons                 | true                      |

    @api @DIS-335
    Scenario: Attempt to retrieve non-existent shipment
        When I request to find the shipment "1111" at location "DIS_335".
        Then I should receive a "WARN" message response "Domain not found for key 1111".
