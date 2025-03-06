@AOA-39
Feature: List of all orders in Search Order

    Background:
        Given I cleaned up from the database the orders with external ID starting with "EXT1141179".
        And I cleaned up from the database the orders with external ID starting with "EXTDIS220".
        And I cleaned up from the database the orders with external ID starting with "EXT20RECORDS".
        And I have restored the default configuration for the order priority colors.

    Rule: I should be able to see the list of orders by priority and status where the user logged in.
        Rule: I should be able to view the following information if an order exists
        BioPro Order Number, External Order ID, Priority, Status, Ship to Customer Name, Create Date and Time (MM/DD/YYYY HR:MINS), Desired Ship Date.
    Rule: I should be able to see an option to view the details of an order.
        Rule: I should be able to see the priority column color coded.
        @DIS-95 @ui
        Scenario Outline: List Biopro Orders by priority, status and location
            Given I cleaned up from the database the orders with external ID "<External ID>".
            And I have a Biopro Order with externalId "<External ID>", Location Code "<LocationCode>", Priority "<Priority>" and Status "<Status>".
            And I am logged in the location "<LocationCode>".
            When I choose search orders.
            Then I should see the order details.
            And I should see the priority colored as "<Priority Color>"
            And I should see an option to see the order details.

            Examples:
                | External ID        | LocationCode | Priority  | Status | Priority Color |
                | EXT114117922233594 | 123456789    | STAT      | OPEN   | Red            |
                | EXT114117922233595 | 123456789    | ROUTINE   | OPEN   | Grey           |
                | EXT114117922233596 | 123456789    | ASAP      | OPEN   | Orange         |
                | EXT114117922233597 | 123456789    | SCHEDULED | OPEN   | Violet         |
                | EXT114117922233598 | 123456789    | DATE-TIME | OPEN   | Blue           |


    Rule: I should be able to configure the color options for the priority column.
        @DIS-95 @ui
        Scenario Outline: List Biopro Orders, changing Status color setup.
            Given I cleaned up from the database the orders with external ID "<External ID>".
            And I have a Biopro Order with externalId "<External ID>", Location Code "<LocationCode>", Priority "<Priority>" and Status "<Status>".
            And I have setup the order priority "<Priority>" color configuration as "<Priority Color>".
            When I choose search orders.
            And I should see the order details.
            And I should see the priority colored as "<Priority Color>"

            Examples:
                | External ID        | LocationCode | Priority | Status | Priority Color |
                | EXT114117922233578 | 123456789    | STAT     | OPEN   | Green          |
                | EXT114117922233579 | 123456789    | ROUTINE  | OPEN   | Yellow         |


        @DIS-146 @DIS-95 @ui
        Scenario Outline: List Biopro Orders different location
            Given I cleaned up from the database the orders with external ID "<External ID>".
            And I have a Biopro Order with externalId "<External ID>", Location Code "<Order LocationCode>", Priority "<Priority>" and Status "<Status>".
            And I am logged in the location "<User LocationCode>".
            When I choose search orders.
            Then I should not see the biopro order in the list of orders.
            And I should see a "Caution" message: "No Results Found".

            Examples:
                | External ID        | Order LocationCode | User LocationCode | Priority | Status |
                | EXT114117922233510 | DL1                | 234567891         | STAT     | OPEN   |


    Rule: I should see the list of orders sorted by priority (ascending order), status (descending order), and desired shipping date (ascending order) where the user logged in.
        @R20-274 @api @bug @DIS-285
        Scenario: List Biopro Orders in the specified order by default
            Given I have these BioPro Orders.
                | External ID      | Location Code | Priority | Status      | Desired Shipment Date |
                | EXT1141179223321 | 1979          | STAT     | IN_PROGRESS | 2025-01-01            |
                | EXT1141179320123 | 1979          | STAT     | IN_PROGRESS | 2025-01-02            |
                | EXT1141179225123 | 1979          | STAT     | OPEN        | 2025-01-01            |
                | EXT1141179114123 | 1979          | ROUTINE  | IN_PROGRESS | 2025-01-02            |
                | EXT1141179123123 | 1979          | ASAP     | IN_PROGRESS | 2025-01-01            |
                | EXT1141179179123 | 1979          | ROUTINE  | OPEN        | 2025-01-01            |
                | EXT1141179443123 | 1979          | ASAP     | IN_PROGRESS | 2025-01-02            |
                | EXT1141179915123 | 1979          | ASAP     | OPEN        | 2025-01-01            |
                | EXT1141179541123 | 1979          | ROUTINE  | IN_PROGRESS | 2025-01-01            |
                | EXT1141179DIS2951| 1979          | ROUTINE  | IN_PROGRESS | NULL_VALUE            |
            When I want to list orders for location "1979".
            Then I should have orders listed in the following order.
                | External ID      | Location Code | Priority | Status      | Desired Shipment Date |
                | EXT1141179225123 | 1979          | STAT     | OPEN        | 2025-01-01            |
                | EXT1141179223321 | 1979          | STAT     | IN_PROGRESS | 2025-01-01            |
                | EXT1141179320123 | 1979          | STAT     | IN_PROGRESS | 2025-01-02            |
                | EXT1141179915123 | 1979          | ASAP     | OPEN        | 2025-01-01            |
                | EXT1141179123123 | 1979          | ASAP     | IN_PROGRESS | 2025-01-01            |
                | EXT1141179443123 | 1979          | ASAP     | IN_PROGRESS | 2025-01-02            |
                | EXT1141179179123 | 1979          | ROUTINE  | OPEN        | 2025-01-01            |
                | EXT1141179541123 | 1979          | ROUTINE  | IN_PROGRESS | 2025-01-01            |
                | EXT1141179114123 | 1979          | ROUTINE  | IN_PROGRESS | 2025-01-02            |
                | EXT1141179DIS2951| 1979          | ROUTINE  | IN_PROGRESS | NULL_VALUE            |




    Rule: I should be able to see all the orders that are open and in progress at that location.
        @R20-274 @api
        Scenario: List Biopro Orders in OPEN or IN_PROGRESS status
            Given I have these BioPro Orders.
                | External ID      | Location Code | Priority | Status      | Desired Shipment Date |
                | EXT1141179223322 | 1979          | STAT     | OPEN        | 2025-01-02            |
                | EXT1141179223321 | 1979          | STAT     | IN_PROGRESS | 2025-01-01            |
                | EXT1141179320123 | 1979          | STAT     | IN_PROGRESS | 2025-01-02            |
                | EXT1141179225123 | 1979          | STAT     | COMPLETED   | 2025-01-01            |
            When I want to list orders for location "1979".
            Then I should have orders listed in the following order.
                | External ID      | Location Code | Priority | Status      | Desired Shipment Date |
                | EXT1141179223322 | 1979          | STAT     | OPEN        | 2025-01-02            |
                | EXT1141179223321 | 1979          | STAT     | IN_PROGRESS | 2025-01-01            |
                | EXT1141179320123 | 1979          | STAT     | IN_PROGRESS | 2025-01-02            |


    Rule: I should be able to view a maximum of 20 rows in the Results table.
        @DIS-95
        Scenario: List Biopro Orders by priority, status and location maximum records
            Given I cleaned up from the database the orders with external ID starting with "EXT20RECORDS".
            And I have more than 20 Biopro Orders.
            When I choose search orders.
            Then I should not see more than 20 orders in the list.
            And I should see the list of orders based on priority and status.
            Then I cleaned up from the database the orders with external ID starting with "EXT20RECORDS".


    Rule: I should be able to navigate across pages.
    Rule: I should be able to know the total number of records and pages.
    Rule: I should be able to know the total number of items per page.
    Rule: I should be able to know the current page number.
    @api @DIS-220
    Scenario Outline: List orders and navigate through multiple pages.
        Given I cleaned up from the database the orders with external ID starting with "EXTDIS220".
        And I have <Total Records> Biopro Order(s).
        # Request without specifying a page (expected to get the first page by default)
        When I request to list the Orders.
        Then I should receive <Total Records> order(s) splitted in <Total Pages> page(s).
        And I confirm that the page 1 "has" <Total Items per Page> orders.
        And I confirm that the page 1 "has no" previous page and "has" next page.
        # Navigate to next page (page 2 out of 3)
        When I request to list the Orders at page 2.
        Then I confirm that the page 2 "has" <Total Items per Page> orders.
        And I confirm that the page 2 "has" previous page and "has" next page.
        # Navigate to last page (page 3 out of 3)
        When I request to list the Orders at page 3.
        Then I confirm that the page 3 "has" <Last Page Total Items> orders.
        And I confirm that the page 3 "has" previous page and "has no" next page.
        Examples:
            | Total Records | Total Pages | Total Items per Page | Last Page Total Items |
            | 50            | 3           | 20                   | 10                    |

