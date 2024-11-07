@ui
Feature: Search Orders


    @R20-227
    Rule: I should be able to filter the order lists by specific criteria.
    Rule: I should be able to apply filter criteria.
        Rule: I should be able to search the order by BioPro order number or External Order ID.
    Rule: I should be prevented from selecting other filters when BioPro Order number or External ID is selected.
        Scenario Outline: Search orders by Order Number
            Given I cleaned up from the database the orders with external ID "<External ID>".
            And I cleaned up from the database the orders with external ID "<Order Number>".
            And I have a Biopro Order with id "<Order Number>", externalId "<External ID>", Location Code "<Order LocationCode>", Priority "<Priority>" and Status "<Status>".
            And I have a Biopro Order with id "<External ID>", externalId "<Order Number>", Location Code "<Order LocationCode>", Priority "<Priority>" and Status "<Status>".
            And I am logged in the location "<User LocationCode>".
            And "create date from, create date to, desired shipment date from, desired shipment date to, order status, priority, ship to customer" fields are enabled.
            And "reset, apply" options are enabled.
            And I choose search orders.
            And "create date from, create date to, desired shipment date from, desired shipment date to, order status, priority, ship to customer" fields are disabled.
            And I enter "00000" for the "OrderNumber"
            When I click on apply option
            Then I should see 2 orders in the search results.

            Examples:
                | External ID | Order LocationCode | User LocationCode | Priority | Status | Order Key  | Order Number |
                | 1979        | 123456789          | 123456789         | STAT     | OPEN   | orderId    | 1984         |
                | 1984        | 123456789          | 123456789         | STAT     | OPEN   | externalId | 1979         |


        @R20-227
        Rule: I should not be able to see the orders from a different location.
    Rule: I should be able to view an error message when I search for a non-existent order number.
        Scenario Outline: Search for an order number from a different location
            Given I cleaned up from the database the orders with external ID "<External ID>".
            And I have a Biopro Order with externalId "<External ID>", Location Code "<Order LocationCode>", Priority "<Priority>" and Status "<Status>".
            And I am logged in the location "<User LocationCode>".
            And "reset, apply" options are enabled.
            And I choose search orders.
            When I search the order by "<Search Key>".
            Then I should see a "Caution" message: "No Results Found".

            Examples:
                | External ID     | Order LocationCode | User LocationCode | Priority | Status | Search Key |
                | 114117922233510 | 123456789          | 234567891         | STAT     | OPEN   | externalId |
                | 114117922233510 | 123456789          | 123456789         | STAT     | OPEN   | 000111     |


        @R20-227
        Rule: I should be redirected to the Order Details page if there is only one order in the system that matches the search criteria.
        Scenario Outline: Search for an order and view the details
            Given I cleaned up from the database the orders with external ID "<External ID>".
            And I have a Biopro Order with id "<Order Number>", externalId "<External ID>", Location Code "<Order LocationCode>", Priority "<Priority>" and Status "<Status>".
            And I have an order item with product family "<ProductFamily>", blood type "<BloodType>", quantity <Quantity>, and order item comments "<Item Comments>".
            And I am logged in the location "<User LocationCode>".
            And "reset, apply" options are enabled.
            And "create date from, create date to, desired shipment date from, desired shipment date to, order status, priority, ship to customer" fields are enabled.
            And I choose search orders.
            And "create date from, create date to, desired shipment date from, desired shipment date to, order status, priority, ship to customer" fields are disabled.
            When I search the order by "externalId".
            Then I should be redirected to the order details page.

            Examples:
                | Order Number | External ID | Order LocationCode | User LocationCode | Priority | Status | ProductFamily       | BloodType | Quantity | Item Comments |
                | 2018         | DIS1141179  | 123456789          | 123456789         | STAT     | OPEN   | PLASMA_TRANSFUSABLE | AB        | 3        | Needed asap   |

        @R20-228
        Rule: I should be able to reset the applied filter criteria.
    Rule: The system should not enable the Apply and Reset options until at least one filter criteria is chosen.
        Rule: I should be able to see the following filter options
    Rule: I should be able to see the required filter options
        Scenario: Ensure the reset button clears the specified filter criteria
            Given I am logged in at location "123456789"
            And I choose search orders.
            And I open the search orders filter panel
            And I should see "order number, create date from, create date to, desired shipment date from, desired shipment date to, order status, priority, ship to customer" fields.
            And I should see "create date from, create date to" fields as required.
            And "reset, apply" options are disabled.
            And I enter "00000" for the "OrderNumber"
            And "reset, apply" options are enabled.
            When I click on the reset filter button
            Then The filter information should be empty


        @R20-228
        Rule: I should not be able to use a greater initial date when compared to final date field
        Scenario Outline: Ensure that the date range validation checks for greater initial dates when compared to final dates for range fields
            Given I am logged in at location "123456789"
            And I choose search orders.
            And I open the search orders filter panel
            And I enter "11/31/2024" for the "<Initial Date Field>"
            When I enter "11/30/2024" for the "<Final Date Field>"
            Then The system should display the "Initial date should not be greater than final date" validation message

            Examples:
                | Initial Date Field         | Final Date Field         |
                | create date from           | create date to           |
                | desired shipping date from | desired shipping date to |

        @R20-228
        Rule: I should not be able to select create date parameters values greater than current date
        Scenario: Ensure that the selected dates for create date aren't greater than current date
            Given I am logged in at location "123456789"
            And I choose search orders.
            And I open the search orders filter panel
            When I enter a future date for the "create date from"
            Then The system should display the "Initial date should not be greater than final date" validation message


        @R20-228
        Rule: I should be able to see the other filter options disabled when filtering by either the BioPro Order number or External Order ID.
        Scenario: Check if other fields are disabled when an order number is specified
            Given I am logged in the location "123456789".
            And I choose to search orders.
            And I open the search orders filter panel
            When I enter "00000" for the "OrderNumber"
            Then "create date from, create date to, desired shipment date from, desired shipment date to, order status, priority, ship to customer" fields are disabled.


        @R20-228
        Rule: I should be able to multi-select options for Priority, Status, and Ship to Customer fields.
    Rule: I should be able to see order number disabled when filtering by remaining filter fields.
        Scenario Outline: Check if multiple select inputs are keeping the multiple selection after the user selects the second item
            Given I am logged in the location "123456789".
            And I choose to search orders.
            And I open the search orders filter panel.
            When I select two items from "<Multi Select Field>".
            And "order number" fields is disabled.
            Then Two items should be selected from "<Multi Select Field>"
            Examples:
                | Multi Select Field |
                | priority           |
                | status             |
                | ship to customer   |

        @R20-228
        Rule: I should see the number of fields used to select the filter criteria.
        Scenario: Show the number of used filter parameters for a search
            Given I am logged in the location "123456789".
            And I choose to search orders.
            And I open the search orders filter panel.
            And I select two items from "priority".
            And I select two items from "status".
            When I click on search option.
            Then I should see 2 as the number of used filters for the search.

        @R20-228
        Rule: I should be able to enter the create date manually or select from the integrated component.
        Scenario: Check if the user can enter dates manually and pick the date from a date picker component for all date fields
            Given I am logged in the location "123456789".
            And I choose to search orders.
            And I open the search orders filter panel.
            And I open "create date from" calendar.
            And I select "11/31/2024" date from the "create date from" calendar
            When The "create date from" calendar is not visible
            Then The value for "create date from" should be "11/31/2024"

        @R20-228
        Rule: I should be able to filter the results for date fields from 2 years back.
    Rule: I should not be able to search more than 2 years range.
        Rule: I should not be able to apply filters if any field validations fail.
    Rule: I should be able to implement the field-level validation and display an error message if the validations fail.
        Scenario: Check if the values informed for create date range don't exceed 2 years in the past
            Given I am logged in the location "123456789".
            And I choose to search orders.
            And I open the search orders filter panel.
            When I enter "11/31/2018" for the "create date from".
            Then The system should display the "Create date should not exceed 2 years in the past" validation message.
            And "reset" option is enabled.
            And "apply" option is disabled.
