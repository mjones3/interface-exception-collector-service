@ui @R20-227 @R20-228
Feature: Search Orders


    Rule: I should be able to filter the order lists by specific criteria.
        Rule: I should be able to apply filter criteria.
    Rule: I should be able to search the order by BioPro order number or External Order ID.
        Scenario Outline: Search orders by Order Number
            Given I cleaned up from the database the orders with external ID "<External ID>".
            And I cleaned up from the database the orders with external ID "<Order Number>".
            And I have a Biopro Order with id "<Order Number>", externalId "<External ID>", Location Code "<Order LocationCode>", Priority "<Priority>" and Status "<Status>".
            And I have a Biopro Order with id "<External ID>", externalId "<Order Number>", Location Code "<Order LocationCode>", Priority "<Priority>" and Status "<Status>".
            And I am logged in the location "<User LocationCode>".
            And Remaining criteria fields are disabled.
            And Reset and Apply options are enabled.
            And I choose search orders.
            When I search the order by "<Order Key>".
            Then I should see 2 orders in the search results.

            Examples:
                | External ID | Order LocationCode | User LocationCode | Priority | Status | Order Key  | Order Number |
                | 1979        | 123456789          | 123456789         | STAT     | OPEN   | orderId    | 1984         |
                | 1984        | 123456789          | 123456789         | STAT     | OPEN   | externalId | 1979         |


#        Discuss to remove this acceptance criteria. This is covered by list orders.
    Rule: I should not be able to see the orders from a different location.
        Rule: I should be able to view an error message when I search for a non-existent order number.
        Scenario Outline: Search for an order number from a different location
            Given I cleaned up from the database the orders with external ID "<External ID>".
            And I have a Biopro Order with externalId "<External ID>", Location Code "<Order LocationCode>", Priority "<Priority>" and Status "<Status>".
            And I am logged in the location "<User LocationCode>".
            And Remaining criteria fields are disabled.
            And Reset and Apply options are enabled.
            And I choose search orders.
            When I search the order by "<Search Key>".
            Then I should see a "Caution" message: "No Results Found".

            Examples:
                | External ID     | Order LocationCode | User LocationCode | Priority | Status | Search Key |
                | 114117922233510 | 123456789          | 234567891         | STAT     | OPEN   | externalId |
                | 114117922233510 | 123456789          | 123456789         | STAT     | OPEN   | 000111     |


    Rule: I should be redirected to the Order Details page if there is only one order in the system that matches the search criteria.
        Scenario Outline: Search for an order and view the details
            Given I cleaned up from the database the orders with external ID "<External ID>".
            And I have a Biopro Order with id "<Order Number>", externalId "<External ID>", Location Code "<Order LocationCode>", Priority "<Priority>" and Status "<Status>".
            And I have an order item with product family "<ProductFamily>", blood type "<BloodType>", quantity <Quantity>, and order item comments "<Item Comments>".
            And I am logged in the location "<User LocationCode>".
            And Reset and Apply options are enabled.
            And I choose search orders.
            And Remaining criteria fields are disabled.
            When I search the order by "externalId".
            Then I should be redirected to the order details page.

            Examples:
                | Order Number | External ID | Order LocationCode | User LocationCode | Priority | Status | ProductFamily       | BloodType | Quantity | Item Comments |
                | 2018         | DIS1141179  | 123456789          | 123456789         | STAT     | OPEN   | PLASMA_TRANSFUSABLE | AB        | 3        | Needed asap   |

    Rule: I should be able to reset the applied filter criteria.
        Scenario: Ensure the reset button clears the specified filter criteria
            Given I am logged in at location "123456789"
            And I choose search orders.
            And I open the search orders filter panel
            And I enter "00000" for the "OrderNumber"
            When I click on the reset filter button
            Then The filter information should be empty


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

    Rule: I should not be able to select create date parameters values greater than current date
        Scenario: Ensure that the selected dates for create date aren't greater than current date
            Given I am logged in at location "<User LocationCode>"
            And I choose search orders.
            And I open the search orders filter panel
            When I enter a future date for the "<Final Create Date Field>"
            Then The system should display the "Initial date should not be greater than final date" validation message


    Rule: The system should not enable the Apply and Reset options until at least one filter criteria is chosen.
        Scenario: Disable Apply and Reset options when no filter criteria is chosen
            Given I am logged in the location "123456789".
            When I choose to search orders.
            And I open the search orders filter panel
            Then I should see Apply and Reset filter options disabled.


    Rule: I should be able to see the following filter options
        Scenario Outline: Check if the filter option is visible and required if specified
            Given I am logged in the location "123456789".
            When I choose to search orders.
            And I open the search orders filter panel
            Then I should see "<Filter Parameter>" which "<isRequired>" required.
            # TODO: Discuss with Ben about alternatives
            Examples:
                | Fields                     | RequiredFields |
                | order number               | is not         |
                | create date from           | is             |
                | create date to             | is             |
                | desired shipment date from | is not         |
                | desired shipment date to   | is not         |
                | orderStatus                | is not         |
                | priority                   | is not         |
                | ship to customer           | is not         |


    Rule: I should be prevented from selecting other filters when BioPro Order number or External ID is selected.
        Rule: I should be able to see the other filter options disabled when filtering by either the BioPro Order number or External Order ID.
        Scenario: Check if other fields are disabled when an order number is specified
            Given I am logged in the location "123456789".
            And I choose to search orders.
            And I open the search orders filter panel
            When I type some information on Order Number Field.
            Then "createDateFrom,createDateTo,desiredShipDateFrom,desiredShipDateTo,orderStatus,priority,shipToCustomer" fields should be disabled.


    Rule: I should be able to multi-select options for Priority, Status, and Ship to Customer fields.
        Scenario Outline: Check if multiple select inputs are keeping the multiple selection after the user selects the second item
            Given I am logged in the location "123456789".
            And I choose to search orders.
            And I open the search orders filter panel
            When I select two items from "<Multi Select Field>"
            Then Two items should be selected from "<Multi Select Field>"
            Examples:
                | Multi Select Field |
                | priority           |
                | status             |
                | ship to customer   |

    Rule: I should see the number of fields used to select the filter criteria.

        Rule: I should be able to enter the create date manually or select from the integrated component.
        Scenario Outline: Check if the user can enter dates manually and pick the date from a date picker component for all date fields
            Given I am logged in the location "123456789".
            And I choose to search orders.
            When I open the search orders filter panel.
            Then I should be able to open the "<Date Parameter>" datepicker and also type the date manually.
            Examples:
                | Date Parameter             |
                | create date from           |
                | create date to             |
                | desired shipment date from |
                | desired shipment date to   |

    Rule: I should be able to filter the results for date fields from 2 years back.
        Rule: I should not be able to search more than 2 years range.
        Scenario: Check if the values informed for create date range don't exceed 2 years in the past
            Given I am logged in the location "123456789".
            And I choose to search orders.
            And I open the search orders filter panel.
            When I enter "11/31/2024" for the "create date from"
            Then The system should display the "Create date should not exceed 2 years in the past" validation message


    Rule: I should not be able to apply filters if any field validations fail.
        Scenario Outline: Disable apply filters button when incorrect parameters are passed
        Give
            Examples:
                | Filter Parameter | Value     |
                | createDateFrom   | 99/1/2024 |


    Rule: I should be able to implement the field-level validation and display an error message if the validations fail.



