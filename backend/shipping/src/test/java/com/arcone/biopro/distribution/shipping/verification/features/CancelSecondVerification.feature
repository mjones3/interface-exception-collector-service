@ui @AOA-40
Feature: Cancel Verification Second Verification of Units Feature
    As a DT,
    I should be able to cancel the second verification process,
    So that I can stop the verification process.

    Background:
        Given I cleaned up from the database the packed item that used the unit number "W825530106087".
        And I cleaned up from the database, all shipments with order number "125".


        Rule: I should be able to cancel the second verification process (at any time) before submitting.
        Rule: I should see a confirmation message stating that all verified products at the current step will be removed.
        Rule: I should be able to abort the cancellation process and resume the second verification.
        Rule: I should not see any verified products when I confirm the cancellation.
        Rule: I should return to the shipment details page when I confirm the cancellation and will receive a message that the cancellation was successful.

        ## Pending Questions
            ### Should we show the confirmation dialog even when there is no verified products ?
            # Should we allow cancel only after all bad products be removed? in case the user tries to cancel show an error message, if yes a new AC should be included.
            # Should we show cancel option on both tabs ?

        @ui @DIS-205
        Scenario Outline: Cancel Second verification verified units.
            Given I have a shipment for order "<Order Number>" with the unit "<UN>" and product code "<Code>" "verified".
            And The second verification configuration is "enabled".
            And I am on the verify products page.
            When I choose to cancel the second verification process.
            Then I should see a confirmation dialog with the message "When cancelling all verified products will be removed, are you sure you want to cancel and remove all products?".
            When I choose to cancel the confirmation.
            Then The confirmation dialog should be closed.
            And The verified units should remain in the verified products table.
            And The complete shipment option should be enabled.
            When I choose to cancel the second verification process.
            Then I should see a confirmation dialog with the message "When cancelling all verified products will be removed, are you sure you want to cancel and remove all products?".
            When I confirm the cancellation.
            Then The confirmation dialog should be closed.
            And I should not have any verified product in the shipment.
            And I should be redirected to the shipment details page.
            And I should see a "Success" message: "Cancellation Completed".

            And The complete shipment option should not be enabled.
            Examples:
                | Order Number | Code     | UN            |
                | 125          | E0685V00 | W825530106087 |
