@ui @R20-227
Feature: Search Orders

    Rule: I should be able to search the order by BioPro order number or External Oder ID. Display in the UI as BioPro Order #.
        Scenario Outline: Search orders by Order Number
            Given I cleaned up from the database the orders with external ID "<External ID>".
            And I have a Biopro Order with externalId "<External ID>", Location Code "<Order LocationCode>", Priority "<Priority>" and Status "<Status>".
            And I am logged in the location "<User LocationCode>".
            And I choose search orders.
            When I search the order by "<Order Key>".
            Then I should be redirected to the order details page.

            Examples:
                | External ID     | Order LocationCode | User LocationCode | Priority | Status | Order Key  |
                | 114117922233510 | DL1                | 234567891         | STAT     | OPEN   | orderId    |
                | 114117922233510 | DL1                | 234567891         | STAT     | OPEN   | externalId |


    Rule: I should be able to view an error message when I search for a non-existent order number.
        Rule: I should not be able to see the orders from a different location.
        Scenario Outline: Search for a non-existent order number
            Given I cleaned up from the database the orders with external ID "<External ID>".
            And I have a Biopro Order with externalId "<External ID>", Location Code "<Order LocationCode>", Priority "<Priority>" and Status "<Status>".
            And I am logged in the location "<User LocationCode>".
            When I choose search orders.

            Examples:
                | External ID     | Order LocationCode | User LocationCode | Priority | Status |
                | 114117922233510 | DL1                | 234567891         | STAT     | OPEN   |


    Rule: I should be redirected to the Order Details page if the order I’m searching exists.
        Scenario Outline: Search for an order and view the details
            Given I cleaned up from the database the orders with external ID "<External ID>".
            And I have a Biopro Order with externalId "<External ID>", Location Code "<Order LocationCode>", Priority "<Priority>" and Status "<Status>".
            And I am logged in the location "<User LocationCode>".
            When I choose search orders.

            Examples:
                | External ID     | Order LocationCode | User LocationCode | Priority | Status |
                | 114117922233510 | DL1                | 234567891         | STAT     | OPEN   |
