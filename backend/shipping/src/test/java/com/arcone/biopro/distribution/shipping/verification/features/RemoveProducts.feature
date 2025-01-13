@AOA-152
Feature: Remove Product from a Shipment
    As a Distribution Technician,
    I want to remove products from a shipment,
    so that I can ensure that only the correct and relevant items are shipped to the customer.


    Background:
        Given I cleaned up from the database the packed item that used the unit number "W822530103001,W822530103002,W822530103003".
        And I cleaned up from the database, all shipments with order number "3001,3002,3003".

    Rule: I should be able to remove one, multiple, or all products from the order until the product status has been moved to shipped status.
    #Rule: The removal of products should automatically update the order and inventory system.

    @api @DIS-3
    Scenario Outline: Remove Product from a Shipment.
        Given I have a shipment for order "<Order Number>" with the unit "<UN>" and product code "<Code>" "packed" into the line item "<Product Family>" and Blood Type "<Blood Type>".
        And The second verification configuration is "disabled".
        When I remove the unit "<UN>" with product code "<Code>" from the line item "<Product Family>" "<Blood Type>".
        Then The unit <"UN"> should not be part of the shipment.
        And I should receive a "success" message "Product(s) successfully removed".
        Examples:
            | Order Number | Code     | UN            |Product Family     | Blood Type |
            | 3001         | E0685V00 | W822530103001 |PLASMA_TRANSFUSABLE| B          |

    @api @DIS-3
    Rule: I should be required to restart the second verification process when products are removed.
    Scenario Outline: Remove verified Product from a Shipment.
        Given I have a shipment for order "<Order Number>" with the unit "<UN>" and product code "<Code>" "verified" into the line item "<Product Family>" and Blood Type "<Blood Type>".
        And The second verification configuration is "enabled".
        When I remove the unit "<UN>" with product code "<Code>" from the line item "<Product Family>" "<Blood Type>".
        Then The unit <"UN"> should not be part of the shipment.
        And I should receive a "success" message "Product(s) successfully removed".
        And I should have 0 items "verified".
        Examples:
            | Order Number | Code     | UN            |Product Family     | Blood Type |
            | 3002         | E0685V00 | W822530103002 |PLASMA_TRANSFUSABLE| B          |

    @api @DIS-3
    Rule: I should not be able to remove the product if the order is closed or shipped.
    Scenario Outline: Remove Product from a completed Shipment.
        Given I have a shipment for order "<Order Number>" with the unit "<UN>" and product code "<Code>" "verified" into the line item "<Product Family>" and Blood Type "<Blood Type>".
        And The shipment is "COMPLETED"
        And The second verification configuration is "enabled".
        When I remove the unit "<UN>" with product code "<Code>" from the line item "<Product Family>" "<Blood Type>".
        Then I should receive a "WARN" message "TO BE DEFINED.".
        Examples:
            | Order Number | Code     | UN            |Product Family     | Blood Type |
            | 3003         | E0685V00 | W822530103003 |PLASMA_TRANSFUSABLE| B          |
