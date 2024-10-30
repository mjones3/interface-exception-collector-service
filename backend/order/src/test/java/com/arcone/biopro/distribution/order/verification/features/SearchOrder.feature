@ui @R20-227
Feature: Search Orders

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
                | 1979        | DL1                | DL1               | STAT     | OPEN   | orderId    | 1984         |
                | 1984        | DL1                | DL1               | STAT     | OPEN   | externalId | 1979         |


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
                | 114117922233510 | DL1                | 234567891         | STAT     | OPEN   | externalId |
                | 114117922233510 | DL1                | DL1               | STAT     | OPEN   | 000111     |


        Rule: I should be redirected to the Order Details page if there is only one order in the system that matches the search criteria.
        Scenario Outline: Search for an order and view the details
            Given I cleaned up from the database the orders with external ID "<External ID>".
            And I have a Biopro Order with externalId "<External ID>", Location Code "<Order LocationCode>", Priority "<Priority>" and Status "<Status>".
            And I am logged in the location "<User LocationCode>".
            And I choose search orders.
            When I search the order by "externalId".
            Then I should be redirected to the order details page.

            Examples:
                | External ID     | Order LocationCode | User LocationCode | Priority | Status |
                | 114117922233510 | DL1                | 234567891         | STAT     | OPEN   |
