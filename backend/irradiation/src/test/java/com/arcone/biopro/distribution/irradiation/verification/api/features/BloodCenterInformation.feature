# Feature Unit Number reference: W777725006000

@api @LAB-630 @AOA-109 @HZD17
Feature: Blood Center Information for Imported Products

    As a lab technician
    I want to submit blood center information for imported units
    So that regulatory compliance is maintained for imported blood products

    Background:
        Given an active device "AUTO-DEVICE100" exists at location "123456789"

    Scenario: Submit batch with complete blood center information for imported units
        Given I prepare a batch with imported units:
            | Unit Number   | Product Code |
            | W777725006001 | E003300      |
            | W777725006002 | E003300      |
        When I submit the batch with blood center information:
            | Unit Number   | Blood Center Name    | Address        | Registration Number | License Number |
            | W777725006001 | Red Cross Center     | Washington DC  | REG001              | LIC001         |
            | W777725006002 | Community Blood Bank | Minneapolis MN | REG002              | LIC002         |

        Then the batch is created successfully
        And each unit contains the provided blood center information
        And I receive confirmation "Batch submitted successfully"

    Scenario: Submit batch with mixed imported and domestic units
        Given I prepare a batch with mixed units:
            | Unit Number   | Product Code | Import Status |
            | W777725006001 | E003300      | true          |
            | W777725006003 | E003300      | false         |
        When I submit the batch with blood center information for imported units only:
            | Unit Number   | Blood Center Name | Address       | Registration Number | License Number |
            | W777725006001 | Red Cross Center  | Washington DC | REG001              | LIC001         |
        Then the batch is created successfully
        And unit "W777725006001" contains blood center information
        And unit "W777725006003" has no blood center information
        And I receive confirmation "Batch submitted successfully"

    Scenario: Reject batch when blood center information is missing for imported units
        Given I prepare a batch with imported unit "W777725006004"
        When I submit the batch without blood center information
        Then the batch submission is rejected
        And I receive error "Blood center information is missing for imported product"

    Scenario: Reject batch when blood center information is incomplete
        Given I prepare a batch with imported unit "W777725006005"
        When I submit the batch with incomplete blood center information:
            | Unit Number   | Blood Center Name | Address   | Registration Number | License Number |
            | W777725004003 | Red Cross Center  | Dallas TX |                     | LIC003         |
        Then the batch submission is rejected
        And I receive error "Blood center information is missing for imported product"
