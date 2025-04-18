@AOA-152
Feature: Complete Order

    Background:
        Given I cleaned up from the database the orders with external ID starting with "EXTDIS111".

        Rule: I should be able to complete an order manually with partial order fulfillment
        Rule: I should be able to receive a success message when an order is completed.
        @api @DIS-111
        Scenario Outline: Complete an order with partial fulfillment
            Given I have an order with external ID "EXTDIS11101" partially fulfilled with a shipment "<Shipment Status>".
            When I request to complete the order.
            Then The order status should be "<Order Status>".
            And I should receive a "<Message Type>" message response "<Message>".

            Examples:
                | Shipment Status | Order Status | Message Type | Message                      |
                | COMPLETED       | COMPLETED    | success      | Order completed successfully |



        Rule: I should not be able to complete an order with pending shipments.
        @api @DIS-111
        Scenario Outline: Try to complete an order with an open shipment
            Given I have an order with external ID "EXTDIS11102" partially fulfilled with a shipment "<Shipment Status>".
            When I request to complete the order.
            Then The order status should be "<Order Status>".
            And I should receive a "<Message Type>" message response "<Message>".

            Examples:
                | Shipment Status | Order Status | Message Type | Message                    |
                | OPEN            | IN_PROGRESS  | error        | Order has an open shipment |


        Rule: I should not be able to complete an order with an open status (without starting the fulfillment process).
        Rule: I should not be able to complete an order already closed (completed).
        @api @DIS-111
        Scenario Outline: Try to complete an open/completed order
            Given I have an order with external ID "EXTDIS11103" and status "<Order Status>".
            When I request to complete the order.
            Then I should receive a "<Message Type>" message response "<Message>".
            And The order status should be "<Order Status>".

            Examples:
                | Order Status | Message Type | Message                                          |
                | OPEN         | error        | Order is not in-progress and cannot be completed |
                | COMPLETED    | error        | Order is already completed                       |


        Rule: I should be able to complete an order manually with partial order fulfillment
        Rule: I should be prompted to confirm before completing an order.
        Rule: I should have an option to enter the reason for completing a partially fulfilled order.
        Rule: The system must not create a backorder if the user doesn’t confirm the action to create a backorder.
        Rule: The system must create a backorder if the user confirms the creation of a backorder.
        @ui @DIS-111 @DIS-175
        Scenario Outline: Complete an order with partial fulfillment
            Given I have an order with external ID "EXTDIS11104" partially fulfilled with a shipment "<Shipment Status>".
            When I navigate to the order details page.
            And I choose to complete the order.
            Then I should be prompted to confirm to complete the order.
            And I define the backorder creation option as "<Create Backorder>".
            When I confirm to complete the order with the reason "<Reason>".
            Then I should see a "<Message Type>" message: "<Message>".
            And A back order "<Create?>" be created with the same external ID and status "OPEN".

            Examples:
                | Shipment Status | Message Type | Message                      | Reason   | Create Backorder | Create?    |
                | COMPLETED       | success      | Order completed successfully | Comments | true             | should     |
                | COMPLETED       | success      | Order completed successfully | Comments | false            | should not |


        Rule: The system must not create a backorder if the user doesn’t confirm the action to create a backorder.
        Rule: The system must create a backorder if the user confirms the creation of a backorder.
        Rule: The system must include the details of the unfulfilled products from the original order.
        Rule: The status of the backorder must be assigned as “Open”.
        Rule: The backorder must be visible in the system.
        Rule: The backorder must contain the same external ID as the original order.
        @api @DIS-175
        Scenario Outline: Complete an order with back order configuration <Back Order Config>
            Given I have an order with external ID "EXTDIS11105" partially fulfilled with a shipment "<Shipment Status>".
            And I have Shipped "<Shipped Quantity>" products of each item line.
            And I have the back order configuration set to "<Back Order Config>".
            When I request the order details.
            Then I "<Option>" have an option to create a back order.
            When I request to complete the order.
            Then The order status should be "COMPLETED".
            And I "<Option>" have <Remaining Quantity> remaining products as part of the back order created.

            Examples:
                | Shipment Status | Back Order Config | Option     | Shipped Quantity | Remaining Quantity |
                | COMPLETED       | false             | should not | 2                | 8                  |
                | COMPLETED       | true              | should     | 3                | 7                  |

            Scenario: Database cleanup
                Given I cleaned up from the database the orders with external ID starting with "EXTDIS111".

