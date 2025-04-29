@AOA-89
Feature: Add Products to Carton

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




    Rule: I should be able to only scan the unit number and product code.
    Rule: I should be able to view the products added to the carton with information like unit number and product code.
    Rule: Verify acceptability of the products as they are added to the carton.
    Rule: I should be able to view shipping information.
    @ui @DIS-339
    Scenario Outline: Successfully add product to carton by scanning
        Given I have an empty carton created with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>" and Location Code as "<Location Code>".
        And I navigate to the Add Carton Products page for the carton sequence number <Carton Sequence Number>.
        When I add an "acceptable" product with the unit number "<unit_number>", product code "<product_code>" and product type "<product_type>".
        Then I should see the product in the packed list with unit number "<unit_number>" and product code "<product_code>".
        When I navigate to the shipment details page for the last shipment created.
        Then I should see a list of all cartons.
        And I should see the unit "<unit_number>" added to the carton sequence "<Carton Sequence Number>".
        Examples:
            | Customer Code | Product Type              | Carton Tare Weight | Shipment Date | Transportation Reference Number | Location Code | Carton Sequence Number | unit_number      | product_code | product_type              | total_products |
            | 408           | RP_FROZEN_WITHIN_24_HOURS | 1000               | <tomorrow>    | DIS-339                         | 123456789     | 1                      | =W03689878680100 | =<E2534V00   | RP_FROZEN_WITHIN_24_HOURS |1               |

        @ui @DIS-339
        Scenario Outline: Add product with correct volume
            Given I have 2 empty cartons created with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>" and Location Code as "<Location Code>".
            And I pack a product with the unit number "<unit_number 1>" and product code "<product_code>" into the carton sequence 1.
            And I pack a product with the unit number "<unit_number 2>" and product code "<product_code>" into the carton sequence 2.
            When I navigate to the shipment details page for the last shipment created.
            Then I should see a list of all cartons.
            And I should see the unit "<unit_number 1>" added to the carton sequence "1".
            And I should see the unit "<unit_number 2>" added to the carton sequence "2".
            Examples:
                | Customer Code | Product Type                  | Carton Tare Weight | Shipment Date | Transportation Reference Number | Location Code | unit_number 1 | unit_number 2 | product_code |
                | 410           | RP_NONINJECTABLE_REFRIGERATED | 1000               | <tomorrow>    | DIS-339                         | 123456789     | W036898786805 | W036898786811 | E6170V00     |


    Rule: Verify acceptability of the products as they are added to the carton.
    Rule: I should not be able to enter a unit number that doesn’t exist in the system and be notified.
    Rule: I should not be able to add discarded products in the carton and be notified.
    Rule: I should not be able to add quarantined products in the carton and be notified.
    Rule: I should not be able to add expired products in the carton and be notified.
    Rule: I should not be able to add products in the carton that do not match the product type criteria and be notified.
    Rule: I should not be able to add shipped products in the carton and be notified.
    @api @DIS-339
    Scenario Outline: Attempt to add unsuitable products to carton
        Given I have an empty carton created with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>" and Location Code as "<Location Code>".
        When I fill an "unsuitable" product with the unit number "<unit_number>", product code "<product_code>" and product type "<product_type>".
        Then I should receive a "<error_type>" message response "<error_message>".
        And The product unit number "<unit_number>" and product code "<product_code>" "should not" be packed in the carton.
        Examples:
            | Customer Code | Product Type               | Carton Tare Weight   | Shipment Date | Transportation Reference Number | Location Code  | unit_number     | product_code | product_type               | error_type | error_message                                                                 |
            | 408           | RP_FROZEN_WITHIN_120_HOURS | 1000                 | <tomorrow>    | DIS-339                         | 123456789_DIS339 | W036898786905 | E6022V00     | RP_FROZEN_WITHIN_120_HOURS | WARN       | This product is not in the inventory and cannot be shipped                    |
            | 408           | RP_FROZEN_WITHIN_120_HOURS | 1000                 | <tomorrow>    | DIS-339                         | 123456789_DIS339 | W036898786757 | E6022V00     | RP_FROZEN_WITHIN_120_HOURS | INFO       | This product is discarded and cannot be shipped                               |
            | 408           | RP_FROZEN_WITHIN_120_HOURS | 1000                 | <tomorrow>    | DIS-339                         | 123456789_DIS339 | W036898786758 | E6022V00     | RP_FROZEN_WITHIN_120_HOURS | INFO       | This product is quarantined and cannot be shipped                             |
            | 408           | RP_FROZEN_WITHIN_120_HOURS | 1000                 | <tomorrow>    | DIS-339                         | 123456789_DIS339 | W036898786756 | E6022V00     | RP_FROZEN_WITHIN_120_HOURS | INFO       | This product is expired and has been discarded. Place in biohazard container. |
            | 408           | RP_FROZEN_WITHIN_120_HOURS | 1000                 | <tomorrow>    | DIS-339                         | 123456789_DIS339 | W036898786804 | E5880V00     | RP_FROZEN_WITHIN_72_HOURS  | WARN       | Product Type does not match                                                   |
            | 408           | RP_FROZEN_WITHIN_120_HOURS | 1000                 | <tomorrow>    | DIS-339                         | 123456789_DIS339 | W036898786700 | E6022V00     | RP_FROZEN_WITHIN_120_HOURS | WARN       | This product was previously shipped.                                          |
            | 408           | RP_FROZEN_WITHIN_120_HOURS | 1000                 | <tomorrow>    | DIS-339                         | 123456789_DIS339 | W036898786700 | E6022V00     | RP_FROZEN_WITHIN_120_HOURS | WARN       | This product was previously shipped.                                          |


    Rule: I should not be able to add products that is part of another carton or shipment and be notified.
    @api @DIS-339
    Scenario: Attempt to add packed products to carton
        Given I have an empty carton created with the Customer Code as "408" , Product Type as "RP_FROZEN_WITHIN_120_HOURS", Carton Tare Weight as "1000", Shipment Date as "<tomorrow>", Transportation Reference Number as "DIS-339" and Location Code as "123456789".
        When I fill an "acceptable" product with the unit number "W036898786800", product code "E6022V00" and product type "RP_FROZEN_WITHIN_120_HOURS".
        Then The product unit number "W036898786800" and product code "E6022V00" "should" be packed in the carton.
        When I fill an "acceptable" product with the unit number "W036898786800", product code "E6022V00" and product type "RP_FROZEN_WITHIN_120_HOURS".
        Then I should receive a "WARN" message response "Product already added in a carton".
        And The product unit number "W036898786800" and product code "E2534V00" "should not" be packed in the carton.


    Rule: I should not be able to add products in the carton with a volume that does not match carton volume (plasma fractionator) criteria and be notified.
    @api @DIS-339
    Scenario Outline: Add product with incorrect volume
        Given I have an empty carton created with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>" and Location Code as "<Location Code>".
        And The Minimum acceptable Volume of Units in Carton is configured as "<configured_volume>" milliliters for the customer code "<Customer Code>" and product type "<Product Type>".
        When I pack a product with the unit number "<unit_number>", product code "<product_code>" and volume "<product_volume>".
        Then I should receive a "WARN" message response "Product Volume does not match criteria".
        And The product unit number "<unit_number>" and product code "<product_code>" "should not" be packed in the carton.
        Examples:
            | Customer Code | Product Type              | Carton Tare Weight | Shipment Date | Transportation Reference Number | Location Code | configured_volume | unit_number   | product_code | product_volume |
            | 408           | RP_FROZEN_WITHIN_24_HOURS | 1000               | <tomorrow>    | DIS-339                         | 123456789     | 300               | W036898786801 | E2534V00     | 259            |


    Rule: I should not be able to add products in the carton once the configured number of products in the carton criteria is met and be notified.
        Rule: Add products to a carton up to configurable number by plasma customer.
        @api @DIS-339
        Scenario Outline: Attempt to exceed maximum products in carton
            Given I have an empty carton created with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>" and Location Code as "<Location Code>".
            And The Maximum Number of Units in Carton is configured as "<configured_max_products>" products for the customer code "<Customer Code>" and product type "<Product Type>".
            And I have packed the following products:
                | unit_number   | product_code | product_type               |
                | W036898786806 | E2488V00     | RP_NONINJECTABLE_LIQUID_RT |
                | W036898786807 | E2488V00     | RP_NONINJECTABLE_LIQUID_RT |
                | W036898786808 | E2488V00     | RP_NONINJECTABLE_LIQUID_RT |
                | W036898786809 | E2488V00     | RP_NONINJECTABLE_LIQUID_RT |
                | W036898786810 | E2488V00     | RP_NONINJECTABLE_LIQUID_RT |
            When I pack a product with the unit number "<unit_number>", product code "<product_code>".
            Then The product unit number "<unit_number>" and product code "<product_code>" "should not" be packed in the carton.
            And  I should receive a "WARN" message response "Maximum number of products exceeded".
            Examples:
                | Customer Code | Product Type               | Carton Tare Weight | Shipment Date | Transportation Reference Number | Location Code | configured_max_products | unit_number   | product_code |
                | 409           | RP_NONINJECTABLE_LIQUID_RT | 1000               | <tomorrow>    | DIS-339                         | 123456789     | 5                       | W036898786803 | E2488V00     |


        Rule: I should be able to add products in the carton for customers that do not requires minimum volume criteria.
        @api @DIS-339
        Scenario Outline: Add product with correct volume
            Given I have an empty carton created with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>" and Location Code as "<Location Code>".
            When I pack a product with the unit number "<unit_number>", product code "<product_code>" and volume "<product_volume>".
            Then The product unit number "<unit_number>" and product code "<product_code>" "should" be packed in the carton.
            Examples:
                | Customer Code | Product Type                  | Carton Tare Weight | Shipment Date | Transportation Reference Number | Location Code | unit_number   | product_code | product_volume |
                | 410           | RP_NONINJECTABLE_REFRIGERATED | 1000               | <tomorrow>    | DIS-339                         | 123456789     | W036898786805 | E6170V00     | 259            |

