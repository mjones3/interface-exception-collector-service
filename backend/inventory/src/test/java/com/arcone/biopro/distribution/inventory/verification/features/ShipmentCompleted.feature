@api @LAB-160 @AOA-130
Feature: Shipment Completed Event - API
    As a lab specialist I want the system is able to update inventory status of multiple inventories after receiving a Shipment Completed event.

    Scenario: Update the inventory status of multiple inventories after receiving a Shipment Completed event.
        Given I have the following inventories:
            | Unit Number     | Product Code | Status     |
            | W036824111111   | E1624V00     | AVAILABLE  |
            | W036824111112   | E1624V00     | AVAILABLE  |
            | W036824111113   | E1624V00     | AVAILABLE  |
            | W036824111114   | E1624V00     | AVAILABLE  |
        When I received a Shipment Completed event for the following units:
            | Unit Number     | Product Code |
            | W036824111111   | E1624V00     |
            | W036824111112   | E1624V00     |
            | W036824111113   | E1624V00     |
        Then the inventory statuses should be updated as follows:
            | Unit Number     | Product Code | Status     |
            | W036824111111   | E1624V00     | SHIPPED    |
            | W036824111112   | E1624V00     | SHIPPED    |
            | W036824111113   | E1624V00     | SHIPPED    |
            | W036824111114   | E1624V00     | AVAILABLE  |


