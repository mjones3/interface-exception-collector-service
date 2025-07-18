@api @LAB-614 @AOA-61
Feature: Lot Number Validation
    As a distribution specialist
    I want to validate lot numbers through the supply service
    So that I can ensure lot numbers are valid before processing

    Scenario: Lot number validation succeeds
        Given I have a valid lot number "LII1" and supply type "IRRADIATION_INDICATOR"
        When I call the supply service to validate the lot number
        Then the supply service should return true
        And I should receive a notification message "Validation success"

    Scenario Outline: Lot number validation fails for invalid combinations
        Given I have a lot number "<lotNumber>" and supply type "<supplyType>"
        When I call the supply service to validate the lot number
        Then the supply service should return false
        And I should receive an error message "Lot number validation failed"

        Examples:
            | lotNumber   | supplyType            |
            | INVALID_LOT | INVALID_TYPE          |
            | LII1        | INVALID_TYPE          |
            | INVALID_LOT | IRRADIATION_INDICATOR |
