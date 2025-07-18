@AOA-19
Feature: Fill Unlabeled Products for Internal Transfer order

    Background:
        Given I cleaned up from the database, all shipments with order number "452000016,452000017,452000018,452000019,452000020,452000021,452000022,452000023,452000024".

        Rule: I should be able to see all eligible products for a given unit number.
        Rule: I should be able to select unlabeled products to fill an internal transfer order.
        @api @DIS-452
        Scenario Outline: Product Selection Unlabeled Products.
            Given The shipment details are order Number "<Order Number>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>", Temperature Category as "<Temperature Category>", Shipment Type defined as "<Shipment Type>", Label Status as "<Label Status>" and Quarantined Products as "<Quarantined Products>".
            And I have received a shipment fulfillment request with above details.
            When I request all unlabeled products for the unit number "<UN>" in the line item 1.
            Then I should receive the product list with the products "<product_list>" available for the unit number "<UN>".
            Examples:
                | Order Number | Customer ID | Customer Name     | Quantity | BloodType | ProductFamily                | Temperature Category | Shipment Type     | Label Status | Quarantined Products | UN            | product_list  |
                | 452000016    | DO1         | Distribution Only | 10       | ANY       | RED_BLOOD_CELLS_LEUKOREDUCED | FROZEN               | INTERNAL_TRANSFER | UNLABELED    | false                | W036825185915 | LR_RBC,LR_RBB |
                | 452000017    | DO1         | Distribution Only | 5        | ANY       | PLASMA_TRANSFUSABLE          | FROZEN               | INTERNAL_TRANSFER | UNLABELED    | true                 | W036825158907 | BAG-A,BAG-B   |


        Rule: I should not be able to select products that have already been selected.
        @api @DIS-452
        Scenario Outline: Product Selection Unlabeled Products Filtering out packed products.
            Given The shipment details are order Number "<Order Number>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>", Temperature Category as "<Category>", Shipment Type defined as "<Shipment Type>", Label Status as "<Label Status>" and Quarantined Products as "<Quarantined Products>".
            And The visual inspection configuration is "enabled".
            And I have received a shipment fulfillment request with above details.
            When I request all unlabeled products for the unit number "<UN>" in the line item 1.
            Then I should receive the product list with the products "<product_list_1>" available for the unit number "<UN>".
            When I fill a product with the unit number "<UN>", product code "<Code_1>".
            Then The product unit number "<UN>" and product code "<Code_1>" should be packed in the shipment.
            When I request all unlabeled products for the unit number "<UN>" in the line item 1.
            Then I should receive the product list with the products "<product_list_2>" available for the unit number "<UN>".
            Examples:
                | Order Number | Customer ID | Customer Name             | Quantity | BloodType | ProductFamily                    | UN            | Code_1 | Category | Shipment Type     | Label Status | Quarantined Products | product_list_1 | product_list_2 |
                | 452000018    |DL1          | Distribution and Labeling | 10       | ANY       | RED_BLOOD_CELLS_LEUKOREDUCED     | W036825185915 | LR_RBC | FROZEN   | INTERNAL_TRANSFER | UNLABELED    | false                | LR_RBC,LR_RBB  | LR_RBB         |
                | 452000019    |DO1          | Distribution Only         | 5        | ANY       | PLASMA_TRANSFUSABLE              | W036825158907 | BAG-A  | FROZEN   | INTERNAL_TRANSFER | UNLABELED    | true                 | BAG-A,BAG-B    | BAG-B          |



        Rule: I should not be able to select a product if the temperature category does not match the order criteria.
        Rule: I should not be able to select a product if the family does not match the order criteria.
        Rule: I should not be able to select a product if the blood type does not match the order criteria.
        Rule: I should not be able to select a product that is modified.
        Rule: I should not be able to select a product that is discarded.
        Rule: I should not be able to select a product that is converted.
        Rule: I should not be able to select a product that is unsuitable.
        Rule: I should not be able to select a product that is labeled.
        Rule: I should not be able to select a product that is shipped.
        Rule: I should be notified when I select an expired product.
        Rule: I should not be able to enter a unit number if it is part of another order or shipment.
        Rule: I should be able to fill an internal transfer order with quarantined products as requested.
        Rule: I should not be able to fill the same internal order with both quarantined and quarantined products.

        @api @DIS-452
        Scenario Outline: Product Selection Unlabeled Products unavailable products.
            Given The shipment details are order Number "<Order Number>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>", Temperature Category as "<Temperature Category>", Shipment Type defined as "<Shipment Type>", Label Status as "<Label Status>" and Quarantined Products as "<Quarantined Products>".
            And I have received a shipment fulfillment request with above details.
            When I request all unlabeled products for the unit number "<UN>" in the line item 1.
            Then I should receive a "<Message Type>" message response "<Message>".
            Examples:
                | Order Number | Customer ID | Customer Name     | Quantity | BloodType | ProductFamily                    | Temperature Category | Shipment Type     | Label Status | Quarantined Products | UN            | Message                                                             | Message Type |
                | 452000020    | DO1         | Distribution Only | 10       | ANY       | PLASMA_TRANSFUSABLE              | FROZEN               | INTERNAL_TRANSFER | UNLABELED    | false                | W036898781111 | This product is not in the inventory and cannot be shipped          | WARN         |
                | 452000022    | DO1         | Distribution Only | 5        | AP        | PLASMA_TRANSFUSABLE              | FROZEN               | INTERNAL_TRANSFER | UNLABELED    | true                 | W036825158913 | This product is labeled and cannot be used for unlabeled shipments. | WARN         |
                | 452000023    | DO1         | Distribution Only | 5        | A         | APHERESIS_PLATELETS_LEUKOREDUCED | ROOM_TEMPERATURE     | INTERNAL_TRANSFER | UNLABELED    | false                | W036825158914 | This product is quarantined and cannot be shipped                   | INFO         |
                | 452000024    | DO1         | Distribution Only | 5        | A         | APHERESIS_PLATELETS_LEUKOREDUCED | ROOM_TEMPERATURE     | INTERNAL_TRANSFER | UNLABELED    | false                | W036825158915 | This product is not in the inventory and cannot be shipped          | INFO         |







