# Feature Unit Number reference: W777725001xxx

@api
Feature: Scan Unit Number for Irradiation

    @LAB-576 @AOA-61
    Scenario: I successfully scan a unit number with a product eligible for irradiation
        Given I have the following products:
            | Unit Number   | Product Code | Product Family               |
            | W777725001001 | E033600      | WHOLE_BLOOD                  |
        When I scan the unit number "W777725001001" in irradiation
        Then I see the product "E033600" from unit number "W777725001001" is in the list of products for selection
        When I select the product "E033600" from unit number "W777725001001"
        Then I see the "success" message "Product was added in the batch"
