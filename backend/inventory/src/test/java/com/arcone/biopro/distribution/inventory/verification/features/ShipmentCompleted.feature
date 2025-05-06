# Feature Unit Number reference: W036825012000
@api @LAB-160 @AOA-130
Feature: Shipment Completed Event - API
    As a lab specialist I want the system is able to update inventory status of multiple inventories after receiving a Shipment Completed event.

    Scenario: Update the inventory status of multiple inventories after receiving a Shipment Completed event for customer shipment type.
        Given I have the following inventories:
            | Unit Number   | Product Code | Status    |
            | W036825012001 | E1624V00     | AVAILABLE |
            | W036825012002 | E1624V00     | AVAILABLE |
            | W036825012003 | E1624V00     | AVAILABLE |
            | W036825012004 | E1624V00     | AVAILABLE |
        When I received a Shipment Completed event with shipment type "CUSTOMER" for the following units:
            | Unit Number   | Product Code |
            | W036825012001 | E1624V00     |
            | W036825012002 | E1624V00     |
            | W036825012003 | E1624V00     |
        Then the inventory statuses should be updated as follows:
            | Unit Number   | Product Code | Status    |
            | W036825012001 | E1624V00     | SHIPPED   |
            | W036825012002 | E1624V00     | SHIPPED   |
            | W036825012003 | E1624V00     | SHIPPED   |
            | W036825012004 | E1624V00     | AVAILABLE |

    Scenario: Update the inventory status of multiple inventories after receiving a Shipment Completed event for internal transfer shipment type.
        Given I have the following inventories:
            | Unit Number   | Product Code | Status    |
            | W036825012005 | WHOLEBLOOD   | AVAILABLE |
            | W036825012006 | RBC          | AVAILABLE |
            | W036825012007 | PLASMA       | AVAILABLE |
            | W036825012008 | PLASAPHP     | AVAILABLE |
        When I received a Shipment Completed event with shipment type "INTERNAL_TRANSFER" for the following units:
            | Unit Number   | Product Code |
            | W036825012005 | WHOLEBLOOD   |
            | W036825012006 | RBC          |
            | W036825012007 | PLASMA       |
        Then the inventory statuses should be updated as follows:
            | Unit Number   | Product Code | Status     |
            | W036825012005 | WHOLEBLOOD   | IN_TRANSIT |
            | W036825012006 | RBC          | IN_TRANSIT |
            | W036825012007 | PLASMA       | IN_TRANSIT |
            | W036825012008 | PLASAPHP     | AVAILABLE  |

