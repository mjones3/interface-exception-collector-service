@ui @R20-228
Feature: Search Orders

    Rule: I should be able to filter the order lists by specific criteria.
    Rule: I should be able to apply filter criteria.
    Rule: I should be able to reset the applied filter criteria.
    Rule: The system should not enable the Apply and Reset options until at least one filter criteria is chosen.
        Scenario: Disable Apply and Reset options when no filter criteria is chosen
            Given I am logged in the location "123456789".
            And I choose to search orders.
            When I open the search filter panel.
            Then Apply and Reset options has to be disabled
    Rule: I should be able to see the following filter options
    Rule: I should be prevented from selecting other filters when BioPro Order number or External ID is selected.
    Rule: I should be able to multi-select options for Priority, Status, and Ship to Customer fields.
    Rule: I should see the number of fields used to select the filter criteria.
    Rule: I should be able to enter the create date manually or select from the integrated component.
    Rule: I should be able to filter the results for date fields from 2 years back.
    Rule: I should not be able to search more than 2 years range.
    Rule: I should be able to see the other filter options disabled when filtering by either the BioPro Order number or External Order ID.
    Rule: I should not be able to apply filters if any field validations fail.
    Rule: I should be able to implement the field-level validation and display an error message if the validations fail.
