# Feature Unit Number reference: W777725003xxx

@api @LAB-575
Feature: Submit Batch for Irradiation

    Scenario: I successfully submit a batch with valid parameters
        Given I have a device "AUTO-DEVICE100" at location "123456789" with status "ACTIVE"
        And I have a batch submission request with device "AUTO-DEVICE100", start time "2024-01-15T10:00:00Z" and products below:
            | Unit Number   | Product Code | Irradiator Indicator |
            | W777725003001 | E0102V00     | 123                  |
            | W777725003002 | E010200      | 234                  |
        When I submit the batch for irradiation
        Then the batch should be successfully created in the repository with items:
            | Unit Number   | Product Code | Irradiator Indicator |
            | W777725003001 | E0102V00     | 123                  |
            | W777725003002 | E010200      | 234                  |
        And I should see the success message "Batch submitted successfully"
