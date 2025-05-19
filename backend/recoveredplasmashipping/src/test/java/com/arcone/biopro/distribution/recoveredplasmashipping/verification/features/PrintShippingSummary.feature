@AOA-89
Feature: Print Shipment Summary

    Background:
        Given I have removed from the database all the configurations for the location "123456789_DIS346".
        And I have removed from the database all shipments which code contains with "DIS34600".
        And I have removed from the database all shipments from location "123456789" with transportation ref number "DIS-346".
        And The location "123456789_DIS346" is configured with prefix "DIS_346", shipping code "DIS34600", carton prefix "BPM" and prefix configuration "Y".
        And I have reset the shipment product criteria to have the following values:
            | recovered_plasma_shipment_criteria_id | type                    | value | message                                   | message_type |
            | 1                                     | MINIMUM_VOLUME          | 165   | Product Volume does not match criteria    | WARN         |
            | 2                                     | MINIMUM_VOLUME          | 200   | Product Volume does not match criteria    | WARN         |
            | 1                                     | MAXIMUM_UNITS_BY_CARTON | 20    | Maximum number of products exceeded       | WARN         |
            | 2                                     | MAXIMUM_UNITS_BY_CARTON | 20    | Maximum number of products exceeded       | WARN         |
            | 3                                     | MAXIMUM_UNITS_BY_CARTON | 30    | Maximum number of products exceeded       | WARN         |
            | 4                                     | MAXIMUM_UNITS_BY_CARTON | 20    | Maximum number of products exceeded       | WARN         |
            | 5                                     | MAXIMUM_UNITS_BY_CARTON | 20    | Maximum number of products exceeded       | WARN         |
            | 6                                     | MAXIMUM_UNITS_BY_CARTON | 20    | Maximum number of products exceeded       | WARN         |
            | 5                                     | MINIMUM_UNITS_BY_CARTON | 15    | Minimum number of products does not match | WARN         |
            | 2                                     | MINIMUM_UNITS_BY_CARTON | 20    | Minimum number of products does not match | WARN         |
            | 3                                     | MINIMUM_UNITS_BY_CARTON | 25    | Minimum number of products does not match | WARN         |
            | 4                                     | MINIMUM_UNITS_BY_CARTON | 15    | Minimum number of products does not match | WARN         |
            | 1                                     | MINIMUM_UNITS_BY_CARTON | 20    | Minimum number of products does not match | WARN         |
            | 6                                     | MINIMUM_UNITS_BY_CARTON | 15    | Minimum number of products does not match | WARN         |
        And I have reset the carton packing slip system configurations following values:
            | system_configuration_key     | system_configuration_value                                                                                                             | process_type                |
            | TESTING_STATEMENT_TXT        | Products packed, inspected and found satisfactory by: {employeeName}                                                                   | RPS_CARTON_PACKING_SLIP     |
            | USE_SIGNATURE                | Y                                                                                                                                      | RPS_CARTON_PACKING_SLIP     |
            | USE_TRANSPORTATION_NUMBER    | Y                                                                                                                                      | RPS_CARTON_PACKING_SLIP     |
            | USE_LICENSE_NUMBER           | Y                                                                                                                                      | RPS_CARTON_PACKING_SLIP     |
            | USE_TESTING_STATEMENT        | Y                                                                                                                                      | RPS_CARTON_PACKING_SLIP     |
            | TESTING_STATEMENT_TXT        | All products in this shipment meet FDA and ARC-One Solutions testing requirements. These units are acceptable for further manufacture. | RPS_SHIPPING_SUMMARY_REPORT |
            | HEADER_SECTION_TXT           | ARC-One Solutions                                                                                                                      | RPS_SHIPPING_SUMMARY_REPORT |
            | USE_TESTING_STATEMENT        | Y                                                                                                                                      | RPS_SHIPPING_SUMMARY_REPORT |
            | USE_TRANSPORTATION_NUMBER    | Y                                                                                                                                      | RPS_SHIPPING_SUMMARY_REPORT |
            | USE_HEADER_SECTION           | Y                                                                                                                                      | RPS_SHIPPING_SUMMARY_REPORT |


    @api @DIS-346
    Rule: I should be able to print the shipping summary if needed when the shipment status is CLOSED.
    Rule: I should be able to view the Customer details, Shipment Details, Product Details and Carton Information on the Shipment summary Report.
        Rule: The header section content displayed on the shipment summary is configurable.
        Scenario: Print shipment summary for closed shipment
            Given I have a "CLOSED" shipment with the Customer Code as "409" , Product Type as "RP_NONINJECTABLE_LIQUID_RT", Carton Tare Weight as "100", Shipment Date as "<tomorrow>", Transportation Reference Number as "DIS-346", Location Code as "123456789_DIS346" and the unit numbers as "W036898346757,W036898346758,W036898346756" and product codes as "E6022V00,E2534V00,E5880V00" and product types "RP_NONINJECTABLE_LIQUID_RT,RP_NONINJECTABLE_FROZEN,RP_FROZEN_WITHIN_72_HOURS".
            When I request to print the shipping summary report.
            Then I should have the following information in the shipping summary report response:
                | Section Name                                      | Section Content                                                                                                                        |
                | Header Section                                    | ARC-One Solutions                                                                                                                      |
                | Report Title                                      | Plasma Shipment Summary Report                                                                                                         |
                | Ship To Customer Name                             | Southern Biologics                                                                                                                     |
                | Ship To Customer Address                          | 4801 Woodlane Circle Tallahassee, FL, 32303 USA                                                                                        |
                | Ship From Facility Name                           | ARC-One Solutions                                                                                                                      |
                | Ship From Facility Address                        | address_line_1 city, state, 000000 USA                                                                                                 |
                | Ship From Phone                                   | 123-456-7894                                                                                                                           |
                | Shipment Details Transportation Reference Number  | DIS-346                                                                                                                                 |
                | Shipment Details Shipment Number Prefix           | DIS_346DIS34600                                                                                                                        |
                | Shipment Closed Date/Time                         | <tomorrow_formatted>                                                                                                                                     |
                | Shipment Details Product Type                    | RP NONINJECTABLE LIQUID RT                                                                                                              |
                | Shipment Details Product Code                    | E2534V00, E5880V00, E6022V00                                                                                                            |
                | Shipment Details Total Number of Cartons         | 3                                                                                                                                       |
                | Shipment Details Total Number of Products        | 3                                                                                                                                       |
                | Carton Information Carton Number Prefix           | BPMMH1,BPMMH1,BPMMH1                                                                                                                   |
                | Carton Information Product Code                   | E6022V00,E2534V00,E5880V00                                                                                                             |
                | Carton Information Product Description            | RP NONINJECTABLE LIQUID RT,RP NONINJECTABLE FROZEN,RP FROZEN WITHIN 72 HOURS                                                           |
                | Carton Information Total Number of Products       | 1,1,1                                                                                                                                  |
                | Testing Statement                                 | All products in this shipment meet FDA and ARC-One Solutions testing requirements. These units are acceptable for further manufacture. |
                | Shipment Closing Details Employee Name           | 5db1da0b-6392-45ff-86d0-17265ea33226                                                                                                   |
                | Shipment Closing Details Date                    | <today_formatted>                                                                                                                      |


    Rule: I should not be able to print a shipping summary when the shipment status is not CLOSED.
    @api @DIS-346
    Scenario Outline: Cannot print shipping summary for non-closed shipment
        Given I have a "<Shipment Status>" shipment with the Customer Code as "409" , Product Type as "RP_NONINJECTABLE_LIQUID_RT", Carton Tare Weight as "100", Shipment Date as "<tomorrow>", Transportation Reference Number as "DIS-346", Location Code as "123456789_DIS346" and the unit numbers as "W036898346757,W036898346758,W036898346756" and product codes as "E6022V00,E2534V00,E5880V00" and product types "RP_NONINJECTABLE_LIQUID_RT,RP_NONINJECTABLE_FROZEN,RP_FROZEN_WITHIN_72_HOURS".
        When I request to print the shipping summary report.
        Then I should receive a "SYSTEM" message response "Shipping Summary Report generation error. Contact Support.".
        Examples:
            | Shipment Status |
            | OPEN            |
            | IN_PROGRESS     |
            | PROCESSING      |


#        @ui @DIS-346
#        Rule: I should be able to view the Customer details, Shipment Details, Product Details and Carton Information on the Shipment summary Report.
#        Rule: The header section content displayed on the shipment summary is configurable.
#        Scenario: View shipping summary for closed shipment
#            Given I have a closed shipment with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>", Location Code as "<Location Code>" and the unit numbers as "<unit_number>" and product codes as "<product_code>".
#            When I navigate to the shipment details page for the last shipment created.
#            Then The print option should be "enabled".
#            When I choose to print the shipping summary report.
#            Then I should see the following information in the shipping summary report:
#                | Section Name             | Section Content                                                                                                                                 |
#                | Report Title             | Plasma Shipment Summary Report                                                                                                                  |
#                | Header Section           | bla bla bla                                                                                                                                     |
#                | Ship To                  |                                                                                                                                                 |
#                | Ship From                |                                                                                                                                                 |
#                | Shipment Details         |                                                                                                                                                 |
#                | Product Shipped          |                                                                                                                                                 |
#                | Shipment Information     |                                                                                                                                                 |
#                | Testing Statement        | All products in this shipment meet FDA and [insert blood center name] testing requirements. These units are acceptable for further manufacture. |
#                | Shipment Closing Details |                                                                                                                                                 |
#            And I should see the following Carton Information information in the shipping summary report:
#                | Carton Number | Product Code | Product Description | Total Number of Products |
#                |               |              |                     |                          |



    Rule: The Transportation Reference Number displayed on the shipment summary is configurable.
    Rule: The Transportation Reference Number must be printed only if configured.
    @api @DIS-346
    Scenario Outline: Display configured information in the report
        Given I have a "CLOSED" shipment with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>", Location Code as "<Location Code>" and the unit numbers as "<unit_number>" and product codes as "<product_code>" and product types "<product_type>".
        And The system configuration "<system_configuration_key>" is configured as "<system_configuration_value>" for the element "<element>" in the process type "RPS_SHIPPING_SUMMARY_REPORT".
        When I request to print the shipping summary report.
        Then The element "<element>" for the property "<element property>" "<should_should_not>" be visible in the shipping summary report.
        Examples:
            | system_configuration_key  | system_configuration_value | element property                          | element                         | should_should_not | Customer Code | Product Type               | Carton Tare Weight | Shipment Date | Transportation Reference Number | Location Code    | unit_number                 | product_code       | product_type                                        |
            | USE_TRANSPORTATION_NUMBER | Y                          | shipmentDetailDisplayTransportationNumber | Transportation Reference Number | should            | 409           | RP_NONINJECTABLE_LIQUID_RT | 1000               | <tomorrow>    | DIS-346                         | 123456789_DIS346 | W036898346757,W036898346758 | E2488V00, E2488V00 | RP_NONINJECTABLE_LIQUID_RT, RP_NONINJECTABLE_FROZEN |
            | USE_TRANSPORTATION_NUMBER | N                          | shipmentDetailDisplayTransportationNumber | Transportation Reference Number | should not        | 409           | RP_NONINJECTABLE_LIQUID_RT | 1000               | <tomorrow>    | DIS-346                         | 123456789_DIS346 | W036898346757,W036898346758 | E2488V00, E2488V00 | RP_NONINJECTABLE_LIQUID_RT, RP_NONINJECTABLE_FROZEN |
            | USE_HEADER_SECTION        | N                          | displayHeader                             | Header Section                  | should not        | 409           | RP_NONINJECTABLE_LIQUID_RT | 1000               | <tomorrow>    | DIS-346                         | 123456789_DIS346 | W036898346757,W036898346758 | E2488V00, E2488V00 | RP_NONINJECTABLE_LIQUID_RT, RP_NONINJECTABLE_FROZEN |
            | USE_HEADER_SECTION        | Y                          | displayHeader                             | Header Section                  | should            | 409           | RP_NONINJECTABLE_LIQUID_RT | 1000               | <tomorrow>    | DIS-346                         | 123456789_DIS346 | W036898346757,W036898346758 | E2488V00, E2488V00 | RP_NONINJECTABLE_LIQUID_RT, RP_NONINJECTABLE_FROZEN |



    Rule: The header section content displayed on the shipment summary is configurable.
    @api @DIS-346
    Scenario Outline: Display Header Section configured information in the report
        Given I have a "CLOSED" shipment with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>", Location Code as "<Location Code>" and the unit numbers as "<unit_number>" and product codes as "<product_code>" and product types "<product_type>".
        And The system configuration "<system_configuration_key>" is configured as "<system_configuration_value>" for the element "<element>" in the process type "RPS_SHIPPING_SUMMARY_REPORT".
        When I request to print the shipping summary report.
        Then The element "<element>" for the property "<element property>" should have the value "<system_configuration_value>" in the shipping summary report.
        Examples:
            | system_configuration_key | system_configuration_value | element property | element           | Customer Code | Product Type               | Carton Tare Weight | Shipment Date | Transportation Reference Number | Location Code    | unit_number                 | product_code       | product_type                                        |
            | HEADER_SECTION_TXT       | Header Section DIS-346     | headerStatement  | Header Section    | 409           | RP_NONINJECTABLE_LIQUID_RT | 1000               | <tomorrow>    | DIS-346                         | 123456789_DIS346 | W036898346757,W036898346758 | E2488V00, E2488V00 | RP_NONINJECTABLE_LIQUID_RT, RP_NONINJECTABLE_FROZEN |
            | TESTING_STATEMENT_TXT    | Testing Statement DIS-346  | testingStatement | Testing Statement | 409           | RP_NONINJECTABLE_LIQUID_RT | 1000               | <tomorrow>    | DIS-346                         | 123456789_DIS346 | W036898346757,W036898346758 | E2488V00, E2488V00 | RP_NONINJECTABLE_LIQUID_RT, RP_NONINJECTABLE_FROZEN |





    Scenario: Reset default configurations
        Given I have reset the shipment product criteria to have the following values:
            | recovered_plasma_shipment_criteria_id | type                    | value | message                                   | message_type |
            | 1                                     | MINIMUM_VOLUME          | 165   | Product Volume does not match criteria    | WARN         |
            | 2                                     | MINIMUM_VOLUME          | 200   | Product Volume does not match criteria    | WARN         |
            | 1                                     | MAXIMUM_UNITS_BY_CARTON | 20    | Maximum number of products exceeded       | WARN         |
            | 2                                     | MAXIMUM_UNITS_BY_CARTON | 20    | Maximum number of products exceeded       | WARN         |
            | 3                                     | MAXIMUM_UNITS_BY_CARTON | 30    | Maximum number of products exceeded       | WARN         |
            | 4                                     | MAXIMUM_UNITS_BY_CARTON | 20    | Maximum number of products exceeded       | WARN         |
            | 5                                     | MAXIMUM_UNITS_BY_CARTON | 20    | Maximum number of products exceeded       | WARN         |
            | 6                                     | MAXIMUM_UNITS_BY_CARTON | 20    | Maximum number of products exceeded       | WARN         |
            | 5                                     | MINIMUM_UNITS_BY_CARTON | 15    | Minimum number of products does not match | WARN         |
            | 2                                     | MINIMUM_UNITS_BY_CARTON | 20    | Minimum number of products does not match | WARN         |
            | 3                                     | MINIMUM_UNITS_BY_CARTON | 25    | Minimum number of products does not match | WARN         |
            | 4                                     | MINIMUM_UNITS_BY_CARTON | 15    | Minimum number of products does not match | WARN         |
            | 1                                     | MINIMUM_UNITS_BY_CARTON | 20    | Minimum number of products does not match | WARN         |
            | 6                                     | MINIMUM_UNITS_BY_CARTON | 15    | Minimum number of products does not match | WARN         |
        And I have reset the carton packing slip system configurations following values:
            | system_configuration_key     | system_configuration_value                                                                                                             | process_type                |
            | TESTING_STATEMENT_TXT        | Products packed, inspected and found satisfactory by: {employeeName}                                                                   | RPS_CARTON_PACKING_SLIP     |
            | USE_SIGNATURE                | Y                                                                                                                                      | RPS_CARTON_PACKING_SLIP     |
            | USE_TRANSPORTATION_NUMBER    | Y                                                                                                                                      | RPS_CARTON_PACKING_SLIP     |
            | USE_LICENSE_NUMBER           | Y                                                                                                                                      | RPS_CARTON_PACKING_SLIP     |
            | USE_TESTING_STATEMENT        | Y                                                                                                                                      | RPS_CARTON_PACKING_SLIP     |
            | TESTING_STATEMENT_TXT        | All products in this shipment meet FDA and ARC-One Solutions testing requirements. These units are acceptable for further manufacture. | RPS_SHIPPING_SUMMARY_REPORT |
            | HEADER_SECTION_TXT           | ARC-One Solutions                                                                                                                      | RPS_SHIPPING_SUMMARY_REPORT |
            | USE_TESTING_STATEMENT        | Y                                                                                                                                      | RPS_SHIPPING_SUMMARY_REPORT |
            | USE_TRANSPORTATION_NUMBER    | Y                                                                                                                                      | RPS_SHIPPING_SUMMARY_REPORT |
            | USE_HEADER_SECTION           | Y                                                                                                                                      | RPS_SHIPPING_SUMMARY_REPORT |







