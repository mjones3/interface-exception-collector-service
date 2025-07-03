# Feature Unit Number reference: W777725012000
@api @LAB-289 @AOA-19 @cleanUpAll
Feature: Product Received Event - API
    As a lab specialist I want the system is able to update inventory status of multiple inventories after receiving a Product Received event.

    Scenario: Update the inventory status and inventory location of multiple inventories after receiving a Shipment Completed event for internal transfer shipment type.
        Given I have the following inventories:
            | Unit Number   | Product Code | Status     |
            | W777725012005 | WHOLEBLOOD   | IN_TRANSIT |
            | W777725012006 | RBC          | IN_TRANSIT |
            | W777725012007 | PLASMA       | IN_TRANSIT |
            | W777725012008 | PLASAPHP     | IN_TRANSIT |
        When I received a Product Completed event with location code "1FS" for the following units:
            | Unit Number   | Product Code |
            | W777725012005 | WHOLEBLOOD   |
            | W777725012006 | RBC          |
            | W777725012007 | PLASMA       |
        Then the inventory statuses should be updated as follows:
            | Unit Number   | Product Code | Status     | Inventory Location |
            | W777725012005 | WHOLEBLOOD   | AVAILABLE  | 1FS                |
            | W777725012006 | RBC          | AVAILABLE  | 1FS                |
            | W777725012007 | PLASMA       | AVAILABLE  | 1FS                |
            | W777725012008 | PLASAPHP     | IN_TRANSIT |                    |

