# Feature Unit Number reference: W777725012000
@api @LAB-160 @AOA-130 @cleanUpAll
Feature: Shipment Completed Event - API
    As a lab specialist I want the system is able to update inventory status of multiple inventories after receiving a Shipment Completed event.

    Scenario: Update the inventory status of multiple inventories after receiving a Shipment Completed event for customer shipment type.
        Given I have the following inventories:
            | Unit Number   | Product Code | Status    |
            | W777725012001 | E1624V00     | AVAILABLE |
            | W777725012002 | E1624V00     | AVAILABLE |
            | W777725012003 | E1624V00     | AVAILABLE |
            | W777725012004 | E1624V00     | AVAILABLE |
        When I received a Shipment Completed event with shipment type "CUSTOMER" for the following units:
            | Unit Number   | Product Code |
            | W777725012001 | E1624V00     |
            | W777725012002 | E1624V00     |
            | W777725012003 | E1624V00     |
        Then the inventory statuses should be updated as follows:
            | Unit Number   | Product Code | Status    |
            | W777725012001 | E1624V00     | SHIPPED   |
            | W777725012002 | E1624V00     | SHIPPED   |
            | W777725012003 | E1624V00     | SHIPPED   |
            | W777725012004 | E1624V00     | AVAILABLE |

    @LAB-289
    Scenario: Update the inventory status of multiple inventories after receiving a Shipment Completed event for internal transfer shipment type.
        Given I have the following inventories:
            | Unit Number   | Product Code | Status    |
            | W777725012005 | WHOLEBLOOD   | AVAILABLE |
            | W777725012006 | RBC          | AVAILABLE |
            | W777725012007 | PLASMA       | AVAILABLE |
            | W777725012008 | PLASAPHP     | AVAILABLE |
        When I received a Shipment Completed event with shipment type "INTERNAL_TRANSFER" and location code "1FS" for the following units:
            | Unit Number   | Product Code |
            | W777725012005 | WHOLEBLOOD   |
            | W777725012006 | RBC          |
            | W777725012007 | PLASMA       |
        Then the inventory statuses should be updated as follows:
            | Unit Number   | Product Code | Status     | Shipped Location |
            | W777725012005 | WHOLEBLOOD   | IN_TRANSIT | 1FS              |
            | W777725012006 | RBC          | IN_TRANSIT | 1FS              |
            | W777725012007 | PLASMA       | IN_TRANSIT | 1FS              |
            | W777725012008 | PLASAPHP     | AVAILABLE  |                  |

