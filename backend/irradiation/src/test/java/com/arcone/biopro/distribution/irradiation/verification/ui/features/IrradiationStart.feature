# Feature Unit Number reference: W777725002xxx

@ui @LAB-576 @AOA-61 @disabled
Feature: Starts Irradiation Process

    Rule: As a distribution specialist, I want to add products into an irradiation batch so that I can start the irradiation process
        @LAB-576 @LAB-575 @LAB-574
        Scenario Outline: I can start an irradiation batch.
            Given I have a device "<Blood Center Id>" at location "<Location>" with status "ACTIVE"
            And I login to Distribution module
            And I select the location "MDL Hub 1"

            When I navigate to "Start Irradiation" in "Irradiation"
            Then I verify that I am taken to the page "Start Irradiation" in "Irradiation"
            And I verify that the "Lot Number" field is "disabled"
            And I verify that the "Unit Number" field is "disabled"
            And I verify that I am "Unable" to "Submit"

            When I scan the irradiator id "<Blood Center Id>"
            Then I verify that the "Lot Number" field is "enabled"
            And I verify that the "Irradiator Id" field is "disabled"
            And I verify that I am "Unable" to "Submit"

            When I scan the lot number "<Lot Number 1>"
            Then I verify that the "Unit Number" field is "enabled"

            # this validation needs to be confirmed - not sure if Lot Number field is going to be disabled or not
            And I verify that the "Lot Number" field is "disabled"
            And I verify that the "Irradiator Id" field is "disabled"
            And I verify that I am "Unable" to "Submit"

            When I scan the unit number "=<Unit Number 1>00" in the irradiation page
            Then I verify the product "<Product Code 1>" is displayed for selection

            When I select the product "<Product Code>"
            Then I verify that the unit number "<Unit Number 1>" with product "<Product Code>" was added to the batch
            And I verify that I am "Able" to "Submit"
            And I verify that the "Irradiator Id" field is "disabled"

            When I scan the lot number "<Lot Number 2>"
            And I scan the unit number "=<Unit Number 2>00" in the irradiation page
            When I select the product "<Product Code 2>"
            Then I verify that the unit number "<Unit Number 2>" with product "<Product Code 2>" was added to the batch

            And I choose to "Submit"
            And I see the "Success" message "Start irradiation successfully complete"

            Examples:
                | Unit Number 1 | Product Code 1 | Unit Number 2 | Product Code 2 | Blood Center Id | Category   | Location  | Lot Number 1 | Lot Number 2 |
                | W777725002001 | E033600        | W777725002002 | E068600        | IRRAD0123       | IRRADIATOR | 123456789 | Lot1234      | Lot5678      |
