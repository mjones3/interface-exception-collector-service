@api @LAB-160 @AOA-130
Feature: Shipment Completed Event - API
    As a lab specialist I want the system is able to update inventory status of multiple inventories after receiving a Shipment Completed event.

    Scenario: Update the inventory status of multiple inventories after receiving a Shipment Completed event for customer shipment type.
        Given I have the following inventories:
            | Unit Number   | Product Code | Status    |
            | W036824111111 | E1624V00     | AVAILABLE |
            | W036824111112 | E1624V00     | AVAILABLE |
            | W036824111113 | E1624V00     | AVAILABLE |
            | W036824111114 | E1624V00     | AVAILABLE |
        When I received a Shipment Completed event with shipment type "CUSTOMER" for the following units:
            | Unit Number   | Product Code |
            | W036824111111 | E1624V00     |
            | W036824111112 | E1624V00     |
            | W036824111113 | E1624V00     |
        Then the inventory statuses should be updated as follows:
            | Unit Number   | Product Code | Status    |
            | W036824111111 | E1624V00     | SHIPPED   |
            | W036824111112 | E1624V00     | SHIPPED   |
            | W036824111113 | E1624V00     | SHIPPED   |
            | W036824111114 | E1624V00     | AVAILABLE |

    Scenario: Update the inventory status of multiple inventories after receiving a Shipment Completed event for internal transfer shipment type.
        Given I have the following inventories:
            | Unit Number   | Product Code | Status    |
            | W036824111115 | WHOLEBLOOD   | AVAILABLE |
            | W036824111116 | RBC          | AVAILABLE |
            | W036824111117 | PLASMA       | AVAILABLE |
            | W036824111118 | PLASAPHP     | AVAILABLE |
        When I received a Shipment Completed event with shipment type "INTERNAL_TRANSFER" for the following units:
            | Unit Number   | Product Code |
            | W036824111115 | WHOLEBLOOD   |
            | W036824111116 | RBC          |
            | W036824111117 | PLASMA       |
        Then the inventory statuses should be updated as follows:
            | Unit Number   | Product Code | Status     |
            | W036824111115 | WHOLEBLOOD   | IN_TRANSIT |
            | W036824111116 | RBC          | IN_TRANSIT |
            | W036824111117 | PLASMA       | IN_TRANSIT |
            | W036824111118 | PLASAPHP     | AVAILABLE  |

