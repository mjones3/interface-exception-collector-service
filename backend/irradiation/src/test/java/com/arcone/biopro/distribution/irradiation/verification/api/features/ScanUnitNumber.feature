# Feature Unit Number reference: W777725001xxx

@api @hzd9
Feature: Scan Unit Number for Irradiation

    @LAB-576 @AOA-61
    Scenario Outline: I successfully scan a unit number with a product eligible for irradiation
        Given I have the following inventory products:
            | Unit Number   | Product Code | Status     | Location  | Product Family               |
            | W777725001001 | E0869V00     | AVAILABLE  | 123456789 | PLASMA_TRANSFUSABLE          |
            | W777725001001 | E1624V00     | IN_TRANSIT | 123456789 | PLASMA_TRANSFUSABLE          |
            | W777725001001 | E4689V00     | SHIPPED    | 123456789 | PLASMA_TRANSFUSABLE          |
            | W777725001001 | E0686V00     | CONVERTED  | 123456789 | RED_BLOOD_CELLS_LEUKOREDUCED |
            | W777725001001 | E2555V00     | MODIFIED   | 123456789 | PLASMA_TRANSFUSABLE          |
            | W777725001001 | E7644V00     | AVAILABLE  | 234567891 | PLASMA_TRANSFUSABLE          |

        And I'm in the irradiation service at the location "<Location>"
        When I scan the unit number "<Unit Number>" in irradiation
        Then I verify that there are only 1 product(s) eligible for irradiation for the unit number "<Unit Number>"
        Then I see the product "<Product Code>" from unit number "<Unit Number>" is in the list of products for selection
        Examples:
            | Unit Number   | Location  | Product Code |
            | W777725001001 | 123456789 | E0869V00     |

    @disabled @LAB-615 @AOA-61
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

    @disabled @LAB-615 @AOA-61
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

    @disabled @LAB-615 @AOA-61
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

    @disabled @LAB-615 @AOA-61
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

    @disabled @LAB-615 @AOA-61
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

    @disabled @LAB-615 @AOA-61
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

    @LAB-615
    Scenario: I cannot add a product that was already irradiated
        Given I have the following inventory products:
            | Unit Number   | Product Code | Status    | Location  | Product Family |
            | W777725001011 | E003200      | AVAILABLE | 123456789 | WHOLE_BLOOD    |
        And I'm in the irradiation service at the location "123456789"
        And the product "E003200" in the unit "W777725001011" was already irradiated in a completed batch for device "AUTO-DEVICE1011"
        When I scan the unit number "W777725001011" in irradiation
        Then I verify that product "E003200" in the unit "W777725001011" is flagged as already irradiated

    @LAB-615
    Scenario: I cannot add a product that is not configured for irradiation
        Given I have the following inventory products:
            | Unit Number   | Product Code | Status    | Location  | Product Family |
            | W777725001012 | E0869V00     | AVAILABLE | 123456789 | WHOLE_BLOOD    |
        And I'm in the irradiation service at the location "123456789"
        When I scan the unit number "W777725001012" in irradiation
        Then I verify that product "E0869V00" in the unit "W777725001012" is flagged as not configurable for irradiation

    @LAB-615
    Scenario: I cannot add a product that is currently being irradiated
        Given I have the following inventory products:
            | Unit Number   | Product Code | Status    | Location  | Product Family |
            | W777725001013 | E003300      | AVAILABLE | 123456789 | WHOLE_BLOOD    |
        And I'm in the irradiation service at the location "123456789"
        And the product "E003300" in the unit "W777725001013" was already irradiated in a opened batch for device "AUTO-DEVICE1013"
        When I scan the unit number "W777725001013" in irradiation
        Then I see the error message "No products eligible for irradiation"
