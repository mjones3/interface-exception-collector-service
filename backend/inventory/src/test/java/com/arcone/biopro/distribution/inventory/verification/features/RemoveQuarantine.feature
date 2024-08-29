@api
Feature: Remove Quarantine Event - API
    As a labeling specialist I want the system is able to restore the previous product label status after receiving a Remove Quarantine event.

    Background:
        Given I have this Apheresis Plasma product
            | Unit Number   | Product Code | Location       | ABORH | Expiration Date          | Temperature Category | Label Status | Quarantine Reason |
            | W036824111111 | E162400      | Charlotte Main | OP    | 2025-01-08T02:05:45.231Z | Frozen               | QUARANTINED  | ABS Positive      |

    Scenario Outline: Revert the product to the previous status after receiving a Remove Quarantine event.
        Given The previous status for unit number "W036824111111" and product "E162400" was "<Previous Status>"
        And I add quarantine with reason "BCA_UNIT_NEEDED" and id "2" to the unit "W036824111111" and the product "E162400"

        When I received a "Remove Quarantine" event for unit "W036824111111" and product "E162400" with reason "ABS Positive" and id "1"
        And I received a "Remove Quarantine" event for unit "W036824111111" and product "E162400" with reason "BCA Unit Needed" and id "2"

        Then I verify the label status for unit number "W036824111111" and product "E162400" is "<Previous Status>"

        Examples:
            | Previous Status |
            | READY_TO_BE_LABELED |
            | NOT_READY_TO_LABEL |
            | PRINTED |
            | LABELED |
            | UNSUITABLE |

    Scenario: Remove only the quarantine that matches the quarantine reason received in the quarantine event.
        Given The previous status for unit number "W036824111111" and product "E162400" was "READY_TO_BE_LABELED"
        And I add quarantine with reason "BCA_UNIT_NEEDED" and id "2" to the unit "W036824111111" and the product "E162400"

        When I received a "Remove Quarantine" event for unit "W036824111111" and product "E162400" with reason "BCA Unit Needed" and id "2"

        Then I verify the quarantine reason "ABS Positive" with id "1" is found "true" for unit number "W036824111111" and product "E162400"
        And I verify the quarantine reason "BCA Unit Needed" with id "2" is found "false" for unit number "W036824111111" and product "E162400"
        And I verify the label status for unit number "W036824111111" and product "E162400" is "QUARANTINED"
