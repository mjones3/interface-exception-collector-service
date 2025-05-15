@AOA-89
Feature: Print Shipment Summary

    Background:
        Given I have removed from the database all the configurations for the location "123456789_DIS343".
        And I have removed from the database all shipments which code contains with "DIS34300".
        And I have removed from the database all shipments from location "123456789" with transportation ref number "DIS-343".
        And The location "123456789_DIS343" is configured with prefix "DIS_343", shipping code "DIS34300", carton prefix "BPM" and prefix configuration "Y".
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
            | system_configuration_key  | system_configuration_value                                                                                                              | process_type                |
            | TESTING_STATEMENT_TXT     | Products packed, inspected and found satisfactory by: {employeeName}                                                                    | RPS_CARTON_PACKING_SLIP     |
            | USE_SIGNATURE             | Y                                                                                                                                       | RPS_CARTON_PACKING_SLIP     |
            | USE_TRANSPORTATION_NUMBER | Y                                                                                                                                       | RPS_CARTON_PACKING_SLIP     |
            | USE_LICENSE_NUMBER        | Y                                                                                                                                       | RPS_CARTON_PACKING_SLIP     |
            | USE_TESTING_STATEMENT     | Y                                                                                                                                       | RPS_CARTON_PACKING_SLIP     |
            | TESTING_STATEMENT_TXT     | All products in this shipment meet FDA and {bloodCenterName} testing requirements. These units are acceptable for further manufacture.  | RPS_SHIPPING_SUMMARY_REPORT |
            | HEADER_SECTION_STATEMENT_TXT     | bla bla bla   | RPS_SHIPPING_SUMMARY_REPORT |

    @api @DIS-346
    Rule: I should be able to print the shipping summary if needed when the shipment status is CLOSED.
    Rule: I should be able to view the Customer details, Shipment Details, Product Details and Carton Information on the Shipment summary Report.
    Rule: The header section content displayed on the shipment summary is configurable.
    Scenario: Print shipment summary for closed shipment
        Given I have a closed shipment with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>", Location Code as "<Location Code>" and the unit numbers as "<unit_number>" and product codes as "<product_code>".
        When I request to print the shipping summary report.
        Then I should have the following information in the shipping summary report response:
            | Section Name             | Section Content                                                                                                                       |
            | Header Section           | bla bla bla                                                                                                   |
            | Report Title             | Plasma Shipment Summary Report                                                                                                        |
            | Ship To                  |                                                                                                                                       |
            | Ship Fro                 |                                                                                                                                       |
            | Shipment Details         |                                                                                                                                       |
            | Product Shipped          |                                                                                                                                       |
            | Shipment Information     |                                                                                                                                       |
            | Testing Statement        | All products in this shipment meet FDA and ARC-One Solutions testing requirements. These units are acceptable for further manufacture.|
            | Shipment Closing Details |                                                                                                                                       |
        And I should see the following Carton Information information in the shipping summary report response:
            | Carton Number   |Product Code |Product Description | Total Number of Products |
            |                 |             |                    |                          |



    Rule: I should not be able to print a shipping summary when the shipment status is not CLOSED.
    @api @DIS-346
    Scenario Outline: Cannot print shipping summary for non-closed shipment
        Given I have a shipment created with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>" and Location Code as "<Location Code>".
        When I request to print the shipping summary report.
        Then I should receive a "WARN" message response "Shipping summary cannot be print".
        Examples:
        |Shipment Status |error type| error message |
        | OPEN           |          |               |
        | IN_PROGRESS    |          |               |
        | PROCESSING     |          |               |


    @ui @DIS-346
    Rule: I should be able to view the Customer details, Shipment Details, Product Details and Carton Information on the Shipment summary Report.
    Rule: The header section content displayed on the shipment summary is configurable.
    Scenario: View shipping summary for closed shipment
        Given I have a closed shipment with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>", Location Code as "<Location Code>" and the unit numbers as "<unit_number>" and product codes as "<product_code>".
        When I navigate to the shipment details page for the last shipment created.
        Then The print option should be "enabled".
        When I choose to print the shipping summary report.
        Then I should see the following information in the shipping summary report:
            | Section Name             | Section Content                                                                                                                                 |
            | Report Title             | Plasma Shipment Summary Report                                                                                                                  |
            | Header Section           | bla bla bla                                                                                                   |
            | Ship To                  |                                                                                                                                                 |
            | Ship From                |                                                                                                                                                 |
            | Shipment Details         |                                                                                                                                                 |
            | Product Shipped          |                                                                                                                                                 |
            | Shipment Information     |                                                                                                                                                 |
            | Testing Statement        | All products in this shipment meet FDA and [insert blood center name] testing requirements. These units are acceptable for further manufacture. |
            | Shipment Closing Details |                                                                                                                                                 |
        And I should see the following Carton Information information in the shipping summary report:
            | Carton Number   |Product Code |Product Description | Total Number of Products |
            |                 |             |                    |                          |


    @api @DIS-346
    Rule: The Transportation Reference Number displayed on the shipment summary is configurable.
    Rule: The Transportation Reference Number must be printed only if configured.
    Scenario Outline: Display configured information in the report
        Given I have a closed shipment with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>", Location Code as "<Location Code>" and the unit numbers as "<unit_number>" and product codes as "<product_code>".
        And The system configuration "<system_configuration_key>" is configured as "<system_configuration_value>" for the element "<element>" in the process type "RPS_SHIPPING_SUMMARY_REPORT".
        When I request to print the shipping summary report.
        Then The element "<element>" for the property "<element property>" "<should_should_not>" be display in the shipping summary report.
        Examples:
            | system_configuration_key  | system_configuration_value | element property                     | element                         | should_should_not | Customer Code | Product Type               | Carton Tare Weight | Shipment Date | Transportation Reference Number | Location Code | configured_min_products | unit_number                 | product_code       | product_type                                           |
            | USE_TRANSPORTATION_NUMBER | Y                          | displayTransportationReferenceNumber | Transportation Reference Number | should            | 409           | RP_NONINJECTABLE_LIQUID_RT | 1000               | <tomorrow>    | DIS-343                         | 123456789     | 2                       | W036898786808,W036898786809 | E2488V00, E2488V00 | RP_FROZEN_WITHIN_120_HOURS, RP_FROZEN_WITHIN_120_HOURS |
            | USE_TRANSPORTATION_NUMBER | N                          | displayTransportationReferenceNumber | Transportation Reference Number | should not        | 409           | RP_NONINJECTABLE_LIQUID_RT | 1000               | <tomorrow>    | DIS-343                         | 123456789     | 2                       | W036898786808,W036898786809 | E2488V00, E2488V00 | RP_FROZEN_WITHIN_120_HOURS, RP_FROZEN_WITHIN_120_HOURS |


        @api @DIS-346
        Rule: The header section content displayed on the shipment summary is configurable.
        Scenario Outline: Display Header Section configured information in the report
            Given I have a closed shipment with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>", Location Code as "<Location Code>" and the unit numbers as "<unit_number>" and product codes as "<product_code>".
            And The system configuration "<system_configuration_key>" is configured as "<system_configuration_value>" for the element "<element>" in the process type "RPS_SHIPPING_SUMMARY_REPORT".
            When I request to print the shipping summary report.
            Then The element "<element>" for the property "<element property>" "<should_should_not>" be display in the shipping summary report.
            Examples:
                | system_configuration_key     | system_configuration_value | element property                     | element                         | should_should_not | Customer Code | Product Type               | Carton Tare Weight | Shipment Date | Transportation Reference Number | Location Code | configured_min_products | unit_number                 | product_code       | product_type                                           |
                | HEADER_SECTION_STATEMENT_TXT | BLA BLA BLA                | displayTransportationReferenceNumber | Transportation Reference Number | should            | 409           | RP_NONINJECTABLE_LIQUID_RT | 1000               | <tomorrow>    | DIS-343                         | 123456789     | 2                       | W036898786808,W036898786809 | E2488V00, E2488V00 | RP_FROZEN_WITHIN_120_HOURS, RP_FROZEN_WITHIN_120_HOURS |
                | HEADER_SECTION_STATEMENT_TXT | ZZZZZZZZ                   | displayTransportationReferenceNumber | Transportation Reference Number | should not        | 409           | RP_NONINJECTABLE_LIQUID_RT | 1000               | <tomorrow>    | DIS-343                         | 123456789     | 2                       | W036898786808,W036898786809 | E2488V00, E2488V00 | RP_FROZEN_WITHIN_120_HOURS, RP_FROZEN_WITHIN_120_HOURS |




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
            | system_configuration_key  | system_configuration_value                                            | process_type            |
            | TESTING_STATEMENT_TXT     | Products packed, inspected and found satisfactory by: {employeeName}  | RPS_CARTON_PACKING_SLIP |
            | USE_SIGNATURE             | Y                                                                     | RPS_CARTON_PACKING_SLIP |
            | USE_TRANSPORTATION_NUMBER | Y                                                                     | RPS_CARTON_PACKING_SLIP |
            | USE_LICENSE_NUMBER        | Y                                                                     | RPS_CARTON_PACKING_SLIP |
            | USE_TESTING_STATEMENT     | Y                                                                     | RPS_CARTON_PACKING_SLIP |








