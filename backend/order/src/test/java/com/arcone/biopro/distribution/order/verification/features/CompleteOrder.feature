@AOA-152
Feature: Back Orders



        Rule: I should be able to complete an order manually with partial order fulfillment
        Rule: I should be able to receive a success message when an order is completed.
        Rule: The system must publish an event when an order is completed.
        Rule: I should not be able to complete an order with pending shipments.
        @api @DIS-111
        Scenario Outline: Complete an order with partial fulfillment
            Given I have an order partially fulfilled with a shipment "<Shipment Status>".
            When I complete the order.
            Then The order status should be "<Order Status>".
            And I should receive a "<Message Type>" message "<Message>".
            And The system should publish an event when the order is completed.

            Examples:
                | Shipment Status | Order Status | Message Type | Message                      |
                | COMPLETED       | COMPLETED    | success      | Order completed successfully |
                | OPEN            | IN_PROGRESS  | warn         | Shipment is open             |


        Rule: I should not be able to complete an order with an open status (without starting the fulfillment process).
        Rule: I should not be able to complete an order already closed (completed).
        @api @DIS-111
        Scenario Outline: Try to complete an open/completed order
            Given I have an order with status "<Order Status>".
            When I try to complete the order.
            Then I should receive a "<Message Type>" message "<Message>".
            And The order status should be "<Order Status>".

            Examples:
                | Order Status | Message Type | Message                    |
                | OPEN         | warn         | Order is not fulfilled     |
                | COMPLETED    | warn         | Order is already completed |


        Rule: I should be able to complete an order manually with partial order fulfillment
        Rule: I should be able to receive a success message when an order is completed.
        Rule: I should be prompted to add a reason to complete an order that is partially fulfilled.
        @ui @DIS-111
        Scenario Outline: Complete an order with partial fulfillment
            Given I have an order partially fulfilled with a shipment "<Shipment Status>".
            When I navigate to the order details page.
            And I choose to complete the order.
            Then I should be prompted to add a reason.
            When I confirm to complete the order with the reason "<Reason>".
            Then I should receive a "<Message Type>" message "<Message>".

            Examples:
                | Shipment Status |  Message Type | Message                      |
                | COMPLETED       |  success      | Order completed successfully |

