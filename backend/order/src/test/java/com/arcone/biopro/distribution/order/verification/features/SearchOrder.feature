@ui
Feature: Search Orders

    Background:
        Given I cleaned up from the database the orders with external ID "1979,1984,2018,DIS1141179,114117922233510".


    Rule: I should be able to filter the order lists by specific criteria.
        Rule: I should be able to apply filter criteria.
    Rule: I should be able to search the order by BioPro order number or External Order ID.
        Rule: I should be prevented from selecting other filters when BioPro Order number or External ID is selected.
    Rule: I should be able to see the other filter options disabled when filtering by either the BioPro Order number or External Order ID.
        @R20-227 @R20-228
        Scenario Outline: Search orders by Order Number
            Given I have a Biopro Order with id "<Order Number>", externalId "<External ID>", Location Code "<Order LocationCode>", Priority "<Priority>" and Status "<Status>".
            And I have a Biopro Order with id "<External ID>", externalId "<Order Number>", Location Code "<Order LocationCode>", Priority "<Priority>" and Status "<Status>".
            And I am logged in the location "<User LocationCode>".
            And I choose search orders.
            And I open the search orders filter panel.
            And "order number, create date from, create date to, desired shipment date from, desired shipment date to, order status, priority, ship to customer" fields are "enabled".
            And I search the order by "<Search Key>".
            And "create date from, create date to, desired shipment date from, desired shipment date to, order status, priority, ship to customer" fields are "disabled".
            When I choose "apply" option.
            Then I should see 2 orders in the search results.

            Examples:
                | External ID | Order LocationCode | User LocationCode | Priority | Status | Search Key | Order Number |
                | 1979        | 123456789          | 123456789         | STAT     | OPEN   | 1984       | 1984         |
                | 1984        | 123456789          | 123456789         | STAT     | OPEN   | 1984       | 1979         |



    Rule: I should not be able to see the orders from a different location.
        Rule: I should be able to view an error message when I search for a non-existent order number.
        @R20-227
        Scenario Outline: Search for an order number from a different location
            Given I have a Biopro Order with externalId "<External ID>", Location Code "<Order LocationCode>", Priority "<Priority>" and Status "<Status>".
            And I am logged in the location "<User LocationCode>".
            And I choose search orders.
            And I open the search orders filter panel.
            And I search the order by "<Search Key>".
            When I choose "apply" option.
            Then I should see a "Caution" message: "No Results Found".

            Examples:
                | External ID     | Order LocationCode | User LocationCode | Priority | Status | Search Key |
                | 114117922233510 | 123456789          | 234567891         | STAT     | OPEN   | externalId |
                | 114117922233510 | 123456789          | 123456789         | STAT     | OPEN   | 000111     |



    Rule: I should be redirected to the Order Details page if there is only one order in the system that matches the search criteria.
        @R20-227
        Scenario Outline: Search for an order and view the details
            Given I have a Biopro Order with id "<Order Number>", externalId "<External ID>", Location Code "<Order LocationCode>", Priority "<Priority>" and Status "<Status>".
            And I have an order item with product family "<ProductFamily>", blood type "<BloodType>", quantity <Quantity>, and order item comments "<Item Comments>".
            And I am logged in the location "<User LocationCode>".
            And I choose search orders.
            And I open the search orders filter panel.
            And I search the order by "externalId".
            When I choose "apply" option.
            Then I should be redirected to the order details page.

            Examples:
                | Order Number | External ID | Order LocationCode | User LocationCode | Priority | Status | ProductFamily       | BloodType | Quantity | Item Comments |
                | 2018         | DIS1141179  | 123456789          | 123456789         | STAT     | OPEN   | PLASMA_TRANSFUSABLE | AB        | 3        | Needed asap   |


    Rule: I should be able to reset the applied filter criteria.
        Rule: The system should not enable the Apply and Reset options until at least one filter criteria is chosen.
    Rule: I should be able to see the following filter options
        Rule: I should be able to see the required filter options
        @R20-228
        Scenario: The reset option clears the specified filter criteria
            Given I am logged in the location "123456789".
            And I choose search orders.
            And I open the search orders filter panel.
            And I should see "order number, create date from, create date to, desired shipment date from, desired shipment date to, order status, priority, ship to customer" fields.
            And I should see "create date from, create date to" fields as required.
            And "reset" option is "disabled".
            And "apply" option is "disabled".
            And I search the order by "00000".
            And "apply" option is "enabled".
            And "reset" option is "enabled".
            When I choose "apply" option.
            And I choose "reset" option.
            Then The filter information should be empty.



    Rule: I should not be able to use a greater initial date when compared to final date field
        @R20-228
        Scenario Outline: Ensure that the date range validation checks for greater initial dates when compared to final dates for range fields
            Given I am logged in the location "123456789".
            And I choose search orders.
            And I open the search orders filter panel.
            When I enter the date: "12/31/2023" for the field "<Initial Date Field>" and the date: "12/30/2023"  for the field "<Final Date Field>".
            Then I should see a validation message: "Initial date should not be greater than final date".

            Examples:
                | Initial Date Field         | Final Date Field         |
                | create date from           | create date to           |
                | desired shipping date from | desired shipping date to |


    Rule: I should not be able to select create date parameters values greater than current date
        @R20-228
        Scenario: Ensure that the selected dates for create date aren't greater than current date
            Given I am logged in the location "123456789".
            And I choose search orders.
            And I open the search orders filter panel.
            When I enter a future date for the field "create date from".
            Then I should see a validation message: "From date should not be greater than to date".



    Rule: I should be able to multi-select options for Priority, Status, and Ship to Customer fields.
        Rule: I should be able to see order number disabled when filtering by remaining filter fields.
    Rule: I should see the number of fields used to select the filter criteria.
        @R20-228
        Scenario Outline: Check if multiple select inputs are keeping the multiple selection after the user selects the second item
            Given I have a Biopro Order with id "123", externalId "1979", Location Code "123456789", Priority "STAT" and Status "OPEN".
            And I have a Biopro Order with id "456", externalId "1984", Location Code "123456789", Priority "STAT" and Status "CLOSED".
            And I have a Biopro Order with id "789", externalId "2018", Location Code "123456789", Priority "DIFF" and Status "OPEN".
            And I am logged in the location "123456789".
            And I choose search orders.
            And I open the search orders filter panel.
            When I select "<Selected Priorities>" for the "priority".
            And I select "<Selected Statuses>" for the "order status".
            And I select "<Selected Customers>" for the "ship to customer".
            And "order number" field is "disabled".
            Then Items "<Selected Priorities>" should be selected for "priority".
            And Items "<Selected Statuses>" should be selected for "order status".
            And Items "<Selected Customers>" should be selected for "ship to customer".
            And I should see "<Expected External Ids>" orders in the search results.
            And I should not see "<Not Returned External Ids>".
            And I should see "<Expected Number of Filters>" as the number of used filters for the search.
            Examples:
                | Selected Priorities | Selected Statuses | Selected Customers | Expected External Ids | Not Returned External Ids | Expected Number of Filters |
                | STAT, STAT2         | OPEN,OPEN2        |                    | 1979,1984             | 2018                      | 2                          |
                | STAT, STAT2         |                   |                    | 1979,2018             | 1984                      | 1                          |
                | STAT, STAT2         | OPEN,DIFF         | 1,2,3              |                       |                           | 3                          |

    Rule: I should be able to filter the results for date fields from 2 years back.
        Rule: I should be able to enter the create date manually or select from the integrated component.
    Rule: I should not be able to search more than 2 years range.
        Rule: I should not be able to apply filters if any field validations fail.
    Rule: I should be able to implement the field-level validation and display an error message if the validations fail.
        @R20-228
        Scenario: Check if the values informed for create date range don't exceed 2 years in the past
            Given I am logged in the location "123456789".
            And I choose search orders.
            And I open the search orders filter panel.
            When I enter a past date: "11/31/2018" for the field "create date from".
            Then I should see a validation message: "From date should not exceed 2 years in the past".
            And "reset" option is "enabled".
            And "apply" option is "disabled".
