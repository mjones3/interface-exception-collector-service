@AOA-89
Feature: Carton Packing Slip Printing

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
        | system_configuration_key  | system_configuration_value                                            | process_type            |
        | TESTING_STATEMENT_TXT     | Products packed, inspected and found satisfactory by: {employeeName}  | RPS_CARTON_PACKING_SLIP |
        | USE_SIGNATURE             | Y                                                                     | RPS_CARTON_PACKING_SLIP |
        | USE_TRANSPORTATION_NUMBER | Y                                                                     | RPS_CARTON_PACKING_SLIP |
        | USE_LICENSE_NUMBER        | Y                                                                     | RPS_CARTON_PACKING_SLIP |
        |USE_TESTING_STATEMENT      | Y                                                                     | RPS_CARTON_PACKING_SLIP |




        Rule: The system should trigger the printing of the carton automatically once a carton is closed.
        Rule: I should be able to print the carton packing slip with the carton details and products information.
        @ui @DIS-343 @disabled
        Scenario Outline: Automatic printing when carton is closed
            Given I have an empty carton created with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>" and Location Code as "<Location Code>".
            And The Minimum Number of Units in Carton is configured as "<configured_min_products>" products for the customer code "<Customer Code>" and product type "<Product Type>".
            And I have verified product with the unit number "<unit_number>", product code "<product_code>" and product type "<product_type>".
            And I navigate to the Manage Carton Products page for the carton sequence number <Carton Sequence Number>.
            When I choose to close the carton.
            Then I should be redirected to the Shipment Details page.
            And  I should be able to see the Carton Packing Slip details.
            Examples:
                | Customer Code | Product Type              | Carton Tare Weight | Shipment Date | Transportation Reference Number | Location Code | configured_min_products | unit_number                 | product_code       | product_type                                           | Carton Sequence Number |
                | 409           | RP_NONINJECTABLE_LIQUID_RT | 1000               | <tomorrow>    | DIS-343                         | 123456789     | 2                      | W036898786808,W036898786809 | E2488V00, E2488V00 | RP_NONINJECTABLE_LIQUID_RT, RP_NONINJECTABLE_LIQUID_RT | 1                      |


    Rule: I should be able to reprint carton packing slip if needed.
        Rule: The Testing Statement displayed on the carton packing slip must be configurable.
        @api @DIS-343
        Scenario: Manual printing of carton packing slip
            Given I have an empty carton created with the Customer Code as "409" , Product Type as "RP_NONINJECTABLE_LIQUID_RT", Carton Tare Weight as "1000", Shipment Date as "<tomorrow>", Transportation Reference Number as "DIS-343" and Location Code as "123456789".
            And The Minimum Number of Units in Carton is configured as "2" products for the customer code "409" and product type "RP_NONINJECTABLE_LIQUID_RT".
            And The system configuration "TESTING_STATEMENT_TXT" is configured as "Testing statement template: {employeeName}" for the element "testingStatement" in the process type "RPS_CARTON_PACKING_SLIP".
            And I have verified product with the unit number "W036898786808,W036898786809", product code "E2488V00, E2488V00" and product type "RP_FROZEN_WITHIN_120_HOURS, RP_FROZEN_WITHIN_120_HOURS".
            And I request to close the carton.
            When I request to print the carton packing slip.
            Then The carton packing slip should contain:
                | Response property                     | Information Type                 | Information Value                               |
                | shipFromBloodCenterName               | Blood Center Name                | ARC-One Solutions                               |
                | shipFromLicenseNumber                 | Blood Center License Number      | 2222                                            |
                | shipToCustomerName                    | Ship To: (Customer Name)         | Southern Biologics                              |
                | shipToAddress                         | Customer Address                 | 4801 Woodlane Circle Tallahassee, FL, 32303 USA |
                | shipFromBloodCenterName               | Ship From: (Blood Center Name)   | ARC-One Solutions                               |
                | shipFromLocationAddress               | Location Address                 | 444 Main St. Charlotte, NC, 28209 USA           |
                | shipmentProductDescription            | Product Type                     | RP NONINJECTABLE LIQUID RT                      |
                | cartonProductDescription              | Product Description              | LIQ CPD PLS MNI RT                              |
                | cartonProductCode                     | Product Code                     | E2488V00                                        |
                | cartonNumber                         | Carton Number Prefix             | BPMMH                                            |
                | shipmentNumber                       | Shipment Number Prefix           | BPM2765                                        |
                | cartonSequence                        | Carton Sequence                  | 1                                               |
                | packedByEmployeeId                    | Products Packed and  Verified by | 5db1da0b-6392-45ff-86d0-17265ea33226            |
#                | dateTimePacked                        | Date/Time Packed Carton Closed   | 12/21/2024 16:05                                |
                | totalProducts                         | Total Products                   | 2                                               |
                | shipmentTransportationReferenceNumber | Transportation Reference Number  | DIS-343                                         |
                | products                              | Unit Numbers                     | W036898786808,W036898786809                     |
                | products                              | Collection Date                  | 12/03/2011,12/03/2011                           |
                | products                              | Product Volume                   | 259,259                                         |
                | displaySignature                      | Signature                        | true                                            |
                | testingStatement                      | Testing statement                | Testing statement template: 5db1da0b-6392-45ff-86d0-17265ea33226             |

        Rule: The system must display the transportation reference number, if configured.
        Rule: The system must display the signature, if configured.
        Rule: The system must display the License number, if configured.
        @api @DIS-343
        Scenario Outline: Display of configurable elements on packing slip
            Given I have an empty carton created with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>" and Location Code as "<Location Code>".
            And The Minimum Number of Units in Carton is configured as "<configured_min_products>" products for the customer code "<Customer Code>" and product type "<Product Type>".
            And The system configuration "<system_configuration_key>" is configured as "<system_configuration_value>" for the element "<element>" in the process type "RPS_CARTON_PACKING_SLIP".
            And I have verified product with the unit number "<unit_number>", product code "<product_code>" and product type "<product_type>".
            And I request to close the carton.
            When I request to print the carton packing slip.
            Then The element "<element>" for the property "<element property>" "<should_should_not>" be display.
            Examples:
                | system_configuration_key  | system_configuration_value | element property                     | element                         | should_should_not | Customer Code | Product Type               | Carton Tare Weight | Shipment Date | Transportation Reference Number | Location Code | configured_min_products | unit_number                 | product_code       | product_type                                           |
                | USE_SIGNATURE             | Y                          | displaySignature                     | Signature                       | should            | 409           | RP_NONINJECTABLE_LIQUID_RT | 1000               | <tomorrow>    | DIS-343                         | 123456789     | 2                       | W036898786808,W036898786809 | E2488V00, E2488V00 | RP_FROZEN_WITHIN_120_HOURS, RP_FROZEN_WITHIN_120_HOURS |
                | USE_TRANSPORTATION_NUMBER | Y                          | displayTransportationReferenceNumber | Transportation Reference Number | should            | 409           | RP_NONINJECTABLE_LIQUID_RT | 1000               | <tomorrow>    | DIS-343                         | 123456789     | 2                       | W036898786808,W036898786809 | E2488V00, E2488V00 | RP_FROZEN_WITHIN_120_HOURS, RP_FROZEN_WITHIN_120_HOURS |
                | USE_LICENSE_NUMBER        | Y                          | displayLicenceNumber                 | License number                  | should            | 409           | RP_NONINJECTABLE_LIQUID_RT | 1000               | <tomorrow>    | DIS-343                         | 123456789     | 2                       | W036898786808,W036898786809 | E2488V00, E2488V00 | RP_FROZEN_WITHIN_120_HOURS, RP_FROZEN_WITHIN_120_HOURS |
                | USE_SIGNATURE             | N                          | displaySignature                     | Signature                       | should not        | 409           | RP_NONINJECTABLE_LIQUID_RT | 1000               | <tomorrow>    | DIS-343                         | 123456789     | 2                       | W036898786808,W036898786809 | E2488V00, E2488V00 | RP_FROZEN_WITHIN_120_HOURS, RP_FROZEN_WITHIN_120_HOURS |
                | USE_TRANSPORTATION_NUMBER | N                          | displayTransportationReferenceNumber | Transportation Reference Number | should not        | 409           | RP_NONINJECTABLE_LIQUID_RT | 1000               | <tomorrow>    | DIS-343                         | 123456789     | 2                       | W036898786808,W036898786809 | E2488V00, E2488V00 | RP_FROZEN_WITHIN_120_HOURS, RP_FROZEN_WITHIN_120_HOURS |
                | USE_LICENSE_NUMBER        | N                          | displayLicenceNumber                 | License number                  | should not        | 409           | RP_NONINJECTABLE_LIQUID_RT | 1000               | <tomorrow>    | DIS-343                         | 123456789     | 2                       | W036898786808,W036898786809 | E2488V00, E2488V00 | RP_FROZEN_WITHIN_120_HOURS, RP_FROZEN_WITHIN_120_HOURS |

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




