@AOA-89
Feature: View Shipment Details

    Background:
        Given I have removed from the database all the configurations for the location "DIS_335".
        And I have removed from the database all shipments which code contains with "DIS33500".
        And The location "DIS_335" is configured with prefix "DIS_335", shipping code "DIS33500", and prefix configuration "Y".

    Rule: I should be able to view Shipment Information (Shipment Number, Customer Code, Customer Name, Product Type, Shipment Status, Shipment Date, Transportation Reference Number).
    Rule: I should be able to view the total number of the products (Total Products) in the shipment.
    Rule: I should be able to view the total number of cartons (Total Cartons) in the shipment.
    @ui @DIS-335
    Scenario: View complete shipment information
        Given There is a shipment with the following details:
            | Field                           | Value                         |
            | Shipment Number                 | DIS_335DIS335001              |
            | Customer                        | Bio Products                  |
            | Product Type                    | RP NONINJECTABLE REFRIGERATED |
            | Carton Tare Weight              | 1000                          |
            | Shipment Date                   | <tomorrow>                    |
            | Transportation Reference Number | 111222333                     |
        When I navigate to the shipment details page for "DIS_335DIS335001"
        Then I should see the following shipment information:
            | Field                        | Value            |
            | Shipment Number              | DIS_335DIS335001 |
            | Customer Code                | CUST123          |
            | Customer Name                | Plasma Corp      |
            | Product Type                 | Recovered Plasma |
            | Shipment Status              | In Progress      |
            | Shipment Date                | 2024-01-20       |
            | Transportation Reference No. | TRN-456789       |
            | Total Products               | 0                |
            | Total Cartons                | 0                |


    Rule: I should have an option to add carton to the shipment.
    Rule: I should have an option to go back to Search Shipments list.
    @ui @DIS-335
    Scenario: Access add carton functionality / Navigate back to Search Shipments list
        Given I am viewing the shipment details for "SHP-2024-001"
        When I look for the add carton option
        Then I should see an "Add Carton" button
        When I click on the "Back to Search" button
        Then I should be redirected to the Search Shipments page


    Rule: I should be able to view Shipment Information (Shipment Number, Customer Code, Customer Name, Product Type, Shipment Status, Shipment Date, Transportation Reference Number).
    Rule: I should be able to view the total number of the products (Total Products) in the shipment.
    Rule: I should be able to view the total number of cartons (Total Cartons) in the shipment.
    @api @DIS-335
    Scenario: Successfully retrieve shipment details via API
        Given There is a shipment with the following details:
            | Field                           | Value                         |
            | Shipment Number                 | DIS_335DIS335001              |
            | Customer                        | Bio Products                  |
            | Product Type                    | RP NONINJECTABLE REFRIGERATED |
            | Carton Tare Weight              | 1000                          |
            | Shipment Date                   | <tomorrow>                    |
            | Transportation Reference Number | 111222333                     |
        When I send a GET request to "/api/shipments/DIS_335DIS335001"
        Then the response status code should be 200
        And the response should contain the following shipment information:
            | field                      | value         |
            | shipmentNumber             | SHP-2024-001  |
            | customerCode               | CUST001       |
            | customerName               | Test Customer |
            | productType                | Plasma        |
            | shipmentStatus             | Created       |
            | shipmentDate               | 2024-01-20    |
            | transportationReferenceNo  | TRN001        |
            | totalProducts              | 100           |
            | totalCartons               | 20            |

    @api @DIS-335
    Scenario: Attempt to retrieve non-existent shipment
        When I send a GET request to "/api/shipments/NON-EXISTENT"
        Then the response status code should be 404
        And the response should contain error message "Shipment not found"
