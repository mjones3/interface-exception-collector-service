# Feature Unit Number reference: W036825011000
@api @LAB-79 @AOA-75 @LAB-254 @skipOnPipeline
Feature: Remove Quarantine Event - API
    As a lab specialist I want the system is able to restore the previous inventory status after receiving a Remove Quarantine event.

    Scenario Outline: Revert the inventory to the previous status after all Quarantines are removed.
        Given I have a unit number "<Unit Number>" with product "E1624V00" that is "AVAILABLE"
        When I received an Apply Quarantine event for unit "<Unit Number>" and product "E1624V00" with reason "ABS Positive" and id "1"
        Then The inventory status has quarantine

        When I received an Apply Quarantine event for unit "<Unit Number>" and product "E1624V00" with reason "BCA Unit Needed" and id "2"
        Then The inventory status has quarantine

        When I received a Remove Quarantine event for unit "<Unit Number>" and product "E1624V00" with reason "ABS Positive" and id "1"
        Then The inventory status has quarantine

        When I received a Remove Quarantine event for unit "<Unit Number>" and product "E1624V00" with reason "BCA Unit Needed" and id "2"
        Then The inventory status is "AVAILABLE"
        Examples:
            | Unit Number   |
            | W036825011001 |


    Scenario Outline: Remove the correct quarantine reason received in the remove quarantine event.
        Given I have a unit number "<Unit Number>" with product "E1624V00" that is "AVAILABLE"
        When I received an Apply Quarantine event for unit "<Unit Number>" and product "E1624V00" with reason "ABS Positive" and id "1"
        And I received an Apply Quarantine event for unit "<Unit Number>" and product "E1624V00" with reason "BCA Unit Needed" and id "2"
        And I received an Apply Quarantine event for unit "<Unit Number>" and product "E1624V00" with reason "BCA Unit Needed" and id "3"
        Then The inventory status has quarantine

        When I received a Remove Quarantine event for unit "<Unit Number>" and product "E1624V00" with reason "BCA Unit Needed" and id "2"

        Then I verify the quarantine reason "ABS Positive" with id "1" is found "true" for unit number "<Unit Number>" and product "E1624V00"
        And I verify the quarantine reason "BCA Unit Needed" with id "2" is found "false" for unit number "<Unit Number>" and product "E1624V00"
        And I verify the quarantine reason "BCA Unit Needed" with id "3" is found "true" for unit number "<Unit Number>" and product "E1624V00"
        Then The inventory status has quarantine
        Examples:
            | Unit Number   |
            | W036825011002 |
