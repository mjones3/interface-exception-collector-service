# Feature Unit Number reference: W777725004xxx

@disabled
@ui @AOA-61 @hzd9
Feature: Close Irradiation Batch

    Rule: As a distribution specialist, I want to indicate irradiation status for each unit of the batch so that I can close the irradiation process
        @LAB-621 @LAB-622
        Scenario Outline: I can close an irradiation batch.
            Given An irradiation batch has been started with the following units for irradiator "<Irradiator Id>"
                | Unit Number     | Product Code     | Lot Number |
                | <Unit Number 1> | <Product Code 1> | Lot 123    |
                | <Unit Number 1> | <Product Code 2> | Lot 1234   |
                | <Unit Number 2> | <Product Code 1> | Lot 12345  |
            And I login to Distribution module
            And I select the location "MDL Hub 1"

            When I navigate to "Close Irradiation" in "Irradiation"
            Then I verify that I am taken to the page "Close Irradiation" in "Irradiation"
            And On the "Close Irradiation" page, I verify that the "Irradiator Id" field is "enabled"
            And On the "Close Irradiation" page, I verify that the "Unit Number" field is "disabled"
            And I verify that I am "Unable" to "Submit"

            When On the "Close Irradiation" page, I scan the irradiator id "<Irradiator Id>"
            Then On the "Close Irradiation" page, I verify that the "Unit Number" field is "enabled"
            And On the "Close Irradiation" page, I verify that the "Irradiator Id" field is "disabled"
            And I verify that I am "Unable" to "Submit"
            And On the "Close Irradiation" page, I verify that the unit number "<Unit Number 1>" with product "<Description 1>" was added to the batch
            And On the "Close Irradiation" page, I verify that the unit number "<Unit Number 1>" with product "<Description 2>" was added to the batch
            And On the "Close Irradiation" page, I verify that the unit number "<Unit Number 2>" with product "<Description 1>" was added to the batch

            When On the "Close Irradiation" page, I scan the unit number "=<Unit Number 1>00"
            And I select the product "<Product Code 1>"
            And On the "Close Irradiation" page, I scan the unit number "=<Unit Number 1>00"
            And I select the product "<Product Code 2>"
            And On the "Close Irradiation" page, I scan the unit number "=<Unit Number 2>00"
            And I select the product "<Product Code 1>"
            Then I verify that I am "Able" to "Record Inspection"

            When On the Close Irradiation page, I click to select all units in the batch
            And On the Close Irradiation page, I click on Record Inspection
            And On the Record Inspection window, I verify that Record Inspection window is displayed
            And On the Record Inspection window, I select Irradiated status
            And On the Record Inspection window, I click on Submit
            And I choose to "Submit"
            Then I see the "Success" message "Batch submitted successfully"

            Examples:
                | Unit Number 1 | Product Code 1 | Description 1 | Product Code 2 | Description 2     | Irradiator Id | Unit Number 2 |
                | W777725004001 | E033600        | AS1 LR RBC    | E068600        | APH AS3 LR RBC C2 | AUTO-IRRAD001 | W777725004002 |


    Rule: As a distribution specialist, I want to see a quarantine message for products entered as not irradiated
        @LAB-621 @LAB-622
        Scenario Outline: I see a quarantine message if product was set as not irradiated.
            Given An irradiation batch has been started with the following units for irradiator "<Irradiator Id>"
                | Unit Number     | Product Code     | Lot Number |
                | <Unit Number 1> | <Product Code 1> | Lot 456    |
                | <Unit Number 1> | <Product Code 2> | Lot 4567   |
                | <Unit Number 2> | <Product Code 1> | Lot 45678  |
            And I login to Distribution module
            And I select the location "MDL Hub 1"

            When I navigate to "Close Irradiation" in "Irradiation"
            And On the "Close Irradiation" page, I scan the irradiator id "<Irradiator Id>"
            And On the "Close Irradiation" page, I verify that the unit number "<Unit Number 1>" with product "<Description 1>" was added to the batch
            And On the "Close Irradiation" page, I verify that the unit number "<Unit Number 1>" with product "<Description 2>" was added to the batch
            And On the "Close Irradiation" page, I verify that the unit number "<Unit Number 2>" with product "<Description 1>" was added to the batch

            When On the "Close Irradiation" page, I scan the unit number "=<Unit Number 1>00"
            And I select the product "<Product Code 1>"
            And On the "Close Irradiation" page, I scan the unit number "=<Unit Number 1>00"
            And I select the product "<Product Code 2>"
            And On the "Close Irradiation" page, I scan the unit number "=<Unit Number 2>00"
            And I select the product "<Product Code 1>"
            Then I verify that I am "Able" to "Record Inspection"

            When On the Close Irradiation page, I click to select all units in the batch
            And On the Close Irradiation page, I click on Record Inspection
            Then On the Record Inspection window, I verify that Record Inspection window is displayed

            When On the Record Inspection window, I select Not Irradiated status
            And On the Record Inspection window, I click on Submit
            And I see the "Warning" message "Units will be quarantined"
            And I choose to "Submit"
            Then I see the "Success" message "Batch submitted successfully"

            Examples:
                | Unit Number 1 | Product Code 1 | Description 1 | Product Code 2 | Description 2     | Irradiator Id | Unit Number 2 |
                | W777725004001 | E033600        | AS1 LR RBC    | E068600        | APH AS3 LR RBC C2 | AUTO-IRRAD002 | W777725004002 |

        @LAB-621 @LAB-622
        Scenario Outline: I see quarantine message if the batch has a mix of irradiated and non-irradiated products.
            Given An irradiation batch has been started with the following units for irradiator "<Irradiator Id>"
                | Unit Number     | Product Code     | Lot Number |
                | <Unit Number 1> | <Product Code 1> | Lot 4567   |
                | <Unit Number 2> | <Product Code 2> | Lot 45678  |
            And I login to Distribution module
            And I select the location "MDL Hub 1"

            When I navigate to "Close Irradiation" in "Irradiation"
            And On the "Close Irradiation" page, I scan the irradiator id "<Irradiator Id>"
            And On the "Close Irradiation" page, I verify that the unit number "<Unit Number 1>" with product "<Description 1>" was added to the batch
            And On the "Close Irradiation" page, I verify that the unit number "<Unit Number 2>" with product "<Description 2>" was added to the batch

            When On the "Close Irradiation" page, I scan the unit number "=<Unit Number 1>00"
            And I select the product "<Product Code 1>"
            And On the "Close Irradiation" page, I scan the unit number "=<Unit Number 2>00"
            And I select the product "<Product Code 2>"
            Then I verify that I am "Able" to "Record Inspection"

            When On the "Close Irradiation" page, I select the card for unit "<Unit Number 1>" and product "<Description 1>" in the batch
            And On the Close Irradiation page, I click on Record Inspection
            And On the Record Inspection window, I verify that Record Inspection window is displayed
            And On the Record Inspection window, I select Irradiated status
            And On the Record Inspection window, I click on Submit

            When On the "Close Irradiation" page, I select the card for unit "<Unit Number 2>" and product "<Description 1>" in the batch
            And On the Close Irradiation page, I click on Record Inspection
            Then On the Record Inspection window, I verify that Record Inspection window is displayed

            When On the Record Inspection window, I select Not Irradiated status
            And On the Record Inspection window, I click on Submit
            And I see the "Warning" message "Units will be quarantined"
            And I choose to "Submit"
            Then I see the "Success" message "Batch submitted successfully"

            Examples:
                | Unit Number 1 | Product Code 1 | Description 1 | Product Code 2 | Description 2     | Irradiator Id | Unit Number 2 |
                | W777725004001 | E033600        | AS1 LR RBC    | E068600        | APH AS3 LR RBC C2 | AUTO-IRRAD003 | W777725004002 |
