@api
Feature: Validate Inventory

    Scenario Outline: Validate Inventory
        Given I have one product with "W012345678901", "E0869V00" and "LOCATION_1" in "AVAILABLE" status
        And I have one product with "W012345678902", "E0869V01" and "LOCATION_2" in "AVAILABLE" status
        And I have one product with "W012345678903", "E0869V02" and "LOCATION_2" in "QUARANTINED" status
        And I have one product with "W012345678904", "E0869V03" and "LOCATION_1" in "EXPIRED" status
        And I have one product with "W012345678905", "E0869V05" and "LOCATION_1" in "DISCARDED" status with reason "ADDITIVE_SOLUTION_ISSUES" and comments ""
        And I have one product with "W012345678906", "E0869V05" and "LOCATION_1" in "UNSUITABLE" status
        And I have one product with "W012345678907", "E0869V05" and "LOCATION_1" in "DISCARDED" status with reason "OTHER" and comments "Some comments"
        When I request "<Unit Number>" with "<Product Code>" in the "<Location>"

        Then I receive for "<Unit Number>" with "<Product Code>" in the "<Location>" a "<RESPONSE ERROR>" message with "<ACTION>" action and "<REASON>" reason and "<MESSAGE>" message

        Examples:
            | Unit Number   | Product Code | Location   | RESPONSE ERROR                  | ACTION             | REASON          | MESSAGE                                                                                                              |
            | W012345678901 | E0869V00     | LOCATION_1 |                                 |                    |                 |                                                                                                                      |
            | W012345678902 | E0869V01     | LOCATION_2 |                                 |                    |                 |                                                                                                                      |
            | W012345678903 | E0869V02     | LOCATION_2 | INVENTORY_IS_QUARANTINED        | BACK_TO_STORAGE    | BACK_TO_STORAGE | This product is currently in quarantine and needs to be returned to storage.                                         |
            | W012345678903 | E0869V02     | LOCATION_1 | INVENTORY_NOT_FOUND_IN_LOCATION | BACK_TO_STORAGE    | BACK_TO_STORAGE | This product is not in this location and cannot be shipped.                                                          |
            | W012345678904 | E0869V03     | LOCATION_1 | INVENTORY_IS_EXPIRED            | TRIGGER_DISCARD    | EXPIRED         | This product is expired and has been discarded. Place in biohazard container.                                        |
            | W012345678901 | E0869V00     | LOCATION_2 | INVENTORY_NOT_FOUND_IN_LOCATION | BACK_TO_STORAGE    |                 | This product is not in this location and cannot be shipped.                                                          |
            | W012345678905 | E0869V05     | LOCATION_1 | INVENTORY_IS_DISCARDED          | PLACE_IN_BIOHAZARD |                 | This product has already been discarded for Additive Solution Issues in the system. Place in biohazard container.    |
            | W012345678906 | E0869V05     | LOCATION_1 | INVENTORY_IS_UNSUITABLE         | TRIGGER_DISCARD    |                 | This product has an active deferral with a discard consequence and has been discarded. Place in biohazard container. |
            | W012345678907 | E0869V05     | LOCATION_1 | INVENTORY_IS_DISCARDED          | PLACE_IN_BIOHAZARD |                 | OTHER: Some comments                                                                                                 |



