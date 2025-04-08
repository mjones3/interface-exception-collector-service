@api @AOA-6 @AOA-152
Feature: Shipment fulfillment request

    Background:
        Given I cleaned up from the database the packed item that used the unit number "W822530106093,W822530106094,W812530106095,W812530106097,W812530106098,W812530106199,W812530107006,W812530107007,,W812530107009,,W812530107010".
        And I cleaned up from the database, all shipments with order number "1321,1331,1341,1351,1361,1371,1381,1391,1392,1393,1394,1395,2851,2852,261002,336001,336002,336003,336004".

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
        Rule:
        @api @bug @DIS-260 @DIS-285
        Scenario Outline: Receive a shipment fulfillment request based on priority
            Given I have no shipment fulfillment requests.
            When I receive a shipment fulfillment request event for the order number "<Order Number>" and priority "<Priority>" and shipping date "<ShippingDate>".
            Then The shipment request will be available in the Distribution local data store and I can fill the shipment.
            Examples:
                | Order Number | Priority  | ShippingDate |
                | 1341         | DATE_TIME | 2025-12-31   |
                | 1351         | ASAP      | 2025-12-31   |
                | 1361         | ROUTINE   | 2025-12-31   |
                | 1371         | STAT      | 2025-12-31   |
                | 1381         | SCHEDULED | 2025-12-31   |
                | 2852         | ROUTINE   | NULL_VALUE   |

        Rule: I should be able to fill orders with Whole Blood and Derived Products.
        Rule: I should be able to fill orders with Apheresis Platelets (PRT and BacT) Products.
        @api @DIS-254 @DIS-336
        Scenario Outline: Ship Whole Blood and Derived Products.
            Given The shipment details are order Number "<Order Number>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>" , Temperature Category "<Category>".
            And The visual inspection configuration is "enabled".
            And I have received a shipment fulfillment request with above details.
            When I fill a product with the unit number "<UN>", product code "<Code>".
            Then The product unit number "<UN>" and product code "<Code>" should be packed in the shipment.
            Examples:
                | Order Number   | Customer ID | Customer Name    | Quantity | BloodType | ProductFamily                    | UN            | Code     | Category         |
                | 1391           | 1           | Testing Customer | 10       | ANY       | PLASMA_TRANSFUSABLE              | W822530106093 | E7648V00 | FROZEN           |
                | 1392           | 1           | Testing Customer | 5        | ANY       | RED_BLOOD_CELLS_LEUKOREDUCED     | W822530106094 | E0685V00 | FROZEN           |
                | 1393           | 1           | Testing Customer | 5        | ABP       | WHOLE_BLOOD_LEUKOREDUCED         | W812530106095 | E0033V00 | FROZEN           |
                | 1394           | 1           | Testing Customer | 5        | AP        | WHOLE_BLOOD                      | W812530107002 | E0023V00 | FROZEN           |
                | 1395           | 1           | Testing Customer | 5        | ON        | RED_BLOOD_CELLS                  | W812530106098 | E0167V00 | FROZEN           |
                | 336001         | 1           | Testing Customer | 10       | A         | APHERESIS_PLATELETS_LEUKOREDUCED | W812530107006 | EA007V00 | ROOM_TEMPERATURE |
                | 336002         | 1           | Testing Customer | 5        | AB        | APHERESIS_PLATELETS_LEUKOREDUCED | W812530107007 | EA139V00 | ROOM_TEMPERATURE |
                | 336003         | 1           | Testing Customer | 5        | AP        | PRT_APHERESIS_PLATELETS          | W812530107009 | E8340V00 | ROOM_TEMPERATURE |
                | 336004         | 1           | Testing Customer | 5        | BP        | PRT_APHERESIS_PLATELETS          | W812530107010 | E9431V00 | REFRIGERATED     |

            @api @DIS-261
            Rule: The second verification process should be restarted when a product is added into the shipment.
            Scenario Outline: Restart verification status when a Product is added into a Shipment.
                Given I have a shipment for order "<Order Number>" with the units "<Units>" and product codes "<Product Codes>" of family "<Product Family>" and blood type "<Blood Type>" "verified", out of <Quantity Requested> requested.
                And The second verification configuration is "enabled".
                When I fill a product with the unit number "<Unit Filled>", product code "<Code Filled>".
                Then The product unit number "<Unit Filled>" and product code "<Code Filled>" should be packed in the shipment.
                And I should have 0 items "verified" in the shipment.
                Examples:

                    | Order Number | Product Codes              | Units                                     | Product Family               | Blood Type | Unit Filled   | Code Filled  | Quantity Requested |
                    | 261002       | E4701V00,E4701V00,E4701V00 | W822530103004,W822530103005,W822530103006 | PLASMA_TRANSFUSABLE          | AP         | W822530106093 | E7648V00     | 10                 |
                    | 261002       | E0685V00,E0685V00,E0685V00 | W822530103004,W822530103005               | RED_BLOOD_CELLS_LEUKOREDUCED | OP         | W822530106094 | E0685V00     | 8                  |
                    | 261002       | E0033V00,E0033V00,E0033V00 | W822530103004,W822530103005,W822530103006 | WHOLE_BLOOD_LEUKOREDUCED     | ABP        | W812530106095 | E0033V00     | 5                  |
                    | 261002       | E0167V00,E0167V00,E0167V00 | W822530103004,W822530103005               | RED_BLOOD_CELLS              | ON         | W812530106098 | E0167V00     | 5                  |
                    | 261002       | E0167V00,E0167V00,E0167V00 | W822530103004,W822530103005               | WHOLE_BLOOD                  | AP         | W812530107002 | E0023V00     | 9                  |
