@AOA-152
Feature: Remove Product from a Shipment
    As a Distribution Technician,
    I want to remove products from a shipment,
    so that I can ensure that only the correct and relevant items are shipped to the customer.


    Background:
        Given I cleaned up from the database the packed item that used the unit number "W822530103001,W822530103002,W822530103003".
        And I cleaned up from the database, all shipments with order number "3001,3002,3003".

    Rule: I should be able to remove one, multiple, or all products from the order until the product status has been moved to shipped status.
    Rule: The pending log should be updated if the product(s) is removed once it is filled.

    @api @DIS-3
    Scenario Outline: Remove Product from a Shipment.
        Given I have a shipment for order "<Order Number>" requesting "<Qty>" products of "<Product Family>" and Blood Type "<Blood Type>".
        And I have "packed" the product "<UN>" and the product code "<Code>" into the line item "<Product Family>" "<Blood Type>".
        And The second verification configuration is "disabled".
        And I should have <Packed Product> of <Qty> items "packed".
        When I remove the product "<UN>" with product code "<Code>" from the line item "<Product Family>" "<Blood Type>".
        Then The product <"UN">  and <"Code"> should not be part of the shipment.
        And I should receive a "success" message "Product(s) successfully removed".
        And I should have <Expected Qty> items "packed".

        # add more product family
        Examples:
            | Order Number | Code     | UN            |Product Family     | Blood Type | Qty | Packed Product | Expected Qty |
            | 3001         | E0685V00 | W822530103001 |PLASMA_TRANSFUSABLE| B          | 10  |  1             | 0            |

    @api @DIS-3
    Rule: I should be required to restart the second verification process when products are removed.
    Scenario Outline: Remove verified Product from a Shipment.
        Given I have a shipment for order "<Order Number>" with the product "<UN>" and product code "<Code>" "verified" into the line item "<Product Family>" and Blood Type "<Blood Type>".
        And The second verification configuration is "enabled".
        When I remove the product "<UN>" with product code "<Code>" from the line item "<Product Family>" "<Blood Type>".
        Then The product <"UN"> and product code <"Code"> should not be part of the shipment.
        And I should receive a "success" message "Product(s) successfully removed".
        And I should have 0 items "verified".
        Examples:
            | Order Number | Code     | UN            |Product Family     | Blood Type |
            | 3002         | E0685V00 | W822530103002 |PLASMA_TRANSFUSABLE| B          |

    @api @DIS-3
    Rule: I should not be able to remove the product if the shipment is completed.
    Scenario Outline: Remove Product from a completed Shipment.
        Given I have a shipment for order "<Order Number>" with the unit "<UN>" and product code "<Code>" "verified" into the line item "<Product Family>" and Blood Type "<Blood Type>".
        And The second verification configuration is "enabled".
        And The shipment is "COMPLETED"
        When I remove the unit "<UN>" with product code "<Code>" from the line item "<Product Family>" "<Blood Type>".
        Then I should receive a "WARN" message "Product cannot be removed from a completed shipment.".
        Examples:
            | Order Number | Code     | UN            |Product Family     | Blood Type |
            | 3003         | E0685V00 | W822530103003 |PLASMA_TRANSFUSABLE| B          |



    @ui @DIS-3
        Scenario Outline: Remove Products from shipment - ui flow.
        Given I have a shipment for order "<Order Number>" requesting "<Qty>" products of "<Product Family>" and Blood Type "<Blood Type>".
            And I have "packed" the product "<UN>" and the product code "<Code>" into the line item "<Product Family>" "<Blood Type>".
            And I am on the fill product page of line item related to the "<Product Family>" "<Blood Type>".
            When I select the product "<UN>" with product code "<Code>".
            And The Submit option should be "disabled".
            And I choose remove products.
            Then I should receive a "success" message "Product(s) successfully removed".
            And I should not see the unit "<UN>" with product code "<Code>" added to the filled products table.

        #refactor table
            Examples:
                | Order Number | Customer ID | Customer Name    | Quantity | BloodType | ProductFamily                                                  | Family                       | Type | UN               | Code       |
                | 108          | 1           | Testing Customer | 10,5,8   | ANY,B,O   | PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE    | PLASMA TRANSFUSABLE          | ANY  | =W03689878680200 | =<E7648V00 |
