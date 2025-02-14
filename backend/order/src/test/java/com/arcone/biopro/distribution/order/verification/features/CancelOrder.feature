@api @AOA-152
Feature: Cancel Order

    Background:
        Given I cleaned up from the database the orders with external ID starting with "EXTDIS315".

    Rule: The BioPro Order status must be updated to "Cancelled" when we receive a cancelled order through the order inbound interface.
    Rule: I should be able to cancel an open backorder when we receive a cancelled order through the order inbound interface.
    Rule: The cancellation reason, date, time, and the employee who cancelled the order must be displayed in the BioPro application for a cancelled order.
    Rule: The cancelled order request must be rejected if the internal BioPro order is in the "In Progress", "Completed" or "Cancelled" status.
    Rule: The cancelled order request must be rejected if the external order ID does not exist.
    @DIS-315
    Scenario Outline: Cancel an Open Biopro order from a valid Cancel Order request.
        Given I have an order with external ID "<Source External ID>", status "<Initial Status>" and backorder flag "<Backorder Flag>".
        And I have received a cancel order request with externalId "<Cancel External ID>" and content "cancel-order-valid-request.json".
        When The system processes the cancel order request.
        Then The Biopro Order must have status "<Final Status>".
        And I "<Should/Should not see Cancel Details>" be able to receive the cancel details.
        Examples:
            | Source External ID | Cancel External ID | Backorder Flag | Initial Status | Final Status | Should/Should not see Cancel Details |
            | EXTDIS3150001      | EXTDIS3150001      | false          | OPEN           | CANCELED     | should                               |
            | EXTDIS3150002      | EXTDIS3150002      | true           | OPEN           | CANCELED     | should                               |
            | EXTDIS3150003      | EXTDIS3150003      | false          | IN_PROGRESS    | IN_PROGRESS  | should not                           |
            | EXTDIS3150004      | EXTDIS3150004      | false          | COMPLETED      | COMPLETED    | should not                           |
            | EXTDIS3150005      | EXTDIS3150005      | false          | CANCELED       | CANCELED     | should not                           |
            | EXTDIS3150006      | EXTDIS315NULL      | false          | OPEN           | OPEN         | should not                           |
