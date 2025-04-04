@AOA-89
Feature: Filter Shipments

    Background:
        Given I have removed from the database all the configurations for the location "123456700_TEST".
        And I have removed from the database all shipments which code contains with "DIS33400".

#    Rule: I should be able to filter the shipment lists by specific criteria.
#        Rule: The system should not enable the Apply options until at least one filter criteria is chosen.
#    Rule: I should get shipments default sorting as: “status” descending and “shipmentDate” ascending.
#        @ui @DIS-334
#        Scenario: Filter shipments through UI with multiple criteria
#            Given The location "123456700_TEST" is configured with prefix "BPM_TEST", shipping code "DIS334001", and prefix configuration "Y".
#            And I request to create a new shipment with the values:
#                | Field                           | Value                         |
#                | Customer                        | Bio Products                  |
#                | Product Type                    | RP NONINJECTABLE REFRIGERATED |
#                | Carton Tare Weight              | 1000                          |
#                | Shipment Date         | <tomorrow>                    |
#                | Transportation Reference Number | 111222333                     |
#            When I am on the Shipment List Page.
#            Then The Filter Apply button should be "disabled".
#            When I select the following filter criteria:
#                | Customer        | BioLife Plasma Services |
#                | Product Type    | Recovered Plasma        |
#                | Location        | ABC1                    |
#                | Shipment Status | OPEN                    |
#            And I enter shipment date range from "2024-01-01" to "2024-03-31".
#            Then The Filter Apply button should be "enabled".
#            When I click the Filter Apply button.
#            Then I should see filtered shipments matching the criteria.
#            And The shipments should be sorted by status in descending order.
#            And within same status, shipments should be sorted by shipment date in ascending order.
#            And I should see "5" filter criteria applied.


    Rule: I should be able to search by
    Shipment Number, Customer, Product Type, Shipment Status,
    Shipment Date Range, Location and Transportation Reference Number as filter Options.
        Rule: I should be able to multi-select for Customer, Product Type, Shipment Status and Location.
        @api @DIS-334
        Scenario Outline: Search for shipments by <Attribute>
            Given The location "123456700_TEST" is configured with prefix "BPM_TEST", shipping code "DIS334002", and prefix configuration "Y".
            And I request to create a new shipment with the values:
                | Field                           | Value                      |
                | Customer Code                   | 408                        |
                | Product Type                    | RP_FROZEN_WITHIN_120_HOURS |
                | Carton Tare Weight              | 1000                       |
                | Shipment Date                   | <tomorrow>                 |
                | Transportation Reference Number | 55123                      |
                | Location Code                   | 123456700_TEST             |
            When I requested the list of shipments filtering by "<Attribute>" as "<Value>".
            Then The list shipment response should contains "1" items.
            Examples:
                | Attribute                     | Value                      |
                | shipmentDateFrom              | <today>                    |
                | shipmentNumber                | <currentShipmentNumber>    |
                | shipmentCustomerList          | 408                        |
                | productTypeList               | RP_FROZEN_WITHIN_120_HOURS |
                | shipmentStatusList            | OPEN                       |
                | locationCodeList              | 123456700_TEST             |
                | shipmentDateRange             | <today>,<tomorrow>         |
                | transportationReferenceNumber | 55123                      |


    Rule: I should be notified when no search results are found.
        @api @DIS-334
        Scenario Outline: Validation failure handling
            Given I requested the list of shipments filtering by "<Attribute>" as "<Value>".
            When I receive the shipment list response.
            Then The list shipment response should contains "0" items.
            And I should receive a "CAUTION" message response "No Results Found".
            Examples:
                | Attribute            | Value                 |
                | shipmentDateFrom     | 2030-01-01            |
                | shipmentNumber       | 111111                |
                | shipmentCustomerList | 111,222,333           |
                | productTypeList      | WRONG1, WRONG2        |
                | shipmentStatusList   | WRONG1, WRONG2        |
                | locationCodeList     | WRONG1, WRONG2        |
                | shipmentDateRange    | 2030-01-01,2030-01-01 |



      ###### Acceptance criteria covered by UI Unit tests ######

    # I should be able to reset the applied filter criteria.
    # The system should not enable the Apply options until at least one filter criteria is chosen.
    #  I should be able to multi-select for Customer, Product Type, Shipment Status and Location.
    #  The other filter options must be disabled when filtering by the Shipment Number.
    #  I should be able to enter the shipment date range manually or select from the integrated component.
    #  I should be able to identify the number of fields used to select the filter criteria.
    #  I should not be able to apply filters if any validation fails and notified when a failure occurs.
    #  The Shipment Number, Location, Transportation Reference Number, Customer, Product Type, Shipment Date, and Status should be available.
