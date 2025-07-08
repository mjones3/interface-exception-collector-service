@AOA-19
Feature: Fill Unlabeled Products for Internal Transfer order

    Background:
        Given I cleaned up from the database, all shipments with order number "452000016,452000017,452000018,452000019,452000020,452000021".

        Rule: I should be able to see all available products for a given unit number.
        Rule: I should be able to select unlabeled products to fill an internal transfer order.
        @api @DIS-452
        Scenario Outline: Product Selection Unlabeled Products.
            Given The shipment details are order Number "<Order Number>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>", Temperature Category as "<Temperature Category>", Shipment Type defined as "<Shipment Type>", Label Status as "<Label Status>" and Quarantined Products as "<Quarantined Products>".
            And I have received a shipment fulfillment request with above details.
            When I request all available products for the unit number "<UN>"
            Then I should receive the product list with the products "<product_list>" available for the unit number "<UN>".
            Examples:
                | Order Number | Customer ID | Customer Name     | Quantity | BloodType | ProductFamily                    | Temperature Category | Shipment Type     | Label Status | Quarantined Products  | UN            | product_list      |
                | 452000016    | DO1         | Distribution Only | 10       | ANY       | PLASMA_TRANSFUSABLE              |  FROZEN              | INTERNAL_TRANSFER  | UNLABELED   | false                 | W036898786756 | GENERIC1,GENERIC2 |
                | 452000017    | DO1         | Distribution Only | 5        | A         | APHERESIS_PLATELETS_LEUKOREDUCED |  ROOM_TEMPERATURE    | INTERNAL_TRANSFER  | UNLABELED   | true                  | W812530444002 | GENERIC1,GENERIC2 |


        Rule: I should not see the products that I have already selected.
        @api @DIS-452
        Scenario Outline: Product Selection Unlabeled Products Filtering out packed products.
            Given The shipment details are order Number "<Order Number>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>", Temperature Category as "<Category>", Shipment Type defined as "<Shipment Type>", Label Status as "<Label Status>" and Quarantined Products as "<Quarantined Products>".
            And The visual inspection configuration is "enabled".
            And I have received a shipment fulfillment request with above details.
            When I request all available products for the unit number "<UN>"
            Then I should receive the product list with the products "<product_list_1>" available for the unit number "<UN>".
            When I fill a product with the unit number "<UN>", product code "<Code_1>".
            Then The product unit number "<UN>" and product code "<Code_1>" should be packed in the shipment.
            When I request all available products for the unit number "<UN>"
            Then I should receive the product list with the products "<product_list_2>" available for the unit number "<UN>".
            Examples:
                | Order Number | Customer ID | Customer Name             | Quantity | BloodType | ProductFamily                    | UN            | Code_1   | Category         | Shipment Type     | Label Status | Quarantined Products | product_list_1            | product_list_2    |
                | 452000018    |DL1          | Distribution and Labeling | 10       | ANY       | RED_BLOOD_CELLS_LEUKOREDUCED     | W812530444001 | GENERIC1 | FROZEN           | INTERNAL_TRANSFER | LABELED      | false                |GENERIC1,GENERIC2,GENERIC3 | GENERIC1,GENERIC2 |
                | 452000019    |DO1          | Distribution Only         | 5        | AB        | PLASMA_TRANSFUSABLE              | W036898786758 | GENERIC1 | FROZEN           | INTERNAL_TRANSFER | LABELED      | true                 |GENERIC1,GENERIC2,GENERIC3 | GENERIC1,GENERIC2 |

        Rule: I should not be able to enter a unit number that doesn’t exist in the system.
        @api @DIS-452
        Scenario Outline: Product Selection Unlabeled Products unavailable products.
            Given The shipment details are order Number "<Order Number>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>", Temperature Category as "<Temperature Category>", Shipment Type defined as "<Shipment Type>", Label Status as "<Label Status>" and Quarantined Products as "<Quarantined Products>".
            And I have received a shipment fulfillment request with above details.
            When I request all available products for the unit number "<UN>"
            Then I should receive a "<Message Type>" message "<Message>".
            Examples:
                | Order Number | Customer ID | Customer Name     | Quantity | BloodType | ProductFamily                    | Temperature Category | Shipment Type     | Label Status | Quarantined Products  | UN            | Message                                                    | Message Type  |
                | 452000020    | DO1         | Distribution Only | 10       | ANY       | PLASMA_TRANSFUSABLE              |  FROZEN              | INTERNAL_TRANSFER  | UNLABELED   | false                 | W036898786756 | This product is not in the inventory and cannot be shipped | WARN          |
                | 452000021    | DO1         | Distribution Only | 5        | A         | APHERESIS_PLATELETS_LEUKOREDUCED |  ROOM_TEMPERATURE    | INTERNAL_TRANSFER  | UNLABELED   | true                  | W812530444002 | This product is not in the inventory and cannot be shipped | WARN          |



