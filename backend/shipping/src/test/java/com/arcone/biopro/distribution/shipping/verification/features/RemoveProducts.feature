@AOA-152
Feature: Remove Product from a Shipment
  As a Distribution Technician,
  I want to remove products from a shipment,
  so that I can ensure that only the correct and relevant items are shipped to the customer.


  Background:
    Given I cleaned up from the database the packed item that used the unit number "W036898786801,W822530103001,W822530103002,W822530103003,W822530103004,W822530103005,W822530103006,W822530103007".
    And I cleaned up from the database, all shipments with order number "3001,3002,3003,3004,3005".

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

        # add more product family
      Examples:
        | Order Number | Product Codes              | Units                                     | Product Family      | Blood Type | Unit Removed  | Code Removed | Expected Qty | Quantity Requested |
        | 3001         | E0685V00,E0685V00,E0685V00 | W822530103001,W822530103002,W822530103003 | PLASMA_TRANSFUSABLE | B          | W822530103001 | E0685V00     | 2            | 10                 |

    @api @DIS-3
    Rule: I should be required to restart the second verification process when products are removed.
    Scenario Outline: Remove verified Product from a Shipment.
      Given I have a shipment for order "<Order Number>" with the units "<Units>" and product codes "<Product Codes>" of family "<Product Family>" and blood type "<Blood Type>" "verified", out of <Quantity Requested> requested.
      And The second verification configuration is "enabled".
      When I remove the product "<Unit Removed>" with product code "<Code Removed>" from the line item "<Product Family>" "<Blood Type>".
      Then The product "<Unit Removed>" and "<Code Removed>" should not be part of the shipment.
      And I should receive a "success" message response "Product(s) successfully removed".
      And I should have 0 items "verified".
      Examples:
        | Order Number | Product Codes     | Units                       | Product Family      | Blood Type | Quantity Requested | Unit Removed  | Code Removed |
        | 3002         | E0685V00,E0685V00 | W822530103004,W822530103005 | PLASMA_TRANSFUSABLE | B          | 5                  | W822530103004 | E0685V00     |

    @api @DIS-3
    Rule: I should not be able to remove the product if the shipment is completed.
    Scenario Outline: Remove Product from a completed Shipment.
      Given I have a shipment for order "<Order Number>" with the unit "<UN>" and product code "<Code>" "verified" into the line item "<Product Family>" and Blood Type "<Blood Type>".
      And The second verification configuration is "enabled".
      And I have completed a shipment with above details.
      When I remove the product "<UN>" with product code "<Code>" from the line item "<Product Family>" "<Blood Type>".
      Then I should receive a "WARN" message response "Product cannot be removed because shipment is completed".
      Examples:
        | Order Number | Code     | UN            | Product Family      | Blood Type |
        | 3003         | E7646V00 | W036898786801 | PLASMA_TRANSFUSABLE | AP         |



    @ui @DIS-3 @disabled
    Scenario Outline: Remove Products from shipment - ui flow.
      Given I have a shipment for order "<Order Number>" with the units "<Units>" and product codes "<Product Codes>" of family "<Product Family>" and blood type "<Blood Type>" "packed", out of <Quantity Requested> requested.
      And I am on the fill product page of line item related to the "<Product Family>" "<Blood Type>".
      When I select the product "<Unit Removed>" with product code "<Code Removed>".
      And I choose to remove products.
      Then I should see a "success" message: "Product(s) successfully removed".
      And I should not see the unit "<Unit Removed>" with product code "<Code Removed>" added to the filled products table.

        #refactor table
      Examples:
        | Order Number | Units                       | Product Codes     | Quantity Requested | Blood Type | Product Family      | Unit Removed  | Code Removed |
        | 3004         | W822530103006,W822530103007 | E0685V00,E0685V00 | 10                 | B          | PLASMA_TRANSFUSABLE | W822530103007 | E0685V00     |

    @ui @DIS-3 @disabled
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

        #refactor table
      Examples:
        | Order Number | Units                       | Product Codes     | Quantity Requested | Blood Type | Product Family      | Unit Removed  | Code Removed | Unit Added       | Code Added |
        | 3005         | W822530103008,W822530103009 | E0685V00,E0685V00 | 2                  | AP         | PLASMA_TRANSFUSABLE | W822530103008 | E0685V00     | =W03689878680200 | =<E7648V00 |
