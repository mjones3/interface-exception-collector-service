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
            And I choose search orders.
            When I search the order by "externalId".
            Then I should be redirected to the order details page.

            Examples:
                | Order Number | External ID | Order LocationCode | User LocationCode | Priority | Status | ProductFamily       | BloodType | Quantity | Item Comments |
                | 2018         | DIS1141179  | 123456789          | 123456789         | STAT     | OPEN   | PLASMA_TRANSFUSABLE | AB        | 3        | Needed asap   |





    Rule: I should be able to reset the applied filter criteria.
        Rule: The system should not enable the Apply and Reset options until at least one filter criteria is chosen.
        Scenario: Disable Apply and Reset options when no filter criteria is chosen
            Given I am logged in the location "123456789".
            And I choose to search orders.
            When I open the search filter panel.
            Then I should see Apply and Reset filter options disabled.


    Rule: I should be able to see the following filter options
        Scenario Outline: Check if the filter option is visible and required if specified
            Given I am logged in the location "123456789".
            And I choose to search orders.
            When I open the search filter panel.
            Then I should see "<Filter Parameter>" which "<isRequired>" required.

            Examples:
                | Filter Parameter    | isRequired |
                | orderNumber         | is not     |
                | createDateFrom      | is         |
                | createDateTo        | is         |
                | desiredShipDateFrom | is not     |
                | desiredShipDateTo   | is not     |
                | orderStatus         | is not     |
                | priority            | is not     |
                | shipToCustomer      | is not     |


    Rule: I should be prevented from selecting other filters when BioPro Order number or External ID is selected.
        Scenario Outline: Check if other fields are disable when an order number is specified
            Given I am logged in the location "123456789".
            And I choose to search orders.
            And I open the search filter panel.
            When I type some information on Order Number Field.
            Then "<Filter Parameter>" should be disabled.

            Examples:
                | Filter Parameter    |
                | createDateFrom      |
                | createDateTo        |
                | desiredShipDateFrom |
                | desiredShipDateTo   |
                | orderStatus         |
                | priority            |
                | shipToCustomer      |


    Rule: I should be able to multi-select options for Priority, Status, and Ship to Customer fields.
        Given I am logged in the location "123456789".
            And I choose to search orders.
            When I open the search filter panel.


    Rule: I should see the number of fields used to select the filter criteria.

        Rule: I should be able to enter the create date manually or select from the integrated component.
        Scenario Outline: Check if the user can enter dates manually and pick the date from a date picker component for all date fields
            Given I am logged in the location "123456789".
            And I choose to search orders.
            When I open the search filter panel.
            Then I should be able to open the "<Date Parameter>" datepicker and also type the date manually.

            Examples:
                | Date Parameter      |
                | createDateFrom      |
                | createDateTo        |
                | desiredShipDateFrom |
                | desiredShipDateTo   |

    Rule: I should be able to filter the results for date fields from 2 years back.

        Rule: I should not be able to search more than 2 years range.

    Rule: I should be able to see the other filter options disabled when filtering by either the BioPro Order number or External Order ID.

        Rule: I should not be able to apply filters if any field validations fail.
    Scenario Outline: Disable apply filters button when incorrect parameters are passed
        Give
    Examples:
        | Filter Parameter | Value     |
        | createDateFrom   | 99/1/2024 |

    Rule: I should be able to implement the field-level validation and display an error message if the validations fail.
