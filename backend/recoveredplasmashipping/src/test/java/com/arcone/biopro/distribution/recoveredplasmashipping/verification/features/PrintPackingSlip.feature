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


        Rule: The system should trigger the printing of the carton automatically once a carton is closed.
        Rule: I should be able to print the carton packing slip with the carton details and products information.
        @ui @DIS-343
        Scenario Outline: Automatic printing when carton is closed
            Given I have a verified carton with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>" and Location Code as "<Location Code>".
            And  I navigate to the Manage Carton Products page for the carton sequence number <Carton Sequence Number>.
            When I choose to close the carton.
            Then I should be redirected to the Shipment Details page.
            And I should be able to see the Carton Packing Slip details.
            Examples:
                | Customer Code | Product Type              | Carton Tare Weight | Shipment Date | Transportation Reference Number | Location Code | configured_min_products | unit_number                     | product_code       | product_type                                           |
                | 408           | RP_FROZEN_WITHIN_24_HOURS | 1000               | <tomorrow>    | DIS-343                         | 123456789     | 2                       | W03689878680600,W03689878680700 | E2488V00, E2488V00 | RP_NONINJECTABLE_LIQUID_RT, RP_NONINJECTABLE_LIQUID_RT |


        Rule: I should be able to reprint carton packing slip if needed.
        Rule: The Testing Statement displayed on the carton packing slip must be configurable.
        @api @DIS-343
        Scenario: Manual printing of carton packing slip
            Given I have a verified carton created with the Customer Code as "408" , Product Type as "RP_FROZEN_WITHIN_120_HOURS", Carton Tare Weight as "1000", Shipment Date as "<tomorrow>", Transportation Reference Number as "DIS-339" and Location Code as "123456789".
            And The system configuration "TESTING_STATEMENT_TXT" is configured as "Testing statement template: {employeeName}" for the element "testingStatement".
            And The carton is closed.
            When I request to print the carton packing slip.
            Then The carton packing slip should contain:
                | Information Type                 | Information Value                                             |
                | Blood Center Name                | ARC-One Solutions                                             |
                | Blood Center License Number      | #2222                                                         |
                | Ship To: (Customer Name)         | Prothya                                                       |
                | Customer Address                 | 147 Wild Violet St. Atlanta, Georgia, 30041 USA               |
                | Ship From: (Blood Center Name)   | ARC-One Solutions                                             |
                | Location Address                 | 147 Wild Violet St. Atlanta, Georgia, 30041 USA               |
                | Product Type                     | RP FROZEN WITHIN 120 HOURS                                    |
                | Product Description              | CP2D PLS MI 120H                                              |
                | Product Code                     | E2488V00                                                      |
                | Carton Number                    | BPM2765                                                       |
                | Shipment Number                  | 123                                                           |
                | Carton Sequence                  | 2                                                             |
                | Products Packed and  Verified by | test-employee-id                                              |
                | Date/Time Packed Carton Closed   | 12/21/2024 16:05                                              |
                | Total Products                   | 2                                                             |
                | Transportation Reference Number  | 123                                                           |
                | Unit Numbers                     | W03689878680600,W03689878680700                               |
                | Collection Date                  | 12/12/2024, 12/12/2024                                        |
                | Product Volume                   | 100ml, 200ml                                                  |
                | Signature                        | <is_present>                                                  |
                | Testing statement                | Testing statement template: Jon Doe                           |

        Rule: The system must display the transportation reference number, if configured.
        Rule: The system must display the signature, if configured.
        Rule: The system must display the License number, if configured.
        @api @DIS-343
        Scenario Outline: Display of configurable elements on packing slip
            Given I have a verified carton with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>" and Location Code as "<Location Code>".
            And The carton is closed.
            And The system configuration "<system_configuration_key>" is configured as "<system_configuration_value>" for the element "<element>".
            When I request to print the carton packing slip.
            Then The element "<element>" "<should_should_not>" be present.
            Examples:
                | system_configuration_key  | system_configuration_value | element                       | should_should_not | Customer Code | Product Type              | Carton Tare Weight | Shipment Date | Transportation Reference Number | Location Code | configured_min_products | unit_number                     | product_code       | product_type                                           |
                | USE_SIGNATURE             | Y                          | signature                     | should            | 408           | RP_FROZEN_WITHIN_24_HOURS | 1000               | <tomorrow>    | DIS-343                         | 123456789     | 2                       | W03689878680600,W03689878680700 | E2488V00, E2488V00 | RP_NONINJECTABLE_LIQUID_RT, RP_NONINJECTABLE_LIQUID_RT |
                | USE_TRANSPORTATION_NUMBER | Y                          | transportationReferenceNumber | should            | 408           | RP_FROZEN_WITHIN_24_HOURS | 1000               | <tomorrow>    | DIS-343                         | 123456789     | 2                       | W03689878680600,W03689878680700 | E2488V00, E2488V00 | RP_NONINJECTABLE_LIQUID_RT, RP_NONINJECTABLE_LIQUID_RT |
                | USE_LICENSE_NUMBER        | Y                          | licenseNumber                 | should            | 408           | RP_FROZEN_WITHIN_24_HOURS | 1000               | <tomorrow>    | DIS-343                         | 123456789     | 2                       | W03689878680600,W03689878680700 | E2488V00, E2488V00 | RP_NONINJECTABLE_LIQUID_RT, RP_NONINJECTABLE_LIQUID_RT |
                | USE_TRANSPORTATION_NUMBER | N                          | transportationReferenceNumber | should not        | 408           | RP_FROZEN_WITHIN_24_HOURS | 1000               | <tomorrow>    | DIS-343                         | 123456789     | 2                       | W03689878680600,W03689878680700 | E2488V00, E2488V00 | RP_NONINJECTABLE_LIQUID_RT, RP_NONINJECTABLE_LIQUID_RT |


