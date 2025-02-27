@api @AOA-106
Feature: Unsuitable Event - API
    As an inventory service I want to update the status of an inventory after receiving an Unsuitable event.

    @LAB-356
    Scenario: Update the status of an inventory after receiving a Product Unsuitable event.
        Given I have the following inventories:
            | Unit Number   | Product Code | Status    |
            | W036825043001 | E4689V00     | AVAILABLE |
            | W036825043001 | E4693V00     | AVAILABLE |
        When I received a Product Unsuitable event with unit number "W036825043001", product code "E468900" and reason "POSITIVE_REACTIVE_TEST_RESULTS"
        Then the inventory statuses should be updated as follows:
            | Unit Number   | Product Code | Status     | Status reason                  |
            | W036825043001 | E4689V00     | UNSUITABLE | POSITIVE_REACTIVE_TEST_RESULTS |
            | W036825043001 | E4693V00     | AVAILABLE  | POSITIVE_REACTIVE_TEST_RESULTS |

    @LAB-292
    Scenario: Update the status of multiple inventories with different product code but same division code after receiving a Unit Unsuitable event.
        Given I have the following inventories:
            | Unit Number   | Product Code | Status    |
            | W036825043001 | E4689V00     | AVAILABLE |
            | W036825043001 | E4693V00     | AVAILABLE |
        When I received a Unit Unsuitable event with unit number "W036825043001" and reason "POSITIVE_REACTIVE_TEST_RESULTS"
        Then the inventory statuses should be updated as follows:
            | Unit Number   | Product Code | Status     | Status reason                  |
            | W036825043001 | E4689V00     | UNSUITABLE | POSITIVE_REACTIVE_TEST_RESULTS |
            | W036825043001 | E4693V00     | UNSUITABLE | POSITIVE_REACTIVE_TEST_RESULTS |

    @LAB-292
    Scenario: Update the status of multiple inventories with 1 generic and 1 final without sixth digit after receiving a Unit Unsuitable event.
        Given I have the following inventories:
            | Unit Number   | Product Code | Status    |
            | W036825043001 | RBC          | AVAILABLE |
            | W036825043001 | E070100      | AVAILABLE |
        When I received a Unit Unsuitable event with unit number "W036825043001" and reason "POSITIVE_REACTIVE_TEST_RESULTS"
        Then the inventory statuses should be updated as follows:
            | Unit Number   | Product Code | Status     | Status reason                  |
            | W036825043001 | RBC          | UNSUITABLE | POSITIVE_REACTIVE_TEST_RESULTS |
            | W036825043001 | E070100      | UNSUITABLE | POSITIVE_REACTIVE_TEST_RESULTS |

    @LAB-292
    Scenario: Update the status of multiple inventories with same product code but different division code after receiving a Unit Unsuitable event.
        Given I have the following inventories:
            | Unit Number   | Product Code | Status    |
            | W036825043001 | E1624VA0     | AVAILABLE |
            | W036825043001 | E1624VB0     | AVAILABLE |
        When I received a Unit Unsuitable event with unit number "W036825043001" and reason "POSITIVE_REACTIVE_TEST_RESULTS"
        Then the inventory statuses should be updated as follows:
            | Unit Number   | Product Code | Status     | Status reason                  |
            | W036825043001 | E1624VA0     | UNSUITABLE | POSITIVE_REACTIVE_TEST_RESULTS |
            | W036825043001 | E1624VB0     | UNSUITABLE | POSITIVE_REACTIVE_TEST_RESULTS |
