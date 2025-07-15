# Feature Unit Number reference: W777725002xxx

@ui @LAB-576 @AOA-61
Feature: Starts Irradiation Process

    Rule: As a distribution specialist, I want to add products into an irradiation batch so that I can start the irradiation process
        @LAB-576 @LAB-575 @LAB-574
        Scenario Outline: I can start an irradiation batch.
            Given I have a device "<Blood Center Id>" at location "<Location>" with status "ACTIVE"
            And I login to Distribution module
            And I select the location "MDL Hub 1"

            When I navigate to "Start Irradiation" in "Irradiation"
            Then I verify that I am taken to the page "Start Irradiation" in "Irradiation"
            Then I verify that the "Lot Number" field is "enabled"
            Then I verify that the "Irradiator Id" field is "enabled"
            Then I verify that the "Unit Number" field is "disabled"
            And I verify that I am "Unable" to "Submit"

            When I scan the irradiator id "<Blood Center Id>"
            Then I verify that the "Unit Number" field is "disabled"
            And I verify that the "Irradiator Id" field is "disabled"
            And I verify that I am "Unable" to "Submit"

            When I scan the lot number "<Lot Number 1>"
            Then I verify that the "Unit Number" field is "enabled"
            And I verify that the "Irradiator Id" field is "disabled"
            And I verify that I am "Unable" to "Submit"

            When I scan the unit number "=<Unit Number 1>00" in the irradiation page
            Then I verify the product "<Product Code 1>" is displayed for selection

            When I select the product "<Product Code 1>"
            Then I verify that the unit number "<Unit Number 1>" with product "<Description 1>" was added to the batch
            And I verify that I am "Able" to "Submit"
            And I verify that the "Irradiator Id" field is "disabled"

            When I scan the lot number "<Lot Number 2>"
            And I scan the unit number "=<Unit Number 2>00" in the irradiation page
            When I select the product "<Product Code 2>"
            Then I verify that the unit number "<Unit Number 2>" with product "<Description 2>" was added to the batch

            And I choose to "Submit"
            And I see the "Success" message "Start irradiation successfully complete"

            Examples:
                | Unit Number 1 | Product Code 1 | Description 1 | Unit Number 2 | Product Code 2 | Description 2 |Blood Center Id | Location  | Lot Number 1 | Lot Number 2 |
                | W777725002001 | E033600        |               | W777725002002 | E068600        |                |IRRAD0123       | 123456789 | Lot1234      | Lot5678      |

        @LAB-615 @disabled
        Scenario Outline: I should be notified if the product selected has a Quarantine that stops manufacturing
            Given I have a device "<Blood Center Id>" at location "<Location>" with status "ACTIVE"
            And I login to Distribution module
            And I select the location "MDL Hub 1"
            And I navigate to "Start Irradiation" in "Irradiation"
            And I scan the irradiator id "<Blood Center Id>"
            And I scan the lot number "<Lot Number 1>"

            When I scan the unit number "=<Unit Number 1>00" in the irradiation page
            # TO DO: how can we set a quarantine with stops manufacturing ????
            And I select the product "<Product Code 1>"

            Then I see the "Warning" message "This unit has been quarantined and manufacturing cannot be completed"

            Examples:
                | Unit Number 1 | Product Code 1 | Blood Center Id | Location  | Lot Number 1 |
                | W777725002001 | E033600        | IRRAD0123       | 123456789 | Lot1234      |

        @LAB-615 @disabled
        Scenario Outline: I should see an acknowledgement message if the product selected is an Unsuitable Product.
            Given I have a device "<Blood Center Id>" at location "<Location>" with status "ACTIVE"
            And I login to Distribution module
            And I select the location "MDL Hub 1"
            And I navigate to "Start Irradiation" in "Irradiation"
            And I scan the irradiator id "<Blood Center Id>"
            And I scan the lot number "<Lot Number 1>"

            When I scan the unit number "=<Unit Number 1>00" in the irradiation page
            # TO DO: how can we set the product as unsuitable ????
            And I select the product "<Product Code 1>"

            Then I see the confirmation message with title "Discarded" and message "This product is unsuitable with the reason Positive Reactive Test Results. Place in biohazard container"
            And I confirm the confirmation message

            Examples:
                | Unit Number 1 | Product Code 1 | Blood Center Id | Location  | Lot Number 1 |
                | W777725002001 | E033600        | IRRAD0123       | 123456789 | Lot1234      |
