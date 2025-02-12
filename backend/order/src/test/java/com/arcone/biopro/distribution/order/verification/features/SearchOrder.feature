@ui @AOA-39
Feature: Search Orders

    Background:
        Given I cleaned up from the database the orders with external ID starting with "EXTSEARCH1".
        And I cleaned up from the database the orders with external ID starting with "29402".


    Rule: I should be able to filter the order lists by specific criteria.
        Rule: I should be able to apply filter criteria.
    Rule: I should be able to search the order by BioPro order number or External Order ID.
        Rule: I should be prevented from selecting other filters when BioPro Order number or External ID is selected.
    Rule: I should be able to see the other filter options disabled when filtering by either the BioPro Order number or External Order ID.
        @R20-227 @R20-228
        Scenario Outline: Search orders by Order Number
            Given I have a Biopro Order with externalId "<External ID>", Location Code "<Order LocationCode>", Priority "<Priority>" and Status "<Status>".
            And I have another Biopro Order with the externalId equals to order number of the previous order.
            And I am logged in the location "<User LocationCode>".
            And I choose search orders.
            And I open the search orders filter panel.
            And "order number, create date from, create date to, desired shipment date from, desired shipment date to, order status, priority, ship to customer" fields are "enabled".
            And I search the order by "<Search Key>".
            And "create date from, create date to, desired shipment date from, desired shipment date to, order status, priority, ship to customer" fields are "disabled".
            When I choose "apply" option.
            Then I should see 2 orders in the search results.

            Examples:
                | External ID   | Order LocationCode | User LocationCode | Priority | Status | Search Key |
                | EXTSEARCH1979 | 123456789          | 123456789         | STAT     | OPEN   | orderId    |
                | EXTSEARCH1984 | 123456789          | 123456789         | STAT     | OPEN   | orderId    |



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
                | External ID              | Order LocationCode | User LocationCode | Priority | Status | Search Key |
                | EXTSEARCH114117922233510 | 123456789          | 234567891         | STAT     | OPEN   | externalId |
                | EXTSEARCH114117922233510 | 123456789          | 123456788         | STAT     | OPEN   | 000111     |



    Rule: I should be redirected to the Order Details page if there is only one order in the system that matches the search criteria.
        @R20-227
        Scenario Outline: Search for an order and view the details
            Given I have a Biopro Order with externalId "<External ID>", Location Code "<Order LocationCode>", Priority "<Priority>" and Status "<Status>".
            And I have an order item with product family "<ProductFamily>", blood type "<BloodType>", quantity <Quantity>, and order item comments "<Item Comments>".
            And I am logged in the location "<User LocationCode>".
            And I choose search orders.
            And I open the search orders filter panel.
            And I search the order by "externalId".
            When I choose "apply" option.
            Then I should be redirected to the order details page.

            Examples:
                | External ID          | Order LocationCode | User LocationCode | Priority | Status | ProductFamily       | BloodType | Quantity | Item Comments |
                | EXTSEARCH1DIS1141179 | 123456789          | 123456789         | STAT     | OPEN   | PLASMA_TRANSFUSABLE | AB        | 3        | Needed asap   |


    Rule: I should be able to reset the applied filter criteria.
        Rule: The system should not enable the Apply and Reset options until at least one filter criteria is chosen.
    Rule: I should be able to see the following filter options
        @R20-228
        Scenario: The reset option clears the specified filter criteria
            Given I am logged in the location "123456789".
            And I choose search orders.
            And I open the search orders filter panel.
            And I should see "order number, create date from, create date to, desired shipment date from, desired shipment date to, order status, priority, ship to customer" fields.
            And "reset" option is "disabled".
            And "apply" option is "disabled".
            And I search the order by "00000".
            And "apply" option is "enabled".
            And "reset" option is "enabled".
            When I choose "reset" option.
            Then The filter information should be empty.



    Rule: I should not be able to use a greater initial date when compared to final date field
        Rule: I should be able to see the required filter options
        @R20-228
        Scenario Outline: Ensure that the date range validation checks for greater initial dates when compared to final dates for range fields
            Given I am logged in the location "123456789".
            And I choose search orders.
            And I open the search orders filter panel.
            When I enter the date: "12/31/2023" for the field "<Initial Date Field>" and the date: "12/30/2023"  for the field "<Final Date Field>".
            Then I should see a validation message: "Initial date should not be greater than final date".
            And  I should see "create date" fields as required.

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
            When I enter a future date for the field "create date to".
            Then I should see a validation message: "Final date should not be greater than today".
            And "apply" option is "disabled".



    Rule: I should be able to multi-select options for Priority, Status, and Ship to Customer fields.
        Rule: I should be able to see order number disabled when filtering by remaining filter fields.
    Rule: I should see the number of fields used to select the filter criteria.
        @R20-228
        Scenario Outline: Check if multiple select inputs are keeping the multiple selection after the user selects the second item

            Given I have a Biopro Order with externalId "EXTSEARCH1979", Location Code "123456789", Priority "STAT" and Status "OPEN".
            And I have a Biopro Order with externalId "EXTSEARCH1984", Location Code "123456789", Priority "ASAP" and Status "IN_PROGRESS".
            And I have a Biopro Order with externalId "EXTSEARCH12018", Location Code "123456789", Priority "ROUTINE" and Status "OPEN".
            And I am logged in the location "123456789".
            And I choose search orders.
            And I open the search orders filter panel.
            When I select "<Selected Priorities>" for the "priority".
            And I select "<Selected Statuses>" for the "order status".
            And I select "<Selected Customers>" for the "ship to customer".
            And Items "<Selected Priorities>" should be selected for "priority".
            And Items "<Selected Statuses>" should be selected for "order status".
            And Items "<Selected Customers>" should be selected for "ship to customer".
            And I select the current date as the "create date" range
            And I select the "12/25/2026" as the "desired shipping date" range
            And "order number" field is "disabled".
            Then I choose "apply" option.
            And I should see "<Expected External Ids>" orders in the search results.
            And I should see "<Expected Number of Filters>" as the number of used filters for the search.
            Examples:
                | Selected Priorities | Selected Statuses | Selected Customers         | Expected External Ids                      | Expected Number of Filters |
                | STAT,ASAP           | OPEN,IN PROGRESS  |                            | EXTSEARCH1979,EXTSEARCH1984                | 4                          |
                | STAT,ROUTINE        |                   |                            | EXTSEARCH1979,EXTSEARCH12018               | 3                          |
                | ASAP                | IN PROGRESS       | Creative Testing Solutions | EXTSEARCH1984                              | 5                          |
                |                     |                   |                            | EXTSEARCH1979,EXTSEARCH1984,EXTSEARCH12018 | 2                          |

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
            Then I should see a validation message: "Date range exceeds two years".
            And "reset" option is "enabled".
            And "apply" option is "disabled".


    Rule: I should be able to search completed orders by order number.
        @api @DIS-294 @bug
        Scenario Outline: Search completed order and the associated backorder
            Given I have an order with external ID "<External Id>" partially fulfilled with a shipment "<Shipment Status>".
            And I have Shipped "<Shipped Quantity>" products of each item line.
            And I have the back order configuration set to "true".
            And I request to complete the order.
            When I search for orders by "<Search Key>".
            Then I should receive the search results containing "<Expected Quantity>" orders.

            Examples:
                | Shipment Status | Search Key | Shipped Quantity | Expected Quantity | External Id        |
                | COMPLETED       | externalId | 2                | 2                 | EXTSEARCH1DIS29402 |
                | COMPLETED       | externalId | 2                | 2                 | 29402              |
                | COMPLETED       | orderId    | 3                | 1                 | EXTSEARCH1DIS29402 |
                | COMPLETED       | orderId    | 3                | 1                 | 29402              |
