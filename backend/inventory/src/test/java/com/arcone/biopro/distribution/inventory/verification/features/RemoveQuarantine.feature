@api
Feature: Remove Quarantine Event - API
    As a lab specialist I want the system is able to restore the previous inventory status after receiving a Remove Quarantine event.

    Scenario: Revert the product to the previous status after receiving a Remove Quarantine event.
        Given I have a unit number "W036824111111" with product "E1624V00" that is "AVAILABLE"
        When I received an Apply Quarantine event for unit "W036824111111" and product "E1624V00" with reason "ABS Positive" and id "1"
        And I received an Apply Quarantine event for unit "W036824111111" and product "E1624V00" with reason "BCA Unit Needed" and id "2"
        Then The inventory status is "QUARANTINED"

        When I received a Remove Quarantine event for unit "W036824111111" and product "E1624V00" with reason "ABS Positive" and id "1"
        And I received a Remove Quarantine event for unit "W036824111111" and product "E1624V00" with reason "BCA Unit Needed" and id "2"
        Then The inventory status is "AVAILABLE"


    Scenario: Remove only the quarantine that matches the quarantine reason received in the quarantine event.
        Given I have a unit number "W036824111111" with product "E1624V00" that is "AVAILABLE"
        When I received an Apply Quarantine event for unit "W036824111111" and product "E162400" with reason "ABS Positive" and id "1"
        And I received an Apply Quarantine event for unit "W036824111111" and product "E162400" with reason "BCA Unit Needed" and id "2"
        And I received an Apply Quarantine event for unit "W036824111111" and product "E162400" with reason "BCA Unit Needed" and id "3"
        Then The inventory status is "QUARANTINED"

        When I received a Remove Quarantine event for unit "W036824111111" and product "E162400" with reason "BCA Unit Needed" and id "2"

        Then I verify the quarantine reason "ABS Positive" with id "1" is found "true" for unit number "W036824111111" and product "E162400"
        And I verify the quarantine reason "BCA Unit Needed" with id "2" is found "false" for unit number "W036824111111" and product "E162400"
        And I verify the quarantine reason "BCA Unit Needed" with id "3" is found "true" for unit number "W036824111111" and product "E162400"
        Then The inventory status is "QUARANTINED"
