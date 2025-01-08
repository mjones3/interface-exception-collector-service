@api @AOA-6 @AOA-152
Feature: Shipment fulfillment request

    Background:
        Given I cleaned up from the database the packed item that used the unit number "W822530106093,W822530106094,W812530106095,W812530106097,W812530106098".
        And I cleaned up from the database, all shipments with order number "1321,1331,1341,1351,1361,1371,1381,1391,1392,1393,1394,1395".

        Rule: I should be able to receive the shipment fulfillment request.
        Rule: I should be able to persist the shipment fulfilled request on the local store.
        @DIS-65 @DIS-57
        Scenario: Receive shipment fulfillment request
            Given I have no shipment fulfillment requests.
            When I receive a shipment fulfillment request event.
            Then The shipment request will be available in the Distribution local data store and I can fill the shipment.


        Rule: I should be able to list the pending shipment fulfillment request.
        Rule: I should be able to view the shipment fulfillment details.
        @DIS-65 @DIS-57
        Scenario Outline: View shipment's fulfillment details
            Given I have a shipment request persisted.
            When I retrieve the shipment list.
            Then I am able to see the requests.
            When I retrieve one shipment by shipment id.
            Then I am able to view the shipment fulfillment details.
            And The item attribute "Product Family" contains "PLASMA_TRANSFUSABLE".
            And The item attribute "Blood Type" contains "<Group Value>".
            And The item attribute "Product Quantity" contains "<Quantity>".
            And The item attribute "Shipment Id" is not empty.
            Examples:
                | Group Value | Quantity |
                | A           | 10       |
                | B           | 5        |
                | O           | 8        |


        Rule: I should be able to receive the shipment fulfillment request with short date product details.
        Rule: I should be able to persist with the short date shipment fulfilled request on the local store.
        @DIS-65
        Scenario: Receive shipment fulfillment request
            Given I have no shipment fulfillment requests.
            When I receive a shipment fulfillment request event.
            Then The shipment request will be available in the Distribution local data store and I can fill the shipment.


        Rule: I should be able to list the pending shipment fulfillment request.
        Rule: I should be able to view the shipment fulfillment details with short date products.
        @DIS-59
        Scenario Outline: View shipment's fulfillment details
            Given I have a shipment request persisted.
            When I retrieve the shipment list.
            Then I am able to see the requests.
            When I retrieve one shipment by shipment id.
            Then I am able to view the shipment fulfillment details.
            And The fulfillment request attribute "Order Number" is not empty.
            And The item attribute "Shipment Id" is not empty.
            And The item attribute "Product Family" contains "PLASMA_TRANSFUSABLE".
            And The item attribute "Blood Type" contains "<Group Value>".
            And The item attribute "Product Quantity" contains "<Quantity>".
            And The short date item attribute "Unit Number" contains "<Unit Number>".
            And The short date item attribute "Product Code" contains "<Product Code>".
            Examples:
                | Group Value | Quantity | Unit Number   | Product Code |
                | A           | 10       | W036810946300 | E086900      |
                | B           | 5        | W036810946301 | E070700      |


        Rule: I should be able to fill a product with any valid blood type when the line item product criteria has “ANY“ defined as blood type.
        @api @bug @DIS-273
        Scenario Outline: Fill Shipment with ANY as blood type.
            Given The shipment details are order Number "<Order Number>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>".
            And The visual inspection configuration is "enabled".
            And I have received a shipment fulfillment request with above details.
            When I fill a product with the unit number "<UN>", product code "<Code>".
            Then The product unit number "<UN>" and product code "<Code>" should be packed in the shipment.
            Examples:
                | Order Number | Customer ID | Customer Name    | Quantity | BloodType | ProductFamily                | UN            | Code     |
                | 1321          | 1           | Testing Customer | 10       | ANY       | PLASMA_TRANSFUSABLE          | W822530106093 | E7648V00 |
                | 1331          | 1           | Testing Customer | 5        | ANY       | RED_BLOOD_CELLS_LEUKOREDUCED | W822530106094 | E0685V00 |


        Rule: Distribution Technicians must be able to process and ship products for “DATE-TIME” delivery type orders.
        @api @bug @DIS-260
        Scenario Outline: Receive a shipment fulfillment request based on priority
            Given I have no shipment fulfillment requests.
            When I receive a shipment fulfillment request event for the order number "<Order Number>" and priority "<Priority>".
            Then The shipment request will be available in the Distribution local data store and I can fill the shipment.
            Examples:
                | Order Number | Priority  |
                | 1341         | DATE_TIME |
                | 1351         | ASAP      |
                | 1361         | ROUTINE   |
                | 1371         | STAT      |
                | 1381         | SCHEDULED |

        Rule: I should be able to fill orders with Whole Blood and Derived Products.
        @api @DIS-254
        Scenario Outline: Ship Whole Blood and Derived Products.
            Given The shipment details are order Number "<Order Number>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>".
            And The visual inspection configuration is "enabled".
            And I have received a shipment fulfillment request with above details.
            When I fill a product with the unit number "<UN>", product code "<Code>".
            Then The product unit number "<UN>" and product code "<Code>" should be packed in the shipment.
            Examples:
                | Order Number | Customer ID | Customer Name    | Quantity | BloodType | ProductFamily                | UN            | Code     |
                | 1391         | 1           | Testing Customer | 10       | ANY       | PLASMA_TRANSFUSABLE          | W822530106093 | E7648V00 |
                | 1392         | 1           | Testing Customer | 5        | ANY       | RED_BLOOD_CELLS_LEUKOREDUCED | W822530106094 | E0685V00 |
                | 1393         | 1           | Testing Customer | 5        | ABP       | WHOLE_BLOOD_LEUKOREDUCED     | W812530106095 | E0033V00 |
                | 1394         | 1           | Testing Customer | 5        | AP        | WHOLE_BLOOD                  | W812530106097 | E0023V00 |
                | 1395         | 1           | Testing Customer | 5        | ON        | RED_BLOOD_CELLS              | W812530106098 | E0167V00 |
