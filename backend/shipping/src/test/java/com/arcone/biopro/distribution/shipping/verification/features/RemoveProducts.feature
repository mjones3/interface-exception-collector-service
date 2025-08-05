@AOA-152
Feature: Remove Product from a Shipment

    Background:
        Given I cleaned up from the database, all shipments with order number "3001,3002,3003,3004,3005".

        Rule: I should be able to remove one, multiple, or all products from the order until the product status has been moved to shipped status.
        Rule: The pending log should be updated if the product(s) is removed once it is filled.

        @api @DIS-3
        Scenario Outline: Remove Product from a Shipment.
            Given I have a shipment for order "<Order Number>" with the units "<Units>" and product codes "<Product Codes>" of family "<Product Family>" and blood type "<Blood Type>" "packed", out of <Quantity Requested> requested.
            And The second verification configuration is "disabled".
            When I remove the product "<Unit Removed>" with product code "<Code Removed>" from the line item "<Product Family>" "<Blood Type>".
            Then The product "<Unit Removed>" and "<Code Removed>" should not be part of the shipment.
            And I should receive a "success" message response "Product(s) successfully removed".
            And I should have <Expected Qty> items "packed".

            Examples:
                | Order Number | Product Codes              | Units                                     | Product Family               | Blood Type | Unit Removed  | Code Removed | Expected Qty | Quantity Requested |
                | 3001         | E4701V00,E4701V00,E4701V00 | W822530103001,W822530103002,W822530103003 | PLASMA_TRANSFUSABLE          | B          | W822530103001 | E4701V00     | 2            | 10                 |
                | 3001         | E0685V00,E0685V00,E0685V00 | W822530103001,W822530103002               | RED_BLOOD_CELLS_LEUKOREDUCED | ABP        | W822530103001 | E0685V00     | 1            | 8                  |
                | 3001         | E0033V00,E0033V00,E0033V00 | W822530103001,W822530103002,W822530103003 | WHOLE_BLOOD_LEUKOREDUCED     | ON         | W822530103001 | E0033V00     | 2            | 5                  |
                | 3001         | E0167V00,E0167V00,E0167V00 | W822530103001,W822530103002,W822530103003 | RED_BLOOD_CELLS              | AP         | W822530103001 | E0167V00     | 2            | 9                  |

        @api @DIS-3
        Rule: I should be required to restart the second verification process when products are removed.
        Scenario Outline: Restart verification status when a Product is removed from a Shipment.
            Given I have a shipment for order "<Order Number>" with the units "<Units>" and product codes "<Product Codes>" of family "<Product Family>" and blood type "<Blood Type>" "verified", out of <Quantity Requested> requested.
            And The second verification configuration is "enabled".
            When I remove the product "<Unit Removed>" with product code "<Code Removed>" from the line item "<Product Family>" "<Blood Type>".
            Then The product "<Unit Removed>" and "<Code Removed>" should not be part of the shipment.
            And I should receive a "success" message response "Product(s) successfully removed".
            And I should have 0 items "verified".
            Examples:

                | Order Number | Product Codes              | Units                                     | Product Family               | Blood Type | Unit Removed  | Code Removed | Quantity Requested |
                | 3002         | E4701V00,E4701V00,E4701V00 | W822530103004,W822530103005,W822530103006 | PLASMA_TRANSFUSABLE          | B          | W822530103006 | E4701V00     | 10                 |
                | 3002         | E0685V00,E0685V00,E0685V00 | W822530103004,W822530103005               | RED_BLOOD_CELLS_LEUKOREDUCED | ABP        | W822530103004 | E0685V00     | 8                  |
                | 3002         | E0033V00,E0033V00,E0033V00 | W822530103004,W822530103005,W822530103006 | WHOLE_BLOOD_LEUKOREDUCED     | OP         | W822530103005 | E0033V00     | 5                  |
                | 3002         | E0167V00,E0167V00,E0167V00 | W822530103004,W822530103005               | RED_BLOOD_CELLS              | ON         | W822530103005 | E0167V00     | 9                  |

        @api @DIS-3
        Rule: I should not be able to remove the product if the shipment is completed.
        Scenario Outline: Remove Product from a completed Shipment.
            Given I have a shipment for order "<Order Number>" with the unit "<UN>" and product code "<Code>" "verified" into the line item "<Product Family>" and Blood Type "<Blood Type>".
            And The second verification configuration is "enabled".
            And I have completed a shipment with above details.
            When I remove the product "<UN>" with product code "<Code>" from the line item "<Product Family>" "<Blood Type>".
            Then I should receive a "WARN" message response "Product cannot be removed because shipment is completed".
            Examples:
                | Order Number | Code     | UN            | Product Family               | Blood Type |
                | 3003         | E7646V00 | W036898786801 | PLASMA_TRANSFUSABLE          | AP         |
                | 3003         | E0685V00 | W822530106079 | RED_BLOOD_CELLS_LEUKOREDUCED | OP         |
                | 3003         | E0033V00 | W812530107865 | WHOLE_BLOOD_LEUKOREDUCED     | ABP        |
                | 3003         | E0167V00 | W812530106199 | RED_BLOOD_CELLS              | ON         |





    @ui @DIS-3
    Rule: I should be able to fill another product(s) in order to replace the ones I removed.
    Scenario Outline: Remove a product from a fully packed shipment, then add it back.
        Given I have a shipment for order "<Order Number>" with the units "<Units>" and product codes "<Product Codes>" of family "<Product Family>" and blood type "<Blood Type>" "packed", out of <Quantity Requested> requested.
        And The second verification configuration is "disabled".
        And The visual inspection configuration is "disabled".
        And I am on the fill product page of line item related to the "<Product Family>" "<Blood Type>".
        And I add the unit "<Unit Added>" with product code "<Code Added>".
        Then I should see a "Warning" message: "Quantity exceeded".
        When I close the acknowledgment message.
        And I select the product "<Unit Removed>" with product code "<Code Removed>".
        And I choose to remove products.
        Then I should see a "success" message: "Product(s) successfully removed".
        When I close the acknowledgment message.
        And I add the unit "<Unit Added>" with product code "<Code Added>".
        Then I should see the list of packed products added including "<Unit Added>" and "<Code Added>".

        Examples:
            | Order Number | Units                       | Product Codes     | Quantity Requested           | Blood Type | Product Family               | Unit Removed  | Code Removed | Unit Added       | Code Added |
            | 3005         | W822530103010,W822530103011 | E0685V00,E0685V00 | 2                            | AP         | PLASMA_TRANSFUSABLE          | W822530103010 | E0685V00     | =W03689878680100 | =<E7646V00 |
            | 3005         | W822530103010,W822530103011 | E0685V00,E0685V00 | 2                            | OP         | RED_BLOOD_CELLS_LEUKOREDUCED | W822530103010 | E0685V00     | =W82253010607900 | =<E0685V00 |
            | 3005         | W822530103010,W822530103011 | E0685V00,E0685V00 | 2                            | ABP        | WHOLE_BLOOD_LEUKOREDUCED     | W822530103010 | E0685V00     | =W81253010786500 | =<E0033V00 |
            | 3005         | W822530103010,W822530103011 | E0685V00,E0685V00 | 2                            | ON         | RED_BLOOD_CELLS              | W822530103010 | E0685V00     | =W81253010619900 | =<E0167V00 |

    @ui @DIS-3
    Scenario Outline: Remove Products from shipment - ui flow.
        Given I have a shipment for order "<Order Number>" with the units "<Units>" and product codes "<Product Codes>" of family "<Product Family>" and blood type "<Blood Type>" "packed", out of <Quantity Requested> requested.
        And I am on the fill product page of line item related to the "<Product Family>" "<Blood Type>".
        When I select the product "<Unit Removed>" with product code "<Code Removed>".
        And I choose to remove products.
        Then I should see a "success" message: "Product(s) successfully removed".
        And I should not see the unit "<Unit Removed>" with product code "<Code Removed>" added to the filled products table.

        Examples:
            | Order Number | Product Codes              | Units                                     | Product Family               | Blood Type | Unit Removed  | Code Removed | Quantity Requested |
            | 3004         | E4701V00,E4701V00,E4701V00 | W822530103007,W822530103008,W822530103009 | PLASMA_TRANSFUSABLE          | B          | W822530103008 | E4701V00     | 10                 |
            | 3004         | E0685V00,E0685V00,E0685V00 | W822530103007,W822530103008               | RED_BLOOD_CELLS_LEUKOREDUCED | ABP        | W822530103007 | E0685V00     | 8                  |
            | 3004         | E0033V00,E0033V00,E0033V00 | W822530103007,W822530103008,W822530103009 | WHOLE_BLOOD_LEUKOREDUCED     | OP         | W822530103009 | E0033V00     | 5                  |
            | 3004         | E0167V00,E0167V00,E0167V00 | W822530103007,W822530103008               | RED_BLOOD_CELLS              | ON         | W822530103007 | E0167V00     | 9                  |
