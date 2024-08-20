@api
Feature: Validate Inventory

    Scenario Outline: Validate Inventory
        Given I have one product with "W012345678901", "E0869V00" and "LOCATION_1" in "AVAILABLE" status
        And I have one product with "W012345678902", "E0869V01" and "LOCATION_2" in "AVAILABLE" status
        And I have one product with "W012345678903", "E0869V02" and "LOCATION_2" in "QUARANTINED" status
        And I have one product with "W012345678904", "E0869V03" and "LOCATION_1" in "EXPIRED" status
        And I have one product with "W012345678905", "E0869V05" and "LOCATION_1" in "DISCARDED" status


        When I request "<Unit Number>" with "<Product Code>" in the "<Location>"

        Then I receive for "<Unit Number>" with "<Product Code>" in the "<Location>" a "<RESPONSE ERROR>" message

        Examples:
            | Unit Number   | Product Code | Location   | RESPONSE ERROR       |
            | W012345678901 | E0869V00     | LOCATION_1 |                      |
            | W012345678902 | E0869V01     | LOCATION_2 |                      |
            | W012345678903 | E0869V02     | LOCATION_2 | STATUS_IN_QUARANTINE |
            | W012345678904 | E0869V03     | LOCATION_1 | DATE_EXPIRED         |
            | W012345678901 | E0869V00     | LOCATION_2 | INVENTORY_NOT_FOUND  |
            | W012345678905 | E0869V05     | LOCATION_1 | INVENTORY_NOT_FOUND  |

