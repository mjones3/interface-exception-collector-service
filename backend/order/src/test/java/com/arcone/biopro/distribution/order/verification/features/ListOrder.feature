Feature: List of all orders in Search Order
    As a Distribution Technician,
    I want to see the list of orders
    so that I am able to see the list of orders based on priority.

    Background:
        Given I cleaned up from the database the orders with external ID "114117922233598,114117922233599,114117922233578,114117922233579,114117922233510".

    Rule: I should be able to see the list of orders by priority and status where the user logged in.
    Rule: I should be able to view the following information if an order exists
        BioPro Order Number, External Order ID, Priority, Status, Ship to Customer Name, Create Date and Time (MM/DD/YYYY HR:MINS), Desired Ship Date.
    Rule: I should be able to see an option to view the details of an order.
    Rule: I should be able to see the priority column color coded.

    Scenario Outline: List Biopro Orders by priority, status and location
        Given I have a Biopro Order with externalId "<External ID>" , Location Code "<LocationCode>" , Priority "<Priority>" and Status "<Status>".
        When I choose search orders.
        And I am logged in the location "<LocationCode>".
        Then I should see the list of orders based on priority and status.
        And I should see the order details.
        And I should see the priority colored as "<Priority Color>"
        And I should see an option to see the order details.

        Examples:
            | External ID     | LocationCode | Priority | Status | Priority Color |
            | 114117922233598 |  123456789   |  STAT    |  OPEN  |   Red          |
            | 114117922233599 |  234567891   |  ASAP    |  OPEN  |   Orange       |


    Rule: I should be able to configure the color options for the priority column.
        Scenario Outline: List Biopro Orders, changing Status color setup.
            Given I have a Biopro Order with externalId "<External ID>" , Location Code "<LocationCode>" , Priority "<Priority>" and Status "<Status>".
            And I have setup the order priority "<Priority>" color configuration as "<Priority Color>"
            When I choose search orders.
            Then I should see the list of orders based on priority and status.
            And I should see the order details.
            And I should see the priority colored as "<Priority Color>"

            Examples:
                | External ID     | LocationCode | Priority | Status | Priority Color |
                | 114117922233578 |  123456789   |  STAT    |  OPEN  |   yellow     |
                | 114117922233579 |  123456789   |  ROUTINE |  OPEN  |   pink       |


        Scenario Outline: List Biopro Orders different location
            Given I have a Biopro Order with externalId "<External ID>" , Location Code "<Order LocationCode>" , Priority "<Priority>" and Status "<Status>".
            When I choose search orders.
            And I am logged in the location "<User LocationCode>".
            Then I should not see the the biopro order in the list of orders.

            Examples:
                | External ID     | Order LocationCode  | User LocationCode | Priority | Status |
                | 114117922233510 |  123456789          | 234567891         | STAT     |  OPEN  |


    Rule: I should be able to view a maximum of 20 rows in the Results table.
        Scenario : List Biopro Orders by priority, status and location maximum records
            Given I have more than 20 Biopro Orders.
            When I choose search orders.
            Then I should see the list of orders based on priority and status.
            And I should not see more than 20 orders in the list.
