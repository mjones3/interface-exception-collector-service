@AOA-39 @AO-152
Feature: List of all orders in Search Order

    Background:
        Given I cleaned up from the database the orders with external ID starting with "EXT1141179".
        And I cleaned up from the database the orders with external ID starting with "EXTDIS220".
        And I cleaned up from the database the orders with external ID starting with "EXT20RECORDS".
        And I cleaned up from the database the orders with external ID starting with "EXTDIS237".
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
                | External ID      | Location Code | Priority | Status      | Desired Shipment Date | Customer Code | Ship To Customer Name      | Create Date         |
                | EXT1141179223321 | 1979          | STAT     | IN_PROGRESS | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T20:59:16 |
                | EXT1141179320123 | 1979          | STAT     | IN_PROGRESS | 2025-01-02            | B2346         | Advanced Medical Center    | 2025-01-01T20:59:16 |
                | EXT1141179225123 | 1979          | STAT     | OPEN        | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T20:59:16 |
                | EXT1141179114123 | 1979          | ROUTINE  | IN_PROGRESS | 2025-01-02            | A1235         | Creative Testing Solutions | 2025-01-01T20:59:16 |
                | EXT1141179123123 | 1979          | ASAP     | IN_PROGRESS | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T20:59:16 |
                | EXT1141179179123 | 1979          | ROUTINE  | OPEN        | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T20:59:16 |
                | EXT1141179443123 | 1979          | ASAP     | IN_PROGRESS | 2025-01-02            | B2346         | Advanced Medical Center    | 2025-01-01T20:59:16 |
                | EXT1141179915123 | 1979          | ASAP     | OPEN        | 2025-01-01            | B2346         | Advanced Medical Center    | 2025-01-01T20:59:16 |
                | EXT1141179541123 | 1979          | ROUTINE  | IN_PROGRESS | 2025-01-01            | C3457         | Pioneer Health Services    | 2025-01-01T20:59:16 |
                | EXT1141179DIS2951| 1979          | ROUTINE  | IN_PROGRESS | NULL_VALUE            | C3457         | Pioneer Health Services    | 2025-01-01T20:59:16 |
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
                | External ID      | Location Code | Priority | Status      | Desired Shipment Date | Customer Code | Ship To Customer Name      | Create Date         |
                | EXT1141179223322 | 1979          | STAT     | OPEN        | 2025-01-02            | A1235         | Creative Testing Solutions | 2025-01-01T00:00:00 |
                | EXT1141179223321 | 1979          | STAT     | IN_PROGRESS | 2025-01-01            | B2346         | Advanced Medical Center    | 2025-01-01T01:00:00 |
                | EXT1141179320123 | 1979          | STAT     | IN_PROGRESS | 2025-01-02            | A1235         | Creative Testing Solutions | 2025-01-01T02:00:00 |
                | EXT1141179225123 | 1979          | STAT     | COMPLETED   | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T03:00:00 |
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


        Rule: I should be able to sort the information in ascending order.
        Rule: I should be able to sort the information in descending order.
        Rule: I should be able to sort all the following fields -BioPro Order ID, External Order ID, Priority, Status
        , Ship to Customer Name, Create Date and Time (MM/DD/YYYY HR: MINS), Desired Ship Date
        Rule: I should be able to sort one column at a time.
        @api @DIS-237
        Scenario: Sorting Biopro Orders by properties
            Given I have these BioPro Orders.
                | External ID  | Location Code | Priority | Status      | Desired Shipment Date | Customer Code | Ship To Customer Name      | Create Date         |
                | EXTDIS237001 | 123456789     | STAT     | IN_PROGRESS | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T00:00:00 |
                | EXTDIS237002 | 123456789     | STAT     | IN_PROGRESS | 2025-01-02            | B2346         | Advanced Medical Center    | 2025-01-01T01:00:00 |
                | EXTDIS237003 | 123456789     | STAT     | OPEN        | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T02:00:00 |
                | EXTDIS237004 | 123456789     | ROUTINE  | IN_PROGRESS | 2025-01-02            | A1235         | Creative Testing Solutions | 2025-01-01T03:00:00 |
                | EXTDIS237005 | 123456789     | ASAP     | IN_PROGRESS | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T04:00:00 |
                | EXTDIS237006 | 123456789     | ROUTINE  | OPEN        | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T05:00:00 |
                | EXTDIS237007 | 123456789     | ASAP     | IN_PROGRESS | 2025-01-02            | B2346         | Advanced Medical Center    | 2025-01-01T06:00:00 |
                | EXTDIS237008 | 123456789     | ASAP     | OPEN        | 2025-01-01            | B2346         | Advanced Medical Center    | 2025-01-01T07:00:00 |
                | EXTDIS237009 | 123456789     | ROUTINE  | IN_PROGRESS | 2025-01-01            | C3457         | Pioneer Health Services    | 2025-01-01T13:00:00 |
                | EXTDIS2370010| 123456789     | ROUTINE  | IN_PROGRESS | NULL_VALUE            | C3457         | Pioneer Health Services    | 2025-01-01T15:00:00 |
                | EXTDIS2370011| 123456789     | STAT     | OPEN        | 2025-01-01            | C3457         | Pioneer Health Services    | 2025-01-01T10:00:00 |
                | EXTDIS2370012| 123456789     | ROUTINE  | OPEN        | 2025-01-01            | B2346         | Advanced Medical Center    | 2025-01-01T11:30:00 |
            When I request to list the Orders.
            Then I should receive the orders listed in the following order.
                | External ID  | Location Code | Priority | Status      | Desired Shipment Date | Customer Code | Ship To Customer Name      | Create Date         |
                | EXTDIS2370011| 123456789     | STAT     | OPEN        | 2025-01-01            | C3457         | Pioneer Health Services    | 2025-01-01T10:00:00 |
                | EXTDIS237003 | 123456789     | STAT     | OPEN        | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T02:00:00 |
                | EXTDIS237001 | 123456789     | STAT     | IN_PROGRESS | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T00:00:00 |
                | EXTDIS237002 | 123456789     | STAT     | IN_PROGRESS | 2025-01-02            | B2346         | Advanced Medical Center    | 2025-01-01T01:00:00 |
                | EXTDIS237008 | 123456789     | ASAP     | OPEN        | 2025-01-01            | B2346         | Advanced Medical Center    | 2025-01-01T07:00:00 |
                | EXTDIS237005 | 123456789     | ASAP     | IN_PROGRESS | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T04:00:00 |
                | EXTDIS237007 | 123456789     | ASAP     | IN_PROGRESS | 2025-01-02            | B2346         | Advanced Medical Center    | 2025-01-01T06:00:00 |
                | EXTDIS2370012| 123456789     | ROUTINE  | OPEN        | 2025-01-01            | B2346         | Advanced Medical Center    | 2025-01-01T11:30:00 |
                | EXTDIS237006 | 123456789     | ROUTINE  | OPEN        | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T05:00:00 |
                | EXTDIS237009 | 123456789     | ROUTINE  | IN_PROGRESS | 2025-01-01            | C3457         | Pioneer Health Services    | 2025-01-01T13:00:00 |
                | EXTDIS237004 | 123456789     | ROUTINE  | IN_PROGRESS | 2025-01-02            | A1235         | Creative Testing Solutions | 2025-01-01T03:00:00 |
                | EXTDIS2370010| 123456789     | ROUTINE  | IN_PROGRESS | NULL_VALUE            | C3457         | Pioneer Health Services    | 2025-01-01T15:00:00 |
           When I request to list the Orders sorted by "priority" in "ascending" order.
           Then I should receive the orders listed in the following order.
               | External ID  | Location Code | Priority | Status      | Desired Shipment Date | Customer Code | Ship To Customer Name      | Create Date         |
               | EXTDIS237002 | 123456789     | STAT     | IN_PROGRESS | 2025-01-02            | B2346         | Advanced Medical Center    | 2025-01-01T01:00:00 |
               | EXTDIS237001 | 123456789     | STAT     | IN_PROGRESS | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T00:00:00 |
               | EXTDIS237003 | 123456789     | STAT     | OPEN        | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T02:00:00 |
               | EXTDIS2370011| 123456789     | STAT     | OPEN        | 2025-01-01            | C3457         | Pioneer Health Services    | 2025-01-01T10:00:00 |
               | EXTDIS237007 | 123456789     | ASAP     | IN_PROGRESS | 2025-01-02            | B2346         | Advanced Medical Center    | 2025-01-01T06:00:00 |
               | EXTDIS237005 | 123456789     | ASAP     | IN_PROGRESS | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T04:00:00 |
               | EXTDIS237008 | 123456789     | ASAP     | OPEN        | 2025-01-01            | B2346         | Advanced Medical Center    | 2025-01-01T07:00:00 |
               | EXTDIS2370012| 123456789     | ROUTINE  | OPEN        | 2025-01-01            | B2346         | Advanced Medical Center    | 2025-01-01T11:30:00 |
               | EXTDIS237009 | 123456789     | ROUTINE  | IN_PROGRESS | 2025-01-01            | C3457         | Pioneer Health Services    | 2025-01-01T13:00:00 |
               | EXTDIS2370010| 123456789     | ROUTINE  | IN_PROGRESS | NULL_VALUE            | C3457         | Pioneer Health Services    | 2025-01-01T15:00:00 |
               | EXTDIS237004 | 123456789     | ROUTINE  | IN_PROGRESS | 2025-01-02            | A1235         | Creative Testing Solutions | 2025-01-01T03:00:00 |
               | EXTDIS237006 | 123456789     | ROUTINE  | OPEN        | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T05:00:00 |
           And  The sorting indicator should be at "priority" property in "ascending" order.
           When I request to list the Orders sorted by "priority" in "descending" order.
           Then I should receive the orders listed in the following order.
               | External ID  | Location Code | Priority | Status      | Desired Shipment Date | Customer Code | Ship To Customer Name      | Create Date         |
               | EXTDIS237006 | 123456789     | ROUTINE  | OPEN        | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T05:00:00 |
               | EXTDIS237009 | 123456789     | ROUTINE  | IN_PROGRESS | 2025-01-01            | C3457         | Pioneer Health Services    | 2025-01-01T13:00:00 |
               | EXTDIS2370010| 123456789     | ROUTINE  | IN_PROGRESS | NULL_VALUE            | C3457         | Pioneer Health Services    | 2025-01-01T15:00:00 |
               | EXTDIS237004 | 123456789     | ROUTINE  | IN_PROGRESS | 2025-01-02            | A1235         | Creative Testing Solutions | 2025-01-01T03:00:00 |
               | EXTDIS2370012| 123456789     | ROUTINE  | OPEN        | 2025-01-01            | B2346         | Advanced Medical Center    | 2025-01-01T11:30:00 |
               | EXTDIS237007 | 123456789     | ASAP     | IN_PROGRESS | 2025-01-02            | B2346         | Advanced Medical Center    | 2025-01-01T06:00:00 |
               | EXTDIS237005 | 123456789     | ASAP     | IN_PROGRESS | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T04:00:00 |
               | EXTDIS237008 | 123456789     | ASAP     | OPEN        | 2025-01-01            | B2346         | Advanced Medical Center    | 2025-01-01T07:00:00 |
               | EXTDIS237001 | 123456789     | STAT     | IN_PROGRESS | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T00:00:00 |
               | EXTDIS237003 | 123456789     | STAT     | OPEN        | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T02:00:00 |
               | EXTDIS2370011| 123456789     | STAT     | OPEN        | 2025-01-01            | C3457         | Pioneer Health Services    | 2025-01-01T10:00:00 |
               | EXTDIS237002 | 123456789     | STAT     | IN_PROGRESS | 2025-01-02            | B2346         | Advanced Medical Center    | 2025-01-01T01:00:00 |
           And  The sorting indicator should be at "priority" property in "descending" order.
           When I request to list the Orders sorted by "externalId" in "ascending" order.
           Then I should receive the orders listed in the following order.
               | External ID  | Location Code | Priority | Status      | Desired Shipment Date | Customer Code | Ship To Customer Name      | Create Date         |
               | EXTDIS237001 | 123456789     | STAT     | IN_PROGRESS | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T00:00:00 |
               | EXTDIS2370010| 123456789     | ROUTINE  | IN_PROGRESS | NULL_VALUE            | C3457         | Pioneer Health Services    | 2025-01-01T15:00:00 |
               | EXTDIS2370011| 123456789     | STAT     | OPEN        | 2025-01-01            | C3457         | Pioneer Health Services    | 2025-01-01T10:00:00 |
               | EXTDIS2370012| 123456789     | ROUTINE  | OPEN        | 2025-01-01            | B2346         | Advanced Medical Center    | 2025-01-01T11:30:00 |
               | EXTDIS237002 | 123456789     | STAT     | IN_PROGRESS | 2025-01-02            | B2346         | Advanced Medical Center    | 2025-01-01T01:00:00 |
               | EXTDIS237003 | 123456789     | STAT     | OPEN        | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T02:00:00 |
               | EXTDIS237004 | 123456789     | ROUTINE  | IN_PROGRESS | 2025-01-02            | A1235         | Creative Testing Solutions | 2025-01-01T03:00:00 |
               | EXTDIS237005 | 123456789     | ASAP     | IN_PROGRESS | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T04:00:00 |
               | EXTDIS237006 | 123456789     | ROUTINE  | OPEN        | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T05:00:00 |
               | EXTDIS237007 | 123456789     | ASAP     | IN_PROGRESS | 2025-01-02            | B2346         | Advanced Medical Center    | 2025-01-01T06:00:00 |
               | EXTDIS237008 | 123456789     | ASAP     | OPEN        | 2025-01-01            | B2346         | Advanced Medical Center    | 2025-01-01T07:00:00 |
               | EXTDIS237009 | 123456789     | ROUTINE  | IN_PROGRESS | 2025-01-01            | C3457         | Pioneer Health Services    | 2025-01-01T13:00:00 |
           And  The sorting indicator should be at "externalId" property in "ascending" order.
           When I request to list the Orders sorted by "externalId" in "descending" order.
           Then I should receive the orders listed in the following order.
               | External ID  | Location Code | Priority | Status      | Desired Shipment Date | Customer Code | Ship To Customer Name      | Create Date         |
               | EXTDIS237009 | 123456789     | ROUTINE  | IN_PROGRESS | 2025-01-01            | C3457         | Pioneer Health Services    | 2025-01-01T13:00:00 |
               | EXTDIS237008 | 123456789     | ASAP     | OPEN        | 2025-01-01            | B2346         | Advanced Medical Center    | 2025-01-01T07:00:00 |
               | EXTDIS237007 | 123456789     | ASAP     | IN_PROGRESS | 2025-01-02            | B2346         | Advanced Medical Center    | 2025-01-01T06:00:00 |
               | EXTDIS237006 | 123456789     | ROUTINE  | OPEN        | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T05:00:00 |
               | EXTDIS237005 | 123456789     | ASAP     | IN_PROGRESS | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T04:00:00 |
               | EXTDIS237004 | 123456789     | ROUTINE  | IN_PROGRESS | 2025-01-02            | A1235         | Creative Testing Solutions | 2025-01-01T03:00:00 |
               | EXTDIS237003 | 123456789     | STAT     | OPEN        | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T02:00:00 |
               | EXTDIS237002 | 123456789     | STAT     | IN_PROGRESS | 2025-01-02            | B2346         | Advanced Medical Center    | 2025-01-01T01:00:00 |
               | EXTDIS2370012| 123456789     | ROUTINE  | OPEN        | 2025-01-01            | B2346         | Advanced Medical Center    | 2025-01-01T11:30:00 |
               | EXTDIS2370011| 123456789     | STAT     | OPEN        | 2025-01-01            | C3457         | Pioneer Health Services    | 2025-01-01T10:00:00 |
               | EXTDIS2370010| 123456789     | ROUTINE  | IN_PROGRESS | NULL_VALUE            | C3457         | Pioneer Health Services    | 2025-01-01T15:00:00 |
               | EXTDIS237001 | 123456789     | STAT     | IN_PROGRESS | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T00:00:00 |
            And  The sorting indicator should be at "externalId" property in "descending" order.
            When I request to list the Orders sorted by "status" in "ascending" order.
            Then I should receive the orders listed in the following order.
                | External ID  | Location Code | Priority | Status      | Desired Shipment Date | Customer Code | Ship To Customer Name      | Create Date         |
                | EXTDIS237001 | 123456789     | STAT     | IN_PROGRESS | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T00:00:00 |
                | EXTDIS237002 | 123456789     | STAT     | IN_PROGRESS | 2025-01-02            | B2346         | Advanced Medical Center    | 2025-01-01T01:00:00 |
                | EXTDIS237004 | 123456789     | ROUTINE  | IN_PROGRESS | 2025-01-02            | A1235         | Creative Testing Solutions | 2025-01-01T03:00:00 |
                | EXTDIS237005 | 123456789     | ASAP     | IN_PROGRESS | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T04:00:00 |
                | EXTDIS237007 | 123456789     | ASAP     | IN_PROGRESS | 2025-01-02            | B2346         | Advanced Medical Center    | 2025-01-01T06:00:00 |
                | EXTDIS237009 | 123456789     | ROUTINE  | IN_PROGRESS | 2025-01-01            | C3457         | Pioneer Health Services    | 2025-01-01T13:00:00 |
                | EXTDIS2370010| 123456789     | ROUTINE  | IN_PROGRESS | NULL_VALUE            | C3457         | Pioneer Health Services    | 2025-01-01T15:00:00 |
                | EXTDIS237003 | 123456789     | STAT     | OPEN        | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T02:00:00 |
                | EXTDIS237008 | 123456789     | ASAP     | OPEN        | 2025-01-01            | B2346         | Advanced Medical Center    | 2025-01-01T07:00:00 |
                | EXTDIS2370011| 123456789     | STAT     | OPEN        | 2025-01-01            | C3457         | Pioneer Health Services    | 2025-01-01T10:00:00 |
                | EXTDIS237006 | 123456789     | ROUTINE  | OPEN        | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T05:00:00 |
                | EXTDIS2370012| 123456789     | ROUTINE  | OPEN        | 2025-01-01            | B2346         | Advanced Medical Center    | 2025-01-01T11:30:00 |
            And  The sorting indicator should be at "status" property in "ascending" order.
            When I request to list the Orders sorted by "status" in "descending" order.
            Then I should receive the orders listed in the following order.
                | External ID  | Location Code | Priority | Status      | Desired Shipment Date | Customer Code | Ship To Customer Name      | Create Date         |
                | EXTDIS237008 | 123456789     | ASAP     | OPEN        | 2025-01-01            | B2346         | Advanced Medical Center    | 2025-01-01T07:00:00 |
                | EXTDIS237006 | 123456789     | ROUTINE  | OPEN        | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T05:00:00 |
                | EXTDIS237003 | 123456789     | STAT     | OPEN        | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T02:00:00 |
                | EXTDIS2370011| 123456789     | STAT     | OPEN        | 2025-01-01            | C3457         | Pioneer Health Services    | 2025-01-01T10:00:00 |
                | EXTDIS2370012| 123456789     | ROUTINE  | OPEN        | 2025-01-01            | B2346         | Advanced Medical Center    | 2025-01-01T11:30:00 |
                | EXTDIS237009 | 123456789     | ROUTINE  | IN_PROGRESS | 2025-01-01            | C3457         | Pioneer Health Services    | 2025-01-01T13:00:00 |
                | EXTDIS2370010| 123456789     | ROUTINE  | IN_PROGRESS | NULL_VALUE            | C3457         | Pioneer Health Services    | 2025-01-01T15:00:00 |
                | EXTDIS237007 | 123456789     | ASAP     | IN_PROGRESS | 2025-01-02            | B2346         | Advanced Medical Center    | 2025-01-01T06:00:00 |
                | EXTDIS237002 | 123456789     | STAT     | IN_PROGRESS | 2025-01-02            | B2346         | Advanced Medical Center    | 2025-01-01T01:00:00 |
                | EXTDIS237004 | 123456789     | ROUTINE  | IN_PROGRESS | 2025-01-02            | A1235         | Creative Testing Solutions | 2025-01-01T03:00:00 |
                | EXTDIS237005 | 123456789     | ASAP     | IN_PROGRESS | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T04:00:00 |
                | EXTDIS237001 | 123456789     | STAT     | IN_PROGRESS | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T00:00:00 |
            And  The sorting indicator should be at "status" property in "descending" order.
            When I request to list the Orders sorted by "shippingCustomerName" in "ascending" order.
            Then I should receive the orders listed in the following order.
                | External ID  | Location Code | Priority | Status      | Desired Shipment Date | Customer Code | Ship To Customer Name      | Create Date         |
                | EXTDIS2370012| 123456789     | ROUTINE  | OPEN        | 2025-01-01            | B2346         | Advanced Medical Center    | 2025-01-01T11:30:00 |
                | EXTDIS237002 | 123456789     | STAT     | IN_PROGRESS | 2025-01-02            | B2346         | Advanced Medical Center    | 2025-01-01T01:00:00 |
                | EXTDIS237007 | 123456789     | ASAP     | IN_PROGRESS | 2025-01-02            | B2346         | Advanced Medical Center    | 2025-01-01T06:00:00 |
                | EXTDIS237008 | 123456789     | ASAP     | OPEN        | 2025-01-01            | B2346         | Advanced Medical Center    | 2025-01-01T07:00:00 |
                | EXTDIS237005 | 123456789     | ASAP     | IN_PROGRESS | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T04:00:00 |
                | EXTDIS237006 | 123456789     | ROUTINE  | OPEN        | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T05:00:00 |
                | EXTDIS237001 | 123456789     | STAT     | IN_PROGRESS | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T00:00:00 |
                | EXTDIS237003 | 123456789     | STAT     | OPEN        | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T02:00:00 |
                | EXTDIS237004 | 123456789     | ROUTINE  | IN_PROGRESS | 2025-01-02            | A1235         | Creative Testing Solutions | 2025-01-01T03:00:00 |
                | EXTDIS2370011| 123456789     | STAT     | OPEN        | 2025-01-01            | C3457         | Pioneer Health Services    | 2025-01-01T10:00:00 |
                | EXTDIS2370010| 123456789     | ROUTINE  | IN_PROGRESS | NULL_VALUE            | C3457         | Pioneer Health Services    | 2025-01-01T15:00:00 |
                | EXTDIS237009 | 123456789     | ROUTINE  | IN_PROGRESS | 2025-01-01            | C3457         | Pioneer Health Services    | 2025-01-01T13:00:00 |
            And  The sorting indicator should be at "shippingCustomerName" property in "ascending" order.
            When I request to list the Orders sorted by "shippingCustomerName" in "descending" order.
            Then I should receive the orders listed in the following order.
                | External ID  | Location Code | Priority | Status      | Desired Shipment Date | Customer Code | Ship To Customer Name      | Create Date         |
                | EXTDIS237009 | 123456789     | ROUTINE  | IN_PROGRESS | 2025-01-01            | C3457         | Pioneer Health Services    | 2025-01-01T13:00:00 |
                | EXTDIS2370010| 123456789     | ROUTINE  | IN_PROGRESS | NULL_VALUE            | C3457         | Pioneer Health Services    | 2025-01-01T15:00:00 |
                | EXTDIS2370011| 123456789     | STAT     | OPEN        | 2025-01-01            | C3457         | Pioneer Health Services    | 2025-01-01T10:00:00 |
                | EXTDIS237001 | 123456789     | STAT     | IN_PROGRESS | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T00:00:00 |
                | EXTDIS237005 | 123456789     | ASAP     | IN_PROGRESS | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T04:00:00 |
                | EXTDIS237006 | 123456789     | ROUTINE  | OPEN        | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T05:00:00 |
                | EXTDIS237003 | 123456789     | STAT     | OPEN        | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T02:00:00 |
                | EXTDIS237004 | 123456789     | ROUTINE  | IN_PROGRESS | 2025-01-02            | A1235         | Creative Testing Solutions | 2025-01-01T03:00:00 |
                | EXTDIS2370012| 123456789     | ROUTINE  | OPEN        | 2025-01-01            | B2346         | Advanced Medical Center    | 2025-01-01T11:30:00 |
                | EXTDIS237002 | 123456789     | STAT     | IN_PROGRESS | 2025-01-02            | B2346         | Advanced Medical Center    | 2025-01-01T01:00:00 |
                | EXTDIS237007 | 123456789     | ASAP     | IN_PROGRESS | 2025-01-02            | B2346         | Advanced Medical Center    | 2025-01-01T06:00:00 |
                | EXTDIS237008 | 123456789     | ASAP     | OPEN        | 2025-01-01            | B2346         | Advanced Medical Center    | 2025-01-01T07:00:00 |
            And  The sorting indicator should be at "shippingCustomerName" property in "descending" order.
            When I request to list the Orders sorted by "desiredShippingDate" in "ascending" order.
            Then I should receive the orders listed in the following order.
                | External ID  | Location Code | Priority | Status      | Desired Shipment Date | Customer Code | Ship To Customer Name      | Create Date         |
                | EXTDIS237001 | 123456789     | STAT     | IN_PROGRESS | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T00:00:00 |
                | EXTDIS237008 | 123456789     | ASAP     | OPEN        | 2025-01-01            | B2346         | Advanced Medical Center    | 2025-01-01T07:00:00 |
                | EXTDIS237009 | 123456789     | ROUTINE  | IN_PROGRESS | 2025-01-01            | C3457         | Pioneer Health Services    | 2025-01-01T13:00:00 |
                | EXTDIS2370011| 123456789     | STAT     | OPEN        | 2025-01-01            | C3457         | Pioneer Health Services    | 2025-01-01T10:00:00 |
                | EXTDIS2370012| 123456789     | ROUTINE  | OPEN        | 2025-01-01            | B2346         | Advanced Medical Center    | 2025-01-01T11:30:00 |
                | EXTDIS237003 | 123456789     | STAT     | OPEN        | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T02:00:00 |
                | EXTDIS237005 | 123456789     | ASAP     | IN_PROGRESS | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T04:00:00 |
                | EXTDIS237006 | 123456789     | ROUTINE  | OPEN        | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T05:00:00 |
                | EXTDIS237002 | 123456789     | STAT     | IN_PROGRESS | 2025-01-02            | B2346         | Advanced Medical Center    | 2025-01-01T01:00:00 |
                | EXTDIS237004 | 123456789     | ROUTINE  | IN_PROGRESS | 2025-01-02            | A1235         | Creative Testing Solutions | 2025-01-01T03:00:00 |
                | EXTDIS237007 | 123456789     | ASAP     | IN_PROGRESS | 2025-01-02            | B2346         | Advanced Medical Center    | 2025-01-01T06:00:00 |
                | EXTDIS2370010| 123456789     | ROUTINE  | IN_PROGRESS | NULL_VALUE            | C3457         | Pioneer Health Services    | 2025-01-01T15:00:00 |
            And  The sorting indicator should be at "desiredShippingDate" property in "ascending" order.
            When I request to list the Orders sorted by "createDate" in "ascending" order.
            Then I should receive the orders listed in the following order.
                | External ID  | Location Code | Priority | Status      | Desired Shipment Date | Customer Code | Ship To Customer Name      | Create Date         |
                | EXTDIS237001 | 123456789     | STAT     | IN_PROGRESS | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T00:00:00 |
                | EXTDIS237002 | 123456789     | STAT     | IN_PROGRESS | 2025-01-02            | B2346         | Advanced Medical Center    | 2025-01-01T01:00:00 |
                | EXTDIS237003 | 123456789     | STAT     | OPEN        | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T02:00:00 |
                | EXTDIS237004 | 123456789     | ROUTINE  | IN_PROGRESS | 2025-01-02            | A1235         | Creative Testing Solutions | 2025-01-01T03:00:00 |
                | EXTDIS237005 | 123456789     | ASAP     | IN_PROGRESS | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T04:00:00 |
                | EXTDIS237006 | 123456789     | ROUTINE  | OPEN        | 2025-01-01            | A1235         | Creative Testing Solutions | 2025-01-01T05:00:00 |
                | EXTDIS237007 | 123456789     | ASAP     | IN_PROGRESS | 2025-01-02            | B2346         | Advanced Medical Center    | 2025-01-01T06:00:00 |
                | EXTDIS237008 | 123456789     | ASAP     | OPEN        | 2025-01-01            | B2346         | Advanced Medical Center    | 2025-01-01T07:00:00 |
                | EXTDIS2370011| 123456789     | STAT     | OPEN        | 2025-01-01            | C3457         | Pioneer Health Services    | 2025-01-01T10:00:00 |
                | EXTDIS2370012| 123456789     | ROUTINE  | OPEN        | 2025-01-01            | B2346         | Advanced Medical Center    | 2025-01-01T11:30:00 |
                | EXTDIS237009 | 123456789     | ROUTINE  | IN_PROGRESS | 2025-01-01            | C3457         | Pioneer Health Services    | 2025-01-01T13:00:00 |
                | EXTDIS2370010| 123456789     | ROUTINE  | IN_PROGRESS | NULL_VALUE            | C3457         | Pioneer Health Services    | 2025-01-01T15:00:00 |
            And  The sorting indicator should be at "createDate" property in "ascending" order.
            When I request to list the Orders sorted by "orderNumber" in "ascending" order.
            Then I should receive the orders listed by "orderNumber" in "ascending" order.
            And  The sorting indicator should be at "orderNumber" property in "ascending" order.
            When I request to list the Orders sorted by "orderNumber" in "descending" order.
            Then I should receive the orders listed by "orderNumber" in "descending" order.
            And  The sorting indicator should be at "orderNumber" property in "descending" order.






