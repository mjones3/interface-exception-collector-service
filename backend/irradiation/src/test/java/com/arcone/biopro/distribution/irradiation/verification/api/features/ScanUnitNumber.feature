# Feature Unit Number reference: W777725001xxx

@api @hzd9 @AOA-61
Feature: Scan Unit Number for Irradiation

    @LAB-576
    Scenario: I successfully scan a unit number with a product eligible for irradiation
        Given I have the following inventory products:
            | Unit Number   | Product Code | Status     | Location  |
            | W777725001001 | E003300      | AVAILABLE  | 123456789 |
            | W777725001001 | E0033V00     | AVAILABLE  | 123456789 |
            | W777725001001 | E1624V00     | IN_TRANSIT | 123456789 |
            | W777725001001 | E4689V00     | SHIPPED    | 123456789 |
            | W777725001001 | E0686V00     | CONVERTED  | 123456789 |
            | W777725001001 | E2555V00     | MODIFIED   | 123456789 |
            | W777725001001 | E7644V00     | AVAILABLE  | 234567891 |

        And I'm in the irradiation service at the location "123456789"
        When I scan the unit number "W777725001001" in irradiation
        Then I verify that there are only 2 product(s) eligible for irradiation for the unit number "W777725001001"
        And I see the product "E003300" from unit number "W777725001001" is in the list of products for selection
        And I see the product "E0033V00" from unit number "W777725001001" is in the list of products for selection
        And I verify that product "E003300" in the unit "W777725001001" is flagged as configurable for irradiation
        And I verify that product "E0033V00" in the unit "W777725001001" is flagged as configurable for irradiation

    @LAB-603 @LAB-621
    Scenario Outline: I successfully manually enter a unit number and check digit with a product eligible for irradiation
        Given I have the following inventory products:
            | Unit Number   | Product Code | Status     | Location  |
            | <Unit Number> | <Product Code> | AVAILABLE  | <Location> |

        And I'm in the irradiation service at the location "<Location>"
        And the "Check Digit" is configured as "Y"
        When I enter the unit number "<Unit Number>" in irradiation and the check digit
        Then I verify that there are only 2 product(s) eligible for irradiation for the unit number "<Unit Number>"
        And I see the product "<Product Code>" from unit number "<Unit Number>" is in the list of products for selection
        And I verify that product "<Product Code>" in the unit "<Unit Number>" is flagged as configurable for irradiation
        Examples:
            | Unit Number   | Location  | Product Code |
            | W777725001001 | 123456789 | E003300     |

    @LAB-615
    Scenario: I cannot add into the batch a quarantined unit number with a reason that stops manufacturing
        Given I have the following inventory products:
            | Unit Number   | Product Code | Status      | Stops Manufacturing |
            | W777725001002 | E003300      | Quarantined | Yes                 |
        And I'm in the irradiation service at the location "123456789"
        When I scan the unit number "W777725001002" in irradiation
        Then I see the product "E003300" from unit number "W777725001002" is in the list of products for selection
        Then I verify that product "E003300" in the unit "W777725001002" is flagged as quarantined that stops manufacturing

    @LAB-615
    Scenario: I can add into the batch a quarantined unit number with a reason that doesn't stops manufacturing, being warned with a message
        Given I have the following inventory products:
            | Unit Number   | Product Code | Status      | Stops Manufacturing |
            | W777725001003 | E003300      | Quarantined | No                  |
        And I'm in the irradiation service at the location "123456789"
        When I scan the unit number "W777725001003" in irradiation
        Then I see the product "E003300" from unit number "W777725001003" is in the list of products for selection
        Then I verify that product "E003300" in the unit "W777725001003" is flagged as quarantined that does not stops manufacturing

    @LAB-615
    Scenario: I cannot add into the batch a discarded unit number
        Given I have the following inventory products:
            | Unit Number   | Product Code | Status    | Reason  |
            | W777725001004 | E003300      | Discarded | EXPIRED |
        And I'm in the irradiation service at the location "123456789"
        When I scan the unit number "W777725001004" in irradiation
        Then I see the product "E003300" from unit number "W777725001004" is in the list of products for selection
        Then I verify that product "E003300" in the unit "W777725001004" is flagged as discarded as "EXPIRED"

    @LAB-615
    Scenario: I cannot add into the batch a unit number that is marked for discard/unsuitable
        Given I have the following inventory products:
            | Unit Number   | Product Code | Status    | Unsuitable Reason              |
            | W777725001005 | E003300      | AVAILABLE | POSITIVE_REACTIVE_TEST_RESULTS |
        And I'm in the irradiation service at the location "123456789"
        When I scan the unit number "W777725001005" in irradiation
        Then I see the product "E003300" from unit number "W777725001005" is in the list of products for selection
        Then I verify that product "E003300" in the unit "W777725001005" is flagged as unsuitable with reason "POSITIVE_REACTIVE_TEST_RESULTS"


    @LAB-615
    Scenario: I cannot add into the batch a unit number that is expired
        Given I have the following inventory products:
            | Unit Number   | Product Code | Status    | Expired |
            | W777725001006 | E003300      | AVAILABLE | YES     |
        And I'm in the irradiation service at the location "123456789"
        When I scan the unit number "W777725001006" in irradiation
        Then I see the product "E003300" from unit number "W777725001006" is in the list of products for selection
        Then I verify that product "E003300" in the unit "W777725001006" is flagged as expired


    @LAB-615
    Scenario: I cannot add into the batch a unit number that is not in the location
        Given I have the following inventory products:
            | Unit Number   | Product Code | Status    | Location  |
            | W777725001007 | E003300      | AVAILABLE | 234567891 |
        And I'm in the irradiation service at the location "123456789"
        When I scan the unit number "W777725001007" in irradiation
        Then I see the error message "No products eligible for irradiation"

    @LAB-615
    Scenario: I cannot add a product that was already irradiated
        Given I have the following inventory products:
            | Unit Number   | Product Code | Status    | Location  |
            | W777725001011 | E033600      | AVAILABLE | 123456789 |
            | W777725001011 | E003300      | AVAILABLE | 123456789 |
        And I'm in the irradiation service at the location "123456789"
        And the product "E003300" in the unit "W777725001011" was already irradiated in a completed batch for device "AUTO-DEVICE1011"
        When I scan the unit number "W777725001011" in irradiation
        Then I see the product "E033600" from unit number "W777725001011" is in the list of products for selection
        And I see the product "E003300" from unit number "W777725001011" is in the list of products for selection
        And I verify that product "E003300" in the unit "W777725001011" is flagged as already irradiated
        And I verify that product "E033600" in the unit "W777725001011" is flagged as not irradiated

    @LAB-615
    Scenario: I cannot add a product that is not configured for irradiation
        Given I have the following inventory products:
            | Unit Number   | Product Code | Status    | Location  |
            | W777725001012 | E0869V00     | AVAILABLE | 123456789 |
        And I'm in the irradiation service at the location "123456789"
        When I scan the unit number "W777725001012" in irradiation
        Then I see the product "E0869V00" from unit number "W777725001012" is in the list of products for selection
        And I verify that product "E0869V00" in the unit "W777725001012" is flagged as not configurable for irradiation

    @LAB-615
    Scenario: I cannot add a product that is currently being irradiated
        Given I have the following inventory products:
            | Unit Number   | Product Code | Status    | Location  |
            | W777725001013 | E003300      | AVAILABLE | 123456789 |
        And I'm in the irradiation service at the location "123456789"
        And the product "E003300" in the unit "W777725001013" was already irradiated in a opened batch for device "AUTO-DEVICE1013"
        When I scan the unit number "W777725001013" in irradiation
        And I verify that product "E003300" in the unit "W777725001013" is flagged as is being irradiated
