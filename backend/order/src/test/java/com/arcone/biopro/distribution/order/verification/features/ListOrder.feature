Feature: List of all orders in Search Order
    As a Distribution Technician,
    I want to see the list of orders
    so that I am able to see the list of orders based on priority.

    Background:
        Given I cleaned up from the database the orders with external ID starting with "EXT_".
        And I have restored the default configuration for the order priority colors.

    Rule: I should be able to see the list of orders by priority and status where the user logged in.
        Rule: I should be able to view the following information if an order exists
        BioPro Order Number, External Order ID, Priority, Status, Ship to Customer Name, Create Date and Time (MM/DD/YYYY HR:MINS), Desired Ship Date.
    Rule: I should be able to see an option to view the details of an order.
        Rule: I should be able to see the priority column color coded.

        Scenario Outline: List Biopro Orders by priority, status and location
            Given I cleaned up from the database the orders with external ID "<External ID>".
            And I have a Biopro Order with externalId "<External ID>", Location Code "<LocationCode>", Priority "<Priority>" and Status "<Status>".
            And I am logged in the location "<LocationCode>".
            When I choose search orders.
            Then I should see the order details.
            And I should see the priority colored as "<Priority Color>"
            And I should see an option to see the order details.

            Examples:
                | External ID     | LocationCode | Priority  | Status | Priority Color |
                | 114117922233594 | MDL_HUB_1    | STAT      | OPEN   | Red            |
                | 114117922233595 | MDL_HUB_1    | ROUTINE   | OPEN   | Grey           |
                | 114117922233596 | MDL_HUB_1    | ASAP      | OPEN   | Orange         |
                | 114117922233597 | MDL_HUB_1    | SCHEDULED | OPEN   | Violet         |
                | 114117922233598 | MDL_HUB_1    | DATE-TIME | OPEN   | Blue           |


    Rule: I should be able to configure the color options for the priority column.
        Scenario Outline: List Biopro Orders, changing Status color setup.
            Given I cleaned up from the database the orders with external ID "<External ID>".
            And I have a Biopro Order with externalId "<External ID>", Location Code "<LocationCode>", Priority "<Priority>" and Status "<Status>".
            And I have setup the order priority "<Priority>" color configuration as "<Priority Color>".
            When I choose search orders.
            And I should see the order details.
            And I should see the priority colored as "<Priority Color>"

            Examples:
                | External ID     | LocationCode | Priority | Status | Priority Color |
                | 114117922233578 | MDL_HUB_1    | STAT     | OPEN   | Green          |
                | 114117922233579 | MDL_HUB_1    | ROUTINE  | OPEN   | Yellow         |


        Scenario Outline: List Biopro Orders different location
            Given I cleaned up from the database the orders with external ID "<External ID>".
            And I have a Biopro Order with externalId "<External ID>", Location Code "<Order LocationCode>", Priority "<Priority>" and Status "<Status>".
            And I am logged in the location "<User LocationCode>".
            When I choose search orders.
            Then I should not see the biopro order in the list of orders.
            And I should see a "Caution Message" message: "No Results Found".

            Examples:
                | External ID     | Order LocationCode        | User LocationCode | Priority | Status |
                | 114117922233510 | DISTRIBUTION_AND_LABELING | MDL_HUB_2         | STAT     | OPEN   |


    Rule: I should be able to view a maximum of 20 rows in the Results table.
        Scenario: List Biopro Orders by priority, status and location maximum records
            Given I have more than 20 Biopro Orders.
            When I choose search orders.
            Then I should not see more than 20 orders in the list.
            And I should see the list of orders based on priority and status.
