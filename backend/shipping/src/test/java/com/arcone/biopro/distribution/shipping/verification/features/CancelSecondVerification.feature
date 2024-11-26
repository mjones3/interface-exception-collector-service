@AOA-40
Feature: Cancel Verification Second Verification of Units Feature
    As a DT,
    I should be able to cancel the second verification process,
    So that I can stop the verification process.

    Background:
        Given I cleaned up from the database the packed item that used the unit number "W825530106087,W825530106088,W825530106089,W825530106090,W825530106091".
        And I cleaned up from the database, all shipments with order number "125,126,127,128,129".

        Rule: I should be able to cancel the second verification process (at any time) before submitting.
        Rule: I should see a confirmation message stating that all verified products at the current step will be removed.
        Rule: I should be able to abort the cancellation process and resume the second verification.
        Rule: I should not see any verified products when I confirm the cancellation.
        Rule: I should return to the shipment details page when I confirm the cancellation and will receive a message that the cancellation was successful.
        @ui @DIS-205
        Scenario Outline: Cancel Second verification verified units.
            Given I have a shipment for order "<Order Number>" with the unit "<UN>" and product code "<Code>" "verified".
            And The second verification configuration is "enabled".
            And I am on the verify products page.
            When I choose to cancel the second verification process.
            Then I should see a confirmation dialog with the message "When cancelling, all verified products will be removed. Are you sure you want to cancel and remove all the products?".
            When I choose to cancel the confirmation.
            Then The confirmation dialog should be closed.
            And The verified units should remain in the verified products table.
            And The complete shipment option should be enabled.
            When I choose to cancel the second verification process.
            Then I should see a confirmation dialog with the message "When cancelling, all verified products will be removed. Are you sure you want to cancel and remove all the products?".
            When I confirm the cancellation.
            Then The confirmation dialog should be closed.
            And I should not have any verified product in the shipment.
            And I should be redirected to the shipment details page.
            And I should see a "Success" message: "The second verification cancellation completed".
            And The complete shipment option should not be enabled.
            Examples:
                | Order Number | Code     | UN            |
                | 125          | E0685V00 | W825530106087 |

        Rule: I should be able to cancel the second verification process (at any time) before submitting.
        @ui @DIS-205
        Scenario Outline: Cancel Second verification when there's no verified units.
            Given I have a shipment for order "<Order Number>" with the unit "<UN>" and product code "<Code>" "packed".
            And The second verification configuration is "enabled".
            And I am on the verify products page.
            When I choose to cancel the second verification process.
            And I should be redirected to the shipment details page.
            And I should see a "Success" message: "The second verification cancellation completed".
            And The complete shipment option should not be enabled.
            Examples:
                | Order Number | Code     | UN            |
                | 126          | E0685V00 | W825530106088 |

        @api @DIS-205
        Scenario Outline: Cancel Second verification when there are ineligible products to be removed.
            Given I have a shipment for order "<Order Number>" with the unit "<UN>" and product code "<Code>" "packed".
            And The verified unit "<UN>" is unsuitable with status "Discarded" and message "This product has already been discarded for BROKEN in the system. Place in biohazard container.".
            And The second verification configuration is "enabled".
            When I request to cancel the second verification process.
            Then I should receive status "400 BAD_REQUEST" with type "WARN" and message "Second Verification cannot be cancelled because contains product(s) that should be removed from the shipment.".
            Examples:
                | Order Number | Code     | UN            |
                | 127          | E0685V00 | W825530106089 |

        @api @DIS-205
        Scenario Outline: Cancel Second verification when there's no verified units (API).
            Given I have a shipment for order "<Order Number>" with the unit "<UN>" and product code "<Code>" "packed".
            And The second verification configuration is "enabled".
            When I request to cancel the second verification process.
            Then I should receive status "200 OK" with type "SUCCESS" and message "The second verification cancellation completed".
            And I should receive a redirect address to "Shipment Details Page".
            Examples:
                | Order Number | Code     | UN            |
                | 128          | E0685V00 | W825530106090 |

        @api @DIS-205
        Scenario Outline: Cancel Second verification verified units (API).
            Given I have a shipment for order "<Order Number>" with the unit "<UN>" and product code "<Code>" "verified".
            And The second verification configuration is "enabled".
            When I request to cancel the second verification process.
            Then I should receive status "200 OK" with type "CONFIRMATION" and message "When cancelling all scanned products will be removed, are you sure you want to cancel and remove the products?".
            Examples:
                | Order Number | Code     | UN            |
                | 129          | E0685V00 | W825530106091 |

        @api @DIS-205
        Scenario Outline: Confirm cancellation of Second verification verified units.
            Given I have a shipment for order "<Order Number>" with the unit "<UN>" and product code "<Code>" "verified".
            And The second verification configuration is "enabled".
            When I request to confirm the cancellation.
            Then I should receive status "200 OK" with type "SUCCESS" and message "The second verification cancellation completed".
            And I should receive a redirect address to "Shipment Details Page".
            Examples:
                | Order Number | Code     | UN            |
                | 129          | E0685V00 | W825530106091 |
