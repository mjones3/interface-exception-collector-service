# Feature Unit Number reference: W777725004xxx

@disabled
@ui @AOA-61 @hzd9
Feature: Close Irradiation Process

    Rule: As a distribution specialist, I want to indicate irradiation status for each unit of the batch so that I can close the irradiation process
        @LAB-622
        Scenario Outline: I can close an irradiation batch.
            Given the product "<Product Code 1>" in the unit "<Unit Number>" was already irradiated in a opened batch for device "<Blood Center Id>"
            And the product "<Product Code 2>" in the unit "<Unit Number>" was already irradiated in a opened batch for device "<Blood Center Id>"
            And I login to Distribution module
            And I select the location "MDL Hub 1"

            When I navigate to "Close Irradiation" in "Irradiation"
            Then I verify that I am taken to the page "Close Irradiation" in "Irradiation"
            And On the "Close Irradiation" page, I verify that the "Irradiator Id" field is "enabled"
            And On the "Close Irradiation" page, I verify that the "Unit Number" field is "disabled"
            And I verify that I am "Unable" to "Submit"

            When On the "Close Irradiation" page, I scan the irradiator id "<Blood Center Id>"
            Then On the "Close Irradiation" page, I verify that the "Unit Number" field is "enabled"
            And On the "Close Irradiation" page, I verify that the "Irradiator Id" field is "disabled"
            And I verify that I am "Unable" to "Submit"

            When On the "Close Irradiation" page, I scan the unit number "=<Unit Number 1>00"
            Then I verify the product "<Product Code 1>" is displayed for selection

            When I select the product "<Product Code 1>"
            Then On the "Close Irradiation" page, I verify that the unit number "<Unit Number 1>" with product "<Description 1>" was added to the batch
            And I verify that I am "Able" to "Submit"
            And On the "Close Irradiation" page, I verify that the "Irradiator Id" field is "disabled"

            When On the "Close Irradiation" page, I scan the unit number "=<Unit Number 2>00"
            When I select the product "<Product Code 2>"
            Then On the "Close Irradiation" page, I verify that the unit number "<Unit Number 2>" with product "<Description 2>" was added to the batch

            And I choose to "Submit"
            And I see the "Success" message "Batch submitted successfully"

            Examples:
                | Unit Number 1 | Product Code 1 | Description 1 | Product Code 2 | Description 2     | Blood Center Id | Location  |
                | W777725004001 | E033600        | AS1 LR RBC    | E068600        | APH AS3 LR RBC C2 | AUTO-IRRAD001   | 123456789 |

