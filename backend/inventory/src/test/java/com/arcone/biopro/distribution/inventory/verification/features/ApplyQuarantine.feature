@api
@Disabled
#    TO DO: ENABLE THIS FEATURE FILE AFTER CHANGES IN APPLICATION ARE DONE
Feature: Product Quarantined Event - API

    As a lab specialist I want that a product can have multiple quarantines so that a product can be quarantined for numerous reasons

    Scenario Outline: Receive multiple quarantine reasons allowing to repeat reasons but with different ids
        Given I have this Apheresis Plasma product
            | Unit Number   | Product Code | Location       | Product Description                            |
            | W036824111111 | E086900      | Charlotte Main | Apheresis FRESH FROZEN PLASMA\|ACD-A/XX/<=-18C |

        When I received an Apply Quarantine event for unit "<Unit Number>" and product "<Product Code>" with reason "ABS Positive" and id "1"
        And I received an Apply Quarantine event for unit "<Unit Number>" and product "<Product Code>" with reason "BCA Unit Needed" and id "2"
        And I received an Apply Quarantine event for unit "<Unit Number>" and product "<Product Code>" with reason "Hold Until Expiration" and id "3"
        And I received an Apply Quarantine event for unit "<Unit Number>" and product "<Product Code>" with reason "ABS Positive" and id "4"
        And I received an Apply Quarantine event for unit "<Unit Number>" and product "<Product Code>" with reason "ABS Positive" and id "5"
        And I received an Apply Quarantine event for unit "<Unit Number>" and product "<Product Code>" with reason " BCA Unit Needed" and id "6"

        Then The inventory status for unit "<Unit Number>" and product "<Product Code>" is "QUARANTINED"
        And I verify the quarantine reason "<Quarantine Reason>" with id "<Id>" is found "true" for unit number "<Unit Number>" and product "<Product Code>"

        Examples:
            | Unit Number   | Product Code | Quarantine Reason     | Id |
            | W036824111111 | E086900      | ABS Positive          | 1  |
            | W036824111111 | E086900      | BCA Unit Needed       | 2  |
            | W036824111111 | E086900      | Hold Until Expiration | 3  |
            | W036824111111 | E086900      | ABS Positive          | 4  |
            | W036824111111 | E086900      | BCA Unit Needed       | 5  |

