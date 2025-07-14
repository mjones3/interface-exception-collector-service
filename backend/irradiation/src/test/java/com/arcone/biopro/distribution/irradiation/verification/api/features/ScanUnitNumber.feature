# Feature Unit Number reference: W777725001xxx

@api
Feature: Scan Unit Number for Irradiation

    @LAB-576 @AOA-61
    Scenario Outline: I successfully scan a unit number with a product eligible for irradiation
        Given I have the following inventory products:
            | Unit Number   | Product Code | Status     | Location  | Product Family |
            | W777725001001 | E0869V00     | AVAILABLE  | 123456789 | WHOLE_BLOOD    |
            | W777725001001 | E0868V00     | IN_TRANSIT | 123456789 | WHOLE_BLOOD    |
            | W777725001001 | E0867V00     | SHIPPED    | 123456789 | WHOLE_BLOOD    |
        And I'm in the irradiation service at the location "<Location>"
        When I scan the unit number "<Unit Number>" in irradiation
        Then I verify that there are only 1 product(s) eligible for irradiation for the unit number "<Unit Number>"
        Then I see the product "<Product Code>" from unit number "<Unit Number>" is in the list of products for selection
        Examples:
            | Unit Number   | Location  | Product Code |
            | W777725001001 | 123456789 | E0869V00     |

    @LAB-615 @AOA-61 @disabled
    Scenario Outline: I cannot add into the batch a quarantined unit number with a reason that stops manufacturing
        Given I have the following inventory products:
            | Unit Number   | Product Code | Product Family | Status      | Stops Manufacturing |
            | <Unit Number> | E033600      | WHOLE_BLOOD    | Quarantined | Yes                 |
        And I'm in the irradiation service at the location "<Location>"
        When I scan the unit number "<Unit Number>" in irradiation
        Then I see the product "E033600" from unit number "<Unit Number>" is in the list of products for selection
#        When I select the product "E033600" from unit number "<Unit Number>"
#        Then I see the error message "<Error Message>"

        Examples:
            | Unit Number   | Error Message                                                        |
            | W777725001002 | This unit has been quarantined and manufacturing cannot be completed |

    @LAB-615 @AOA-61 @disabled
    Scenario Outline: I can add into the batch a quarantined unit number with a reason that doesn't stops manufacturing, being warned with a message
        Given I have the following inventory products:
            | Unit Number   | Product Code | Product Family | Status      | Stops Manufacturing |
            | <Unit Number> | E033600      | WHOLE_BLOOD    | Quarantined | No                  |
        And I'm in the irradiation service at the location "<Location>"
        When I scan the unit number "<Unit Number>" in irradiation
        Then I see the product "E033600" from unit number "<Unit Number>" is in the list of products for selection
#        When I select the product "E033600" from unit number "<Unit Number>"
#        Then I see the "success" message "<Message>"

        Examples:
            | Unit Number   | Message                        |
            | W777725001003 | Product was added in the batch |

    @LAB-615 @AOA-61 @disabled
    Scenario Outline: I cannot add into the batch a discarded unit number
        Given I have the following inventory products:
            | Unit Number   | Product Code | Product Family | Status    |
            | <Unit Number> | E033600      | WHOLE_BLOOD    | Discarded |
        And I'm in the irradiation service at the location "<Location>"
        When I scan the unit number "<Unit Number>" in irradiation
        Then I see the product "E033600" from unit number "<Unit Number>" is in the list of products for selection
#        When I select the product "E033600" from unit number "<Unit Number>"
#        Then I see the error message "<Error Message>"
        Examples:
            | Unit Number   | Error Message                                                                                    |
            | W777725001004 | This unit has been discarded and manufacturing cannot be completed. Place in biohazard container |

    @LAB-615 @AOA-61 @disabled
    Scenario Outline: I cannot add into the batch a unit number that is marked for discard/unsuitable
        Given I have the following inventory products:
            | Unit Number   | Product Code | Product Family | Status             | Discard Reason                 |
            | <Unit Number> | E033600      | WHOLE_BLOOD    | MARKED_FOR_DISCARD | POSITIVE_REACTIVE_TEST_RESULTS |
        And I'm in the irradiation service at the location "<Location>"
        When I scan the unit number "<Unit Number>" in irradiation
        Then I see the product "E033600" from unit number "<Unit Number>" is in the list of products for selection
#        When I select the product "E033600" from unit number "<Unit Number>"
#        Then I see the error message "<Error Message>"

        Examples:
            | Unit Number   | Error Message                                                                                           |
            | W777725001005 | This product is unsuitable with the reason Positive Reactive Test Results. Place in biohazard container |

    @LAB-615 @AOA-61 @disabled
    Scenario Outline: I cannot add into the batch a unit number that is not in the location
        Given I have the following inventory products:
            | Unit Number   | Product Code | Product Family | Location      |
            | <Unit Number> | E033600      | WHOLE_BLOOD    | Diff Location |
        And I'm in the irradiation service at the location "<Location>"
        When I scan the unit number "<Unit Number>" in irradiation
        Then I see the product "E033600" from unit number "<Unit Number>" is NOT in the list of products for selection

        Examples:
            | Unit Number   |
            | W777725001006 |

    @LAB-615 @AOA-61 @disabled
    Scenario Outline: I cannot add into the batch a unit number that is not in the location
        Given I have the following inventory products:
            | Unit Number   | Product Code | Product Family | Status   |
            | <Unit Number> | E033600      | WHOLE_BLOOD    | <Status> |
        And I'm in the irradiation service at the location "<Location>"
        When I scan the unit number "<Unit Number>" in irradiation
        Then I see the product "E033600" from unit number "<Unit Number>" is NOT in the list of products for selection
        Examples:
            | Unit Number   | Status     |
            | W777725001007 | CONVERTED  |
            | W777725001008 | MODIFIED   |
            | W777725001009 | SHIPPED    |
            | W777725001010 | IN_TRANSIT |
