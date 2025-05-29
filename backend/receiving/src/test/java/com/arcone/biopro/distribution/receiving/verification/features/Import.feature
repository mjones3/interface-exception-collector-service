@AOA-109
Feature: Import products

    Rule: I should be able to input shipping details like product category, transit date and time, temperature, thermometer ID and comments as necessary.
    Rule: The system should show the appropriate fields based on the selected product category.
    @api @DIS-406
    Scenario Outline: Request to enter shipping information
        Given I request to enter shipping data for a "<Temperature Category>" product category.
        Then I should be able to enter information for the following attributes: "<Attributes to Fill>".
        Examples:
            | Temperature Category | Attributes to Fill              |
            | FROZEN               | transitStart, transitEnd, ...   |
            | ROOM_TEMPERATURE     | temperature, thermometerId, ... |
            | REFRIGERATED         | transitStart, transitEnd, ...   |

        Rule: I should be able to input shipping details like product category, transit date and time, temperature, thermometer ID and comments as necessary.
        Rule: The system should show the appropriate fields based on the selected product category.
        @ui @DIS-406
        Scenario Outline: Enter shipping information
            Given I am at the Enter Shipping Information Page.
            When I select to enter information for a "<Temperature Category>" product category.
            Then I should be able to fill the following fields: "<Attributes to Fill>".
            Examples:
                | Temperature Category | Attributes to Fill              |
                | FROZEN               | transitStart, transitEnd, ...   |
                | ROOM_TEMPERATURE     | temperature, thermometerId, ... |
                | REFRIGERATED         | transitStart, transitEnd, ...   |
