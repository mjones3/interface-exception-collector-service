@AOA-89
Feature: Verify Recovered Plasma Products


    Background:
        Given I have removed from the database all the configurations for the location "123456789_DIS341".
        And I have removed from the database all shipments which code contains with "DIS34100".
        And I have removed from the database all shipments from location "123456789" with transportation ref number "DIS-341".
        And I have removed from the database all shipments from location "123456789" with transportation ref number "DIS-339".
        And The location "123456789_DIS341" is configured with prefix "DIS_339", shipping code "DIS34100", carton prefix "BPM" and prefix configuration "Y".
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


    Rule: I should be able to perform a second scan of the unit number and product code. (Verify Products in the Carton by second scan of unit number and product code)
        Rule: I should be able to confirm that a second verification of the products in the carton has been completed (Have a visual indicator for second verification of products)
    Rule: I should be able to view shipping information.
        Rule: I should be able to view carton information.
    Rule: I should have an option to add more products in the carton when a product fails a validation.
        @ui @DIS-341
        Scenario Outline: Successfully verify products in carton by second scan
            Given I have an empty carton created with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>" and Location Code as "<Location Code>".
            And The Minimum Number of Units in Carton is configured as "<configured_min_products>" products for the customer code "<Customer Code>" and product type "<Product Type>".
            And I navigate to the Manage Carton Products page for the carton sequence number <Carton Sequence Number>.
            When I add an "acceptable" product with the unit number "<unit_number>", product code "<product_code>" and product type "<product_type>".
            Then The verify products option should be "enabled"
            And I choose the Next option to start verify products process.
            When I scan to verify an "acceptable" product with the unit number "<unit_number>", product code "<product_code>" and product type "<product_type>".
            Then I should see the product in the verified list with unit number "<unit_number>" and product code "<product_code>".
            And The close carton option should be "enabled".
            Examples:
                | Customer Code | Product Type               | Carton Tare Weight | Shipment Date | Transportation Reference Number | Location Code | configured_min_products | unit_number                       | product_code           | product_type                                           | Carton Sequence Number |
                | 409           | RP_NONINJECTABLE_LIQUID_RT | 1000               | <tomorrow>    | DIS-339                         | 123456789     | 2                       | =W03689878680800,=W03689878680900 | =<E2488V00, =<E2488V00 | RP_NONINJECTABLE_LIQUID_RT, RP_NONINJECTABLE_LIQUID_RT | 1                      |


    Rule: I should not be able to enter a unit number that doesn’t exist in the system and be notified.
        Rule: I should not be able to verify products that are part of another carton or shipment and be notified.
    Rule: I should not be able to verify shipped products in the carton and be notified.
        Rule: I should not be able to verify discarded products in the carton and be notified.
    Rule: I should not be able to verify quarantined products in the carton and be notified.
        Rule: I should not be able to verify expired products in the carton and be notified.
    Rule: The system should automatically remove the products that are unacceptable from the carton.
        Rule: I should be notified when I scan a unit that is not part of the shipment.
        @api @DIS-341
        Scenario Outline: Attempt to verify unsuitable products in carton
            Given I have an empty carton created with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>" and Location Code as "<Location Code>".
            And The Minimum Number of Units in Carton is configured as "<configured_min_products>" products for the customer code "<Customer Code>" and product type "<Product Type>".
            And I have the unit numbers "<pack_unit_number>", product codes "<product_code>" and product types "<product_type>" packed which become unsuitable.
            When I verify an "unsuitable" product with the unit number "<verify_unit_number>", product code "<product_code>" and product type "<product_type>".
            Then I should receive a "<error_type>" message response "<error_message>".
            And The product unit number "<verify_unit_number>" and product code "<product_code>" "should not" be packed in the carton.
            And The product unit number "<pack_unit_number>" and product code "<product_code>" "should not" be verified in the carton.
            And I should receive a "CAUTION" static message response "Products removed due to failure, repeat process".
            Examples:
                | Customer Code | Product Type               | Carton Tare Weight | Shipment Date | Transportation Reference Number | Location Code    | pack_unit_number | verify_unit_number | product_code | product_type               | error_type | error_message                                                                                 | configured_min_products |
                | 408           | RP_FROZEN_WITHIN_120_HOURS | 1000               | <tomorrow>    | DIS-341                         | 123456789_DIS341 | W036898786905    | W036898786905      | E6022V00     | RP_FROZEN_WITHIN_120_HOURS | WARN       | This product is not in the inventory and cannot be shipped                                    | 1                       |
                | 408           | RP_FROZEN_WITHIN_120_HOURS | 1000               | <tomorrow>    | DIS-341                         | 123456789_DIS341 | W036898786757    | W036898786757      | E6022V00     | RP_FROZEN_WITHIN_120_HOURS | INFO       | This product is discarded and cannot be shipped                                               | 1                       |
                | 408           | RP_FROZEN_WITHIN_120_HOURS | 1000               | <tomorrow>    | DIS-341                         | 123456789_DIS341 | W036898786758    | W036898786758      | E6022V00     | RP_FROZEN_WITHIN_120_HOURS | INFO       | This product is quarantined and cannot be shipped                                             | 1                       |
                | 408           | RP_FROZEN_WITHIN_120_HOURS | 1000               | <tomorrow>    | DIS-341                         | 123456789_DIS341 | W036898786756    | W036898786756      | E6022V00     | RP_FROZEN_WITHIN_120_HOURS | INFO       | This product is expired and has been discarded. Place in biohazard container.                 | 1                       |
                | 408           | RP_FROZEN_WITHIN_120_HOURS | 1000               | <tomorrow>    | DIS-341                         | 123456789_DIS341 | W036898786804    | W036898786804      | E5880V00     | RP_FROZEN_WITHIN_72_HOURS  | WARN       | Product Type does not match                                                                   | 1                       |
                | 408           | RP_FROZEN_WITHIN_120_HOURS | 1000               | <tomorrow>    | DIS-341                         | 123456789_DIS341 | W036898786701    | W036898786700      | E6022V00     | RP_FROZEN_WITHIN_120_HOURS | WARN       | The verification does not match all products in this carton. Please re-scan all the products. | 1                       |


    Rule: The system should automatically remove all the products from the carton when there is an unacceptable product.
        Rule: I should not be able to verify products which are already verified.
        @api @DIS-341
        Scenario: Attempt to verify products which are already verified.
            Given I have an empty carton created with the Customer Code as "408" , Product Type as "RP_FROZEN_WITHIN_120_HOURS", Carton Tare Weight as "1000", Shipment Date as "<tomorrow>", Transportation Reference Number as "DIS-341" and Location Code as "123456789".
            And The Minimum Number of Units in Carton is configured as "1" products for the customer code "408" and product type "RP_FROZEN_WITHIN_120_HOURS".
            And I pack an "acceptable" product with the unit number "W036898786800", product code "E6022V00" and product type "RP_FROZEN_WITHIN_120_HOURS".
            When I verify an "acceptable" product with the unit number "W036898786800", product code "E6022V00" and product type "RP_FROZEN_WITHIN_120_HOURS".
            Then The product unit number "W036898786800" and product code "E6022V00" "should" be verified in the carton.
            When I verify an "acceptable" product with the unit number "W036898786800", product code "E6022V00" and product type "RP_FROZEN_WITHIN_120_HOURS".
            Then I should receive a "WARN" message response "This product has already been verified. Please re-scan all the products in the carton.".
            And The product unit number "<verify_unit_number>" and product code "<product_code>" "should not" be packed in the carton.
            And The product unit number "<pack_unit_number>" and product code "<product_code>" "should not" be verified in the carton.
            And I should receive a "CAUTION" static message response "Products removed due to failure, repeat process".


    Rule: I should not be able to verify products that is part of another carton or shipment and be notified.
        @api @DIS-341
        Scenario: Attempt to verify products which are not packed into the carton.
            Given I have 2 empty cartons created with the Customer Code as "408" , Product Type as "RP_FROZEN_WITHIN_120_HOURS", Carton Tare Weight as "1000", Shipment Date as "<tomorrow>", Transportation Reference Number as "DIS-341" and Location Code as "123456789".
            And The Minimum Number of Units in Carton is configured as "1" products for the customer code "408" and product type "RP_FROZEN_WITHIN_120_HOURS".
            And I pack a product with the unit number "W036898786800" and product code "E6022V00" into the carton sequence 1.
            And I pack a product with the unit number "W036898786813" and product code "E6022V00" into the carton sequence 2.
            When I verify an "acceptable" product with the unit number "W036898786800", product code "E6022V00" and product type "RP_FROZEN_WITHIN_120_HOURS" into the carton sequence 2.
            Then I should receive a "WARN" message response "The verification does not match all products in this carton. Please re-scan all the products.".
            And The product unit number "W036898786800" and product code "E6022V00" "should not" be verified in the carton.
            And The product unit number "W036898786801" and product code "E6022V00" "should not" be packed in the carton.
            And I should receive a "CAUTION" static message response "Products removed due to failure, repeat process".



    Rule: I should not be able to verify products in the carton with a volume that does not match carton volume (plasma fractionator) criteria and be notified.
        @api @DIS-341
        Scenario Outline: Verify products with incorrect volume
            Given I have an empty carton created with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>" and Location Code as "<Location Code>".
            And The Minimum Number of Units in Carton is configured as "1" products for the customer code "<Customer Code>" and product type "<Product Type>".
            And I pack a product with the unit number "<unit_number>", product code "<product_code>" and volume "<product_volume>".
            And The Minimum acceptable Volume of Units in Carton is configured as "<configured_volume>" milliliters for the customer code "<Customer Code>" and product type "<Product Type>".
            When I verify a product with the unit number "<unit_number>", product code "<product_code>" and volume "<product_volume>".
            Then I should receive a "WARN" message response "Product Volume does not match criteria".
            And The product unit number "<unit_number>" and product code "<product_code>" "should not" be verified in the carton.
            And The product unit number "<unit_number>" and product code "<product_code>" "should not" be packed in the carton.
            And I should receive a "CAUTION" static message response "Products removed due to failure, repeat process".
            Examples:
                | Customer Code | Product Type              | Carton Tare Weight | Shipment Date | Transportation Reference Number | Location Code | configured_volume | unit_number   | product_code | product_volume |
                | 408           | RP_FROZEN_WITHIN_24_HOURS | 1000               | <tomorrow>    | DIS-341                         | 123456789     | 300               | W036898786801 | E2534V00     | 259            |


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
