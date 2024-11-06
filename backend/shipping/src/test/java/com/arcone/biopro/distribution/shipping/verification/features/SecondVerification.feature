Feature: Second Verification of Units Feature
    As a distribution technician,
    I want to perform a second verification of the products in a shipment (if configured),
    So that I can ensure that the products recorded in the system match the physical products inside the shipping box.

    Background:
        Given I cleaned up from the database the packed item that used the unit number "W822530106087,W822530106089,W822530106088,W822530106090,W822530106091,W822530106092".
        And I cleaned up from the database, all shipments with order number "118,119,120,121".


        Rule: I should be able to verify each unit that I have packed in the shipment.
        Rule: I should be able to see the progress of units that are being verified.
        Rule: I should be able to see all the verified units.
        Rule: I should see the complete shipment option available once all the units are verified.
        Rule: I should be able to see the shipping information.
        Rule: I should be able to see the order information.
        @ui @DST-203
        Scenario Outline: Second verification packed units.
            Given I have a shipment for order "<Order Number>" with the unit "<UN>" and product code "<Code>" packed.
            And The second verification configuration is "enabled".
            And I am on the Shipment Fulfillment Details page for order <Order Number>.
            When I choose verify products.
            Then I should be redirected to the verify products page.
            And I can see the Order Information Details and the Shipping Information Details.
            When I scan the unit "<UN>" with product code "<Code>".
            Then I should see the unit added to the verified products table.
            And I should see the log of verified products being updated.
            And The complete shipment option should be enabled.
           Examples:
               | Order Number | Code     | UN            |
               | 118          | E0685V00 | W822530106087 |


        Rule: I should be notified when I scan a unit that is not part of the shipment.
        Rule: I should not be able to complete the shipment if all the units are not verified.
        @ui @DST-203
        Scenario Outline: Second verification units not packed.
            Given I have a shipment for order "<Order Number>" with the unit "<UN>" and product code "<Code>" packed.
            And The second verification configuration is "enabled".
            And I am on the Shipment Fulfillment Details page for order <Order Number>.
            When I choose verify products.
            Then I should be redirected to the verify products page.
            And I can see the Order Information Details and the Shipping Information Details.
            When I scan the unit "<Not Packed Unit>" with product code "<Not Packed Code>".
            Then I should see a "Warning" message: "The verification does not match all products in this order. Please re-scan all the products.".
            And  I should not see the unit added to the verified products table.
            And The complete shipment option should not be enabled.
            Examples:
                | Order Number | Code     | UN            | Not Packed Unit | Not Packed Code |
                | 119          | E0685V00 | W822530106088 | W822530106089   | E0685V00        |



        Rule: I should be notified when I scan a unit that is already verified.
        Rule: I should not be able to complete the shipment if all the units are not verified.
        @ui @DST-203
        Scenario Outline: Second verification units already packed.
            Given I have a shipment for order "<Order Number>" with the units "<UN1>,<UN2>"  and product codes "<Code1>,<Code2>" packed.
            And The second verification configuration is "enabled".
            And I am on the Shipment Fulfillment Details page for order <Order Number>.
            When I choose verify products.
            Then I should be redirected to the verify products page.
            When I scan the unit "<UN1>" with product code "<Code1>".
            Then I should see the unit added to the verified products table.
            And I should see the log of verified products being updated.
            When I scan the unit "<UN1>" with product code "<Code1>".
            Then I should see a "Warning" message: "This product has already been verified. Please re-scan all the products in the order.".
            And  I should not see the unit added to the verified products table.
            And The complete shipment option should not be enabled.
            Examples:
                | Order Number | Code1     | UN1          | Code2     | UN2           |
                | 120          | E0685V00 | W822530106090 | E0685V00  | W822530106091 |


        Rule: I should be able to complete the shipment once all filled products have been verified.
        Rule: I should see the progress bar reflecting that the total of products were verified.
        Rule: I should see the status of the shipment updated to “Completed”.
        Rule: I should see a success message indicating the shipment has been successfully completed.
        Rule: I should be able to verify the products' eligibility before completing the shipment.
        @ui @DST-204
        Scenario Outline: Complete shipment Second verification suitable products.
            Given I have a shipment for order "<Order Number>" with the unit "<UN>" and product code "<Code>" verified.
            And The second verification configuration is "enabled".
            And I am on the verify products page.
            Then I should see the unit added to the verified products table.
            And The complete shipment option should be enabled.
            When I choose to complete the Shipment.
            Then I should be redirected to the shipment details page.
            And I should see a "Success" message: "Shipment Completed".
            And I should see the status of the shipment as "Completed"

            Examples:
                | Order Number | Code     | UN            |
                | 121          | E0685V00 | W822530106092 |
