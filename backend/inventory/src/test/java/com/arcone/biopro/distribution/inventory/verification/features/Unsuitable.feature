@api @LAB-356 @AOA-XXX
Feature: Product Unsuitable Event - API
    As a lab specialist I want the system is able to update inventory status of multiple inventories after receiving a Shipment Completed event.

    @LAB-356
    Scenario: Update the status of an inventory after receiving a Product Unsuitable event.
        Given I have the following inventories:
            | Unit Number   | Product Code | Status    |
            | W036825043001 | E1624V00     | AVAILABLE |
        When I received a Product Unsuitable event with unit number "W036825043001", product code "E1624V00" and reason "POSITIVE_REACTIVE_TEST_RESULTS"
        Then the inventory statuses should be updated as follows:
            | Unit Number   | Product Code | Status     | Status reason                  |
            | W036825043001 | E1624V00     | UNSUITABLE | POSITIVE_REACTIVE_TEST_RESULTS |


    @LAB-292
    Scenario: Update the status of multiple inventories after receiving a Unit Unsuitable event.
        Given I have the following inventories:
            | Unit Number   | Product Code | Status    |
            | W036825043001 | E1624VA0     | AVAILABLE |
            | W036825043001 | E1624VB0     | AVAILABLE   |
        When I received a Unit Unsuitable event with unit number "W036825043001" and reason "POSITIVE_REACTIVE_TEST_RESULTS"
        Then the inventory statuses should be updated as follows:
            | Unit Number   | Product Code | Status     | Status reason                  |
            | W036825043001 | E1624VA0     | UNSUITABLE | POSITIVE_REACTIVE_TEST_RESULTS |
            | W036825043001 | E1624VB0     | UNSUITABLE | POSITIVE_REACTIVE_TEST_RESULTS |
