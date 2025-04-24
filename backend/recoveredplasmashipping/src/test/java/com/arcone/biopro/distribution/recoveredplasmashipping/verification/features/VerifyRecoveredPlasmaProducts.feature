@AOA-89
Feature: Verify Recovered Plasma Products


    Background:
        Given I have removed from the database all the configurations for the location "123456789_DIS339".
        And I have removed from the database all shipments which code contains with "DIS33900".
        And I have removed from the database all shipments from location "123456789" with transportation ref number "DIS-339".
        And The location "123456789_DIS339" is configured with prefix "DIS_339", shipping code "DIS33900", carton prefix "BPM" and prefix configuration "Y".
        And I have reset the shipment product criteria to have the following values:
            | recovered_plasma_shipment_criteria_id | type                    | value | message                                | message_type |
            | 1                                     | MINIMUM_VOLUME          | 165   | Product Volume does not match criteria | WARN         |
            | 2                                     | MINIMUM_VOLUME          | 200   | Product Volume does not match criteria | WARN         |
            | 1                                     | MAXIMUM_UNITS_BY_CARTON | 20    | Maximum number of products exceeded    | WARN         |
            | 2                                     | MAXIMUM_UNITS_BY_CARTON | 20    | Maximum number of products exceeded    | WARN         |
            | 3                                     | MAXIMUM_UNITS_BY_CARTON | 30    | Maximum number of products exceeded    | WARN         |
            | 4                                     | MAXIMUM_UNITS_BY_CARTON | 20    | Maximum number of products exceeded    | WARN         |
            | 5                                     | MAXIMUM_UNITS_BY_CARTON | 20    | Maximum number of products exceeded    | WARN         |
            | 6                                     | MAXIMUM_UNITS_BY_CARTON | 20    | Maximum number of products exceeded    | WARN         |

        Rule: I should be able to perform a second scan of the unit number and product code. (Verify Products in the Carton by second scan of unit number and product code)
        Rule: I should be able to confirm that a second verification of the products in the carton has been completed (Have a visual indicator for second verification of products)
        Rule: I should be able to view shipping information.
        Rule: I should be able to view carton information.

        @ui @DIS-341
        Scenario Outline: Successfully verify products in carton by second scan
            Given I have an empty carton created with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>" and Location Code as "<Location Code>".
            And The Minimum Number of Units in Carton is configured as "<configured_min_products>" products for the customer code "<Customer Code>" and product type "<Product Type>".
            And I have packed the unit numbers "<unit_number>", product codes "<product_code>" and product types "<product_type>".
            When I navigate to the shipment details page for the last shipment created.
            When I choose to verify carton sequence "<carton_sequence>".
            Then I should be redirected to the Verify Product page.
            When I scan an "acceptable" product with the unit number "<unit_number>", product code "<product_code>" and product type "<product_type>".
            Then I should see the product in the verified list with unit number "<unit_number>" and product code "<product_code>".
            Examples:
                | Customer Code | Product Type              | Carton Tare Weight | Shipment Date | Transportation Reference Number | Location Code |   configured_min_products | unit_number                      | product_code           | product_type                                           | carton_sequence |
                | 408           | RP_FROZEN_WITHIN_24_HOURS | 1000               | <tomorrow>    | DIS-339                         | 123456789     |  2                       | =W03689878680600,=W03689878680700 | =<E2488V00, =<E2488V00 | RP_NONINJECTABLE_LIQUID_RT, RP_NONINJECTABLE_LIQUID_RT | 1               |


        Rule: I should not be able to enter a unit number that doesn’t exist in the system and be notified.
        Rule: I should not be able to verify products that are part of another carton or shipment and be notified.
        Rule: I should not be able to verify shipped products in the carton and be notified.
        Rule: I should not be able to verify discarded products in the carton and be notified.
        Rule: I should not be able to verify quarantined products in the carton and be notified.
        Rule: I should not be able to verify expired products in the carton and be notified.
        #need confirmation whether products should be removed on each error type, or only when its unsuitable
        # first differentiate between unsuitable and unacceptable,
        #should we remove  unsuitable and unacceptable?
        # message that are used in add carton products cannot be used in verify products when there is an error, because system is reset and removing products.
        Rule: The system should automatically remove the products that are unacceptable from the carton.
        Rule: I should have an option to add more products in the carton when a product fails a validation.
        @api @DIS-341
        Scenario Outline: Attempt to verify unsuitable products in carton
            Given I have an empty carton created with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>" and Location Code as "<Location Code>".
            And The Minimum Number of Units in Carton is configured as "<configured_min_products>" products for the customer code "<Customer Code>" and product type "<Product Type>".
            And I have the unit numbers "<unit_number>", product codes "<product_code>" and product types "<product_type>" packed which become unsuitable.
            When I verify an "unsuitable" product with the unit number "<unit_number>", product code "<product_code>" and product type "<product_type>".
            Then I should receive a "<error_type>" message response "<error_message>".
            And The product unit number "<unit_number>" and product code "<product_code>" "should not" be added to the verified list.
            And The product unit number "<unit_number>" and product code "<product_code>" "should not" be packed in the carton.
            And I should be able to add more products in the carton.
            Examples:
                | Customer Code | Product Type               | Carton Tare Weight | Shipment Date | Transportation Reference Number | Location Code    | unit_number   | product_code | product_type               | error_type | error_message                                                                 |
                | 408           | RP_FROZEN_WITHIN_120_HOURS | 1000               | <tomorrow>    | DIS-339                         | 123456789_DIS339 | W036898786905 | E6022V00     | RP_FROZEN_WITHIN_120_HOURS | WARN       | This product is not in the inventory and cannot be shipped                    |
                | 408           | RP_FROZEN_WITHIN_120_HOURS | 1000               | <tomorrow>    | DIS-339                         | 123456789_DIS339 | W036898786757 | E6022V00     | RP_FROZEN_WITHIN_120_HOURS | INFO       | This product is discarded and cannot be shipped                               |
                | 408           | RP_FROZEN_WITHIN_120_HOURS | 1000               | <tomorrow>    | DIS-339                         | 123456789_DIS339 | W036898786758 | E6022V00     | RP_FROZEN_WITHIN_120_HOURS | INFO       | This product is quarantined and cannot be shipped                             |
                | 408           | RP_FROZEN_WITHIN_120_HOURS | 1000               | <tomorrow>    | DIS-339                         | 123456789_DIS339 | W036898786756 | E6022V00     | RP_FROZEN_WITHIN_120_HOURS | INFO       | This product is expired and has been discarded. Place in biohazard container. |
                | 408           | RP_FROZEN_WITHIN_120_HOURS | 1000               | <tomorrow>    | DIS-339                         | 123456789_DIS339 | W036898786804 | E5880V00     | RP_FROZEN_WITHIN_72_HOURS  | WARN       | Product Type does not match                                                   |
                | 408           | RP_FROZEN_WITHIN_120_HOURS | 1000               | <tomorrow>    | DIS-339                         | 123456789_DIS339 | W036898786700 | E6022V00     | RP_FROZEN_WITHIN_120_HOURS | WARN       | This product was previously shipped.                                          |


        Rule: The system should automatically reset the verification process when the products are unacceptable to be filled in the carton.
         #confirm with Archana to add in AC
        Rule: I should not be able to verify products which are already verified.
        @api @DIS-341
            Scenario: Attempt to verify products which are already verified.
            Given I have an empty carton created with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>" and Location Code as "<Location Code>".
            And The Minimum Number of Units in Carton is configured as "<configured_min_products>" products for the customer code "<Customer Code>" and product type "<Product Type>".
            And I have packed the unit numbers "W036898786800", product codes "E6022V00" and product types "RP_FROZEN_WITHIN_120_HOURS".
            When I verify an "acceptable" product with the unit number "W036898786800", product code "E6022V00" and product type "RP_FROZEN_WITHIN_120_HOURS".
            Then The product unit number "W036898786800" and product code "E6022V00" "should" be varified in the carton.
            When I verify an "acceptable" product with the unit number "W036898786800", product code "E6022V00" and product type "RP_FROZEN_WITHIN_120_HOURS".
            Then I should receive a "WARN" message response "This product has already been verified. Please re-scan all the products in the carton.".
            And The product unit number "W036898786800" and product code "E6022V00" "should not" be varified in the carton.
            And The carton verification should reset.


        # confirm with Archana to add this AC and  wanring message mentioning word please
        Rule: I should not be able to verify products that is part of another carton or shipment and be notified.
        @api @DIS-341
            Scenario: Attempt to verify products which are not packed into the carton.
                Given I have an empty carton created with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>" and Location Code as "<Location Code>".
                And The Minimum Number of Units in Carton is configured as "<configured_min_products>" products for the customer code "<Customer Code>" and product type "<Product Type>".
                And I have packed the unit number "W036898786800", product codes "E6022V00" and product types "RP_FROZEN_WITHIN_120_HOURS".
                When I verify an "acceptable" product with the unit number "W036898786801", product code "E6022V00" and product type "RP_FROZEN_WITHIN_120_HOURS".
                Then I should receive a "WARN" message response "The verification does not match all products in this carton. Please re-scan all the products.".
                And The product unit number "W036898786800" and product code "E6022V00" "should not" be varified in the carton.
                And The carton verification should reset.



        Rule: I should not be able to verify products in the carton with a volume that does not match carton volume (plasma fractionator) criteria and be notified.
        @api @DIS-341
            Scenario Outline: Verify products with incorrect volume
            Given I have an empty carton created with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>" and Location Code as "<Location Code>".
            And I have packed the unit numbers "<unit_number>", product codes "<product_code>" and product types "<product_type>".
            And The Minimum acceptable Volume of Units in Carton is configured as "<configured_volume>" milliliters for the customer code "<Customer Code>" and product type "<Product Type>".
            When I verify a product with the unit number "<unit_number>", product code "<product_code>" and volume "<product_volume>".
            Then I should receive a "WARN" message response "Product Volume does not match criteria".
            And The product unit number "<unit_number>" and product code "<product_code>" "should not" be verified in the carton.
            #need confirmation whether products should be removed on each error type, or only when its unsuitable
            And The carton verification should reset.
            Examples:
                | Customer Code | Product Type              | Carton Tare Weight | Shipment Date | Transportation Reference Number | Location Code | configured_volume | unit_number   | product_code | product_volume |
                | 408           | RP_FROZEN_WITHIN_24_HOURS | 1000               | <tomorrow>    | DIS-339                         | 123456789     | 300               | W036898786801 | E2534V00     | 259            |


