@api @DIS-88
Feature: Customer validation

    Scenario Outline: Validate an existent customer

        Given I have a customer which code is "C3457".
        When I search for the customer by code.
        Then I should see the customer name "<Customer Name>".
        And I should see the customer active flag as "<Active>".
        And I should have an address of type "BILLING".
        And I should have an address of type "SHIPPING".
        And The attributes "<Required Fields>" are not empty.

        Examples:
            | Customer Name           | Active | Required Fields                   |
            | Pioneer Health Services | Y      | externalId, name, code, addresses |

    Scenario Outline: Search for a non-existent customer

        Given I have a customer which code is "Z0000".
        When I search for the customer by code.
        Then I should see a message "<Message>".

        Examples:
            | Message          |
            | No Results Found |
