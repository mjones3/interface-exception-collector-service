@AOA-89
Feature: Filter Shipments

    Background:
        Given I have removed from the database all shipments which code contains with "DIS33400".

        Rule: I should be able to filter the shipment lists by specific criteria.
        Rule: I should get shipments default sorting as: “status” descending and “shipmentDate” ascending.
        @ui @DIS-334
        Scenario: Filter shipments through UI with multiple criteria
            Given The location "ABC1" is configured with prefix "PRE1", shipment code "DIS334002", shipment quantity "0", and prefix configuration "YES".
            And I request to create a new shipment with the values:
                | Field                           | Value      |
                | Customer                        | CUST001    |
                | Product Type                    | PLASMA     |
                | Carton Tare Weight              | 1000g      |
                | Shipment Date                   | <tomorrow> |
                | Transportation Reference Number | 111222333  |
            And I am at the List Shipment Page.
            When I select the following filter criteria:
                | Customer        | BioLife Plasma Services |
                | Product Type    | Recovered Plasma        |
                | Location        | ABC1                    |
                | Shipment Status | OPEN                    |
            And I enter shipment date range from "2024-01-01" to "2024-03-31".
            Then The Filter Apply button should be enabled.
            When I click the Filter Apply button.
            Then I should see filtered shipments matching the criteria.
            And The shipments should be sorted by status in descending order.
            And within same status, shipments should be sorted by shipment date in ascending order.
            And I should see "5" filter criteria applied.


        Rule: I should be able to search by
            Shipment Number, Customer, Product Type, Shipment Status,
            Shipment Date Range, Location and Transportation Reference Number as filter Options.
        @api @DIS-334
        Scenario Outline: Search for shipments by <Attribute>
            Given The location "ABC1" is configured with prefix "PRE1", shipment code "DIS334002", shipment quantity "0", and prefix configuration "YES".
            And I request to create a new shipment with the values:
                | Field                           | Value      |
                | Customer                        | CUST001    |
                | Product Type                    | PLASMA     |
                | Carton Tare Weight              | 1000g      |
                | Shipment Date                   | <tomorrow> |
                | Transportation Reference Number | 111222333  |
            When I requested the list of shipments filtering by "<Attribute>" as "<Value>".
            Then The list shipment response should contains "1" items.
            Examples:
                | Attribute                       | Value          |
                | shipmentDate                    | <CURRENT_DATE> |
                | Shipment Number                 | 000            |
                | Customer                        | CUST001        |
                | Product Type                    | PLASMA         |
                | Shipment Status                 | OPEN           |
                | Shipment Date Range             | XXXXX          |
                | Location                        | ABC1           |
                | Transportation Reference Number | 000            |


        Rule: I should be notified when no search results are found.
        @api @DIS-334
        Scenario Outline: Validation failure handling
            Given I requested the list of shipments filtering by "<Attribute>" as "<Value>".
            When I receive the shipment list response.
            Then The list shipment response should contains "0" items.
            And I should receive a "WARN" message response "No results found".
            Examples:
                | Attribute                       | Value   |
                | shipmentDate                    | xxxxxxx |
                | Shipment Number                 | 000     |
                | Customer                        | XXX     |
                | Product Type                    | XXX     |
                | Shipment Status                 | XXXX    |
                | Shipment Date Range             | XXXXX   |
                | Location                        | XXXX    |
                | Transportation Reference Number | 000     |



      ###### Acceptance criteria covered by UI Unit tests ######

    # I should be able to reset the applied filter criteria.
    # The system should not enable the Apply options until at least one filter criteria is chosen.
    #  I should be able to multi-select for Customer, Product Type, Shipment Status and Location.
    #  The other filter options must be disabled when filtering by the Shipment Number.
    #  I should be able to enter the shipment date range manually or select from the integrated component.
    #  I should be able to identify the number of fields used to select the filter criteria.
    #  I should not be able to apply filters if any validation fails and notified when a failure occurs.
    #  The Shipment Number, Location, Transportation Reference Number, Customer, Product Type, Shipment Date, and Status should be available.
