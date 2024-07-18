Feature: Validate order
    As a system, I want to validate the customer order
    so that I can save the order or decline the order based on the validation result.

#    TODO: Remember to add the product family to the structure. !!!

    Scenario Outline: Creating a BioPro order from a valid order inbound request

        Given I have received a valid order inbound request with externalId "<External ID>".
        When I search for a BioPro order with ID "<External ID>".
        Then I should be able to see the order status as "<Order Status>".
        And I should be able to see the location code as "<Location Code>".
        And I should see the shipment type as "<Shipment Type>".
        And I should see the delivery type as "<Delivery Type>".
        And I should see the shipping method as "<Shipping Method>".
        And I should see the product category as "<Product Category>".
        And I should see "<Shipping Customer Code>" as the shipping customer code.
        And I should see "<Billing Customer Code>" as the billing customer code.
        And I should have an "accepted-order" event sent to notify the new order.

        Examples:
            | External ID     | Order Status | Location Code          | Shipment Type | Delivery Type | Shipping Method | Product Category              | Shipping Customer Code | Billing Customer Code |
            | 114117922233598 | OPEN         | R08JACKSONVILLEMAINMOB | CUSTOMER      | SCHEDULED     | FEDEX           | ORDER_PRODUCT_CATEGORY_FROZEN | 99741                  | 99741                 |

    Scenario Outline: Creating a BioPro order from an invalid order inbound request

        Given I have received an invalid order inbound request with externalId "<External ID>".
        When The order is processed.
        Then I should have an "rejected-order" event sent to notify the rejected order.
        And I should see the rejection reason as "<Error Message>".

        Examples:
            | External ID | Error Message                                    |
            | 00000001    | Order Rejected - Wrong ABO                       |
            | 00000002    | Order Rejected - Shipping method does not exists |
            | 00000003    | Order Rejected - Delivery type does not exists   |
            | 00000004    | Order Rejected - Product category not allowed    |
            | 00000005    | Order Rejected - Customer not found              |
