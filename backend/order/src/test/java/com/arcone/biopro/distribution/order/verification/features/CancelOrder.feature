@api @AOA-152
Feature: Cancel Order

    Background:
        Given I cleaned up from the database the orders with external ID starting with "EXTDIS315".

        Rule: The BioPro Order status must be updated to "Cancelled" when we receive a cancelled order through the order inbound interface.
        Rule: The cancellation reason, date, time, and the employee who cancelled the order must be displayed in the BioPro application for a cancelled order.
        Rule: The cancelled order request must be rejected if the internal BioPro order is in the "In Progress", "Completed" or "Cancelled" status.
        Rule: The cancelled order request must be rejected if the external order ID does not exist.
        @DIS-315
        Scenario Outline: Cancel an Open Biopro order from a valid Cancel Order request.
            Given I have an order with external ID "<Source External ID>" and status "<Initial Status>".
            And I have received a cancel order request with externalId "<Cancel External ID>", cancel date "<Cancel Date>" and content "cancel-order-valid-request.json".
            When The system processes the cancel order request.
            Then The Biopro Order must have status "<Final Status>".
            And I "<Should/Should not receive Cancel Details>" be able to receive the cancel details.
            Examples:
                | Source External ID | Cancel External ID | Initial Status | Final Status | Cancel Date         | Should/Should not receive Cancel Details |
                | EXTDIS3150001      | EXTDIS3150001      | OPEN           | CANCELLED    | 2025-01-01 11:09:55 | should                                   |
                | EXTDIS3150003      | EXTDIS3150003      | IN_PROGRESS    | IN_PROGRESS  | 2025-01-01 11:09:55 | should not                               |
                | EXTDIS3150004      | EXTDIS3150004      | COMPLETED      | COMPLETED    | 2025-01-01 11:09:55 | should not                               |
                | EXTDIS3150005      | EXTDIS3150005      | CANCELLED      | CANCELLED    | 2025-01-01 11:09:55 | should not                               |
                | EXTDIS3150006      | EXTDIS315NULL      | OPEN           | OPEN         | 2025-01-01 11:09:55 | should not                               |
                | EXTDIS3150007      | EXTDIS3150007      | OPEN           | OPEN         | 2050-01-01 11:09:55 | should not                               |
                | EXTDIS3150008      | EXTDIS3150008      | OPEN           | OPEN         | 9999-35-44 11:09:55 | should not                               |

        Rule: I should be able to cancel an open backorder when we receive a cancelled order through the order inbound interface.
        @DIS-315
        Scenario Outline: Cancel an Open Biopro backOrder from a valid Cancel Order request.
            Given I have an order with external ID "<Source External ID>" partially fulfilled with a shipment "<Shipment Status>".
            And I have Shipped "<Shipped Quantity>" products of each item line.
            And I have the back order configuration set to "true".
            And I request to complete the order.
            And I have received a cancel order request with externalId "<Cancel External ID>", cancel date "<Cancel Date>" and content "cancel-order-valid-request.json".
            When The system processes the cancel order request.
            And I search for orders by "<Search Key>".
            Then I should receive the search results containing "<Expected Quantity>" orders with statuses "<Expected Statuses>".

            Examples:
                | Shipment Status | Shipped Quantity | Search Key | Source External ID | Cancel External ID | Cancel Date         | Expected Quantity | Expected Statuses    |
                | COMPLETED       | 2                | externalId | EXTDIS3150009      | EXTDIS3150009      | 2025-01-01 11:09:55 | 2                 | COMPLETED, CANCELLED |

        Scenario: Database clean up
            Given I cleaned up from the database the orders with external ID starting with "EXTDIS315".
