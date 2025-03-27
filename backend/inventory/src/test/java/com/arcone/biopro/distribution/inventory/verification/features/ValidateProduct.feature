# Feature Unit Number reference: W036825014000
@api @LAB-116 @AOA-75 @LAB-185 @LAB-254 @LAB-379
Feature: Validate Inventory

    Scenario Outline: Validate Inventory
        Given I have one product with "W036825014001", "E0869V00" and "LOCATION_1" in "AVAILABLE" status
        And I have one product with "W036825014002", "E0869VA0" and "LOCATION_2" in "AVAILABLE" status
        And I have one product with "W036825014003", "E0869VB0" and "LOCATION_2" in "AVAILABLE" status with quarantine reasons "ABS_POSITIVE, PENDING_FURTHER_REVIEW_INSPECTION, OTHER" and comments "Quarantine other comments"
        And I have one product with "W036825014004", "E0869VC0" and "LOCATION_1" in "EXPIRED" status
        And I have one product with "W036825014005", "E0869VD0" and "LOCATION_1" in "DISCARDED" status with reason "ADDITIVE_SOLUTION_ISSUES" and comments ""
        And I have one product with "W036825014006", "E0869VD0" and "LOCATION_1" in "AVAILABLE" status with unsuitable reason "ACTIVE_DEFERRAL"
        And I have one product with "W036825014006", "E1624V00" and "LOCATION_1" in "AVAILABLE" status with unsuitable reason "TIMING_RULES"
        And I have one product with "W036825014007", "E0869VD0" and "LOCATION_1" in "DISCARDED" status with reason "OTHER" and comments "Some comments"
        And I have one product with "W036825014008", "E0869VD0" and "LOCATION_1" in "AVAILABLE" status and is unlabeled
        When I request "<Unit Number>" with "<Product Code>" in the "<Location>"

        Then I receive for "<Unit Number>" with "<Product Code>" and temperature category "<Temperature Category>" in the "<Location>" a "<RESPONSE ERROR>" message with "<ACTION>" action and "<REASON>" reason and "<MESSAGE>" message and details "<DETAILS>"

        Examples:
            | Unit Number   | Product Code | Temperature Category | Location   | RESPONSE ERROR                  | ACTION             | REASON          | MESSAGE                                                                                                              | DETAILS                                                                             |
            | W036825014001 | E0869V00     | FROZEN               | LOCATION_1 |                                 |                    |                 |                                                                                                                      |                                                                                     |
            | W036825014002 | E0869VA0     | FROZEN               | LOCATION_2 |                                 |                    |                 |                                                                                                                      |                                                                                     |
            | W036825014003 | E0869VB0     | FROZEN               | LOCATION_2 | INVENTORY_IS_QUARANTINED        | BACK_TO_STORAGE    | BACK_TO_STORAGE | This product is currently in quarantine and needs to be returned to storage.                                         | ABS POSITIVE, PENDING FURTHER REVIEW / INSPECTION, OTHER: Quarantine other comments |
            | W036825014003 | E0869VB0     | FROZEN               | LOCATION_1 | INVENTORY_NOT_FOUND_IN_LOCATION | BACK_TO_STORAGE    | BACK_TO_STORAGE | This product is not in this location and cannot be shipped.                                                          |                                                                                     |
            | W036825014004 | E0869VC0     | FROZEN               | LOCATION_1 | INVENTORY_IS_EXPIRED            | TRIGGER_DISCARD    | EXPIRED         | This product is expired and has been discarded. Place in biohazard container.                                        |                                                                                     |
            | W036825014001 | E0869V00     | FROZEN               | LOCATION_2 | INVENTORY_NOT_FOUND_IN_LOCATION | BACK_TO_STORAGE    |                 | This product is not in this location and cannot be shipped.                                                          |                                                                                     |
            | W036825014005 | E0869VD0     | FROZEN               | LOCATION_1 | INVENTORY_IS_DISCARDED          | PLACE_IN_BIOHAZARD |                 | This product has already been discarded for Additive Solution Issues in the system. Place in biohazard container.    |                                                                                     |
            | W036825014006 | E0869VD0     | FROZEN               | LOCATION_1 | INVENTORY_IS_UNSUITABLE         | TRIGGER_DISCARD    | ACTIVE_DEFERRAL | This product has an active deferral with a discard consequence and has been discarded. Place in biohazard container. |                                                                                     |
            | W036825014006 | E1624V00     | FROZEN               | LOCATION_1 | INVENTORY_IS_UNSUITABLE         | TRIGGER_DISCARD    | TIMING_RULES    | This product has been discarded for Timing Rules. Place in biohazard container.                                      |                                                                                     |
            | W036825014007 | E0869VD0     | FROZEN               | LOCATION_1 | INVENTORY_IS_DISCARDED          | PLACE_IN_BIOHAZARD |                 | This product has already been discarded for OTHER: Some comments in the system. Place in biohazard container.        |                                                                                     |
            | W036825014008 | E0869VD0     | FROZEN               | LOCATION_1 | INVENTORY_IS_UNLABELED          | BACK_TO_STORAGE    |                 | This product is not labeled and cannot be shipped.                                                                   |                                                                                     |
