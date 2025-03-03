@api @LAB-116 @AOA-75 @LAB-185 @LAB-254
Feature: Validate Inventory

    Scenario Outline: Validate Inventory
        Given I have one product with "W012345678901", "E0869V00" and "LOCATION_1" in "AVAILABLE" status
        And I have one product with "W012345678902", "E0869VA0" and "LOCATION_2" in "AVAILABLE" status
        And I have one product with "W012345678903", "E0869VB0" and "LOCATION_2" in "AVAILABLE" status with quarantine reasons "ABS_POSITIVE, PENDING_FURTHER_REVIEW_INSPECTION, OTHER" and comments "Quarantine other comments"
        And I have one product with "W012345678904", "E0869VC0" and "LOCATION_1" in "EXPIRED" status
        And I have one product with "W012345678905", "E0869VD0" and "LOCATION_1" in "DISCARDED" status with reason "ADDITIVE_SOLUTION_ISSUES" and comments ""
        And I have one product with "W012345678906", "E0869VD0" and "LOCATION_1" in "AVAILABLE" status with unsuitable reason "ACTIVE_DEFERRAL"
        And I have one product with "W012345678906", "E1624V00" and "LOCATION_1" in "AVAILABLE" status with unsuitable reason "TIMING_RULES"
        And I have one product with "W012345678907", "E0869VD0" and "LOCATION_1" in "DISCARDED" status with reason "OTHER" and comments "Some comments"
        And I have one product with "W012345678908", "E0869VD0" and "LOCATION_1" in "AVAILABLE" status and is unlabeled
        When I request "<Unit Number>" with "<Product Code>" in the "<Location>"

        Then I receive for "<Unit Number>" with "<Product Code>" in the "<Location>" a "<RESPONSE ERROR>" message with "<ACTION>" action and "<REASON>" reason and "<MESSAGE>" message and details "<DETAILS>"

        Examples:
            | Unit Number   | Product Code | Location   | RESPONSE ERROR                  | ACTION             | REASON          | MESSAGE                                                                                                              | DETAILS                                                                             |
            | W012345678901 | E0869V00     | LOCATION_1 |                                 |                    |                 |                                                                                                                      |                                                                                     |
            | W012345678902 | E0869VA0     | LOCATION_2 |                                 |                    |                 |                                                                                                                      |                                                                                     |
            | W012345678903 | E0869VB0     | LOCATION_2 | INVENTORY_IS_QUARANTINED        | BACK_TO_STORAGE    | BACK_TO_STORAGE | This product is currently in quarantine and needs to be returned to storage.                                         | ABS POSITIVE, PENDING FURTHER REVIEW / INSPECTION, OTHER: Quarantine other comments |
            | W012345678903 | E0869VB0     | LOCATION_1 | INVENTORY_NOT_FOUND_IN_LOCATION | BACK_TO_STORAGE    | BACK_TO_STORAGE | This product is not in this location and cannot be shipped.                                                          |                                                                                     |
            | W012345678904 | E0869VC0     | LOCATION_1 | INVENTORY_IS_EXPIRED            | TRIGGER_DISCARD    | EXPIRED         | This product is expired and has been discarded. Place in biohazard container.                                        |                                                                                     |
            | W012345678901 | E0869V00     | LOCATION_2 | INVENTORY_NOT_FOUND_IN_LOCATION | BACK_TO_STORAGE    |                 | This product is not in this location and cannot be shipped.                                                          |                                                                                     |
            | W012345678905 | E0869VD0     | LOCATION_1 | INVENTORY_IS_DISCARDED          | PLACE_IN_BIOHAZARD |                 | This product has already been discarded for Additive Solution Issues in the system. Place in biohazard container.    |                                                                                     |
            | W012345678906 | E0869VD0     | LOCATION_1 | INVENTORY_IS_UNSUITABLE         | TRIGGER_DISCARD    | ACTIVE_DEFERRAL | This product has an active deferral with a discard consequence and has been discarded. Place in biohazard container. |                                                                                     |
            | W012345678906 | E1624V00     | LOCATION_1 | INVENTORY_IS_UNSUITABLE         | TRIGGER_DISCARD    | TIMING_RULES    | This product has been discarded for Timing Rules. Place in biohazard container.                                      |                                                                                     |
            | W012345678907 | E0869VD0     | LOCATION_1 | INVENTORY_IS_DISCARDED          | PLACE_IN_BIOHAZARD |                 | This product has already been discarded for OTHER: Some comments in the system. Place in biohazard container.        |                                                                                     |
            | W012345678908 | E0869VD0     | LOCATION_1 | INVENTORY_IS_UNLABELED          | BACK_TO_STORAGE    |                 | This product is not labeled and cannot be shipped.                                                                   |                                                                                     |
