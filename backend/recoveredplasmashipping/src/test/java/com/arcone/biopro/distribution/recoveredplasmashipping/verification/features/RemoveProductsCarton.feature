@AOA-89
Feature: Remove Products from Carton

    Background:
        Given I have removed from the database all the configurations for the location "123456789_DIS385".
        And I have removed from the database all shipments which code contains with "DIS38500".
        And I have removed from the database all shipments from location "123456789" with transportation ref number "DIS-385".
        And The location "123456789_DIS385" is configured with prefix "DIS_385", shipping code "DIS38500", carton prefix "BPM" and prefix configuration "Y".
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

        Rule: I should be able to remove one or more products from the selected carton.
        Rule: I should receive a success message once the products are successfully removed.
        @api @DIS-385
        Scenario Outline: Successfully remove products from an open carton
            Given I have a shipment created with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>" and Location Code as "<Location Code>".
            And The Minimum Number of Units in Carton is configured as "3" products for the customer code "<Customer Code>" and product type "<Product Type>".
            And I have a "OPEN" carton with the "PACKED" unit numbers as "<unit_number>" and product codes as "<product_code>" and product types "<product_type>".
            When I request to remove the products "<unit_number>" with product codes "<product_code>" from the carton sequence "1".
            Then I should receive a "SUCCESS" message response "Products successfully removed".
            When I request the last carton created.
            Then The product unit number "<unit_number>" and product code "<product_code>" "should not" be packed in the carton.
            Examples:
                | Customer Code | Product Type               | Carton Tare Weight | Shipment Date | Transportation Reference Number | Location Code    | unit_number    | product_code | product_type               |
                | 409           | RP_NONINJECTABLE_LIQUID_RT | 1000               | <tomorrow>    | DIS-385                         | 123456789_DIS385 | W036898385801   | E2488V00    | RP_NONINJECTABLE_LIQUID_RT |
                | 408           | RP_FROZEN_WITHIN_120_HOURS | 1000               | <tomorrow>    | DIS-385                         | 123456789_DIS385 | W036898385803   | E6022V00    | RP_FROZEN_WITHIN_120_HOURS |



        Rule: I should not be able to remove products when the carton is CLOSED.
        @api @DIS-385
        Scenario Outline: Attempt to remove products from a closed carton
            Given I have a shipment created with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>" and Location Code as "<Location Code>".
            And The Minimum Number of Units in Carton is configured as "3" products for the customer code "<Customer Code>" and product type "<Product Type>".
            And I have a "CLOSED" carton with the "VERIFIED" unit numbers as "<unit_number>" and product codes as "<product_code>" and product types "<product_type>".
            When I request to remove the products "<unit_number>" with product codes "<product_code>" from the carton sequence "1".
            Then I should receive a "SYSTEM" message response "Products cannot be removed. Contact Support.".
            When I request the last carton created.
            Then The product unit number "<unit_number>" and product code "<product_code>" "should" be verified in the carton.
            Examples:
                | Customer Code | Product Type               | Carton Tare Weight | Shipment Date | Transportation Reference Number | Location Code    | unit_number   | product_code  | product_type               |
                | 409           | RP_NONINJECTABLE_LIQUID_RT | 1000               | <tomorrow>    | DIS-385                         | 123456789_DIS385 | W036898385812 | E2488V00      | RP_NONINJECTABLE_LIQUID_RT |
                | 408           | RP_FROZEN_WITHIN_120_HOURS | 1000               | <tomorrow>    | DIS-385                         | 123456789_DIS385 | W036898385814 | E6022V00      | RP_FROZEN_WITHIN_120_HOURS |

        Rule: The system should reset the verification of the carton when products are removed.
        @api @DIS-385
        Scenario Outline: Verification status is reset when products are removed from a carton
            Given I have a shipment created with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>" and Location Code as "<Location Code>".
            And The Minimum Number of Units in Carton is configured as "3" products for the customer code "<Customer Code>" and product type "<Product Type>".
            And I have a "OPEN" carton with the "VERIFIED" unit numbers as "<unit_number1>,<unit_number2>" and product codes as "<product_code>,<product_code>" and product types "<product_type>,<product_type>".
            When I request to remove the products "<unit_number1>" with product codes "<product_code>" from the carton sequence "1".
            Then I should receive a "SUCCESS" message response "Products successfully removed".
            When I request the last carton created.
            Then The product unit number "<unit_number1>" and product code "<product_code>" "should not" be packed in the carton.
            And The product unit number "<unit_number2>" and product code "<product_code>" "should" be packed in the carton.
            And The product unit number "<unit_number2>" and product code "<product_code>" "should not" be verified in the carton.
            Examples:
                | Customer Code | Product Type               | Carton Tare Weight | Shipment Date | Transportation Reference Number | Location Code    | unit_number1  | unit_number2  | product_code | product_type               |
                | 409           | RP_NONINJECTABLE_LIQUID_RT | 1000               | <tomorrow>    | DIS-385                         | 123456789_DIS385 | W036898385816 | W036898385817 | E2488V00     | RP_NONINJECTABLE_LIQUID_RT |
                | 408           | RP_FROZEN_WITHIN_120_HOURS | 1000               | <tomorrow>    | DIS-385                         | 123456789_DIS385 | W036898385818 | W036898385819 | E6022V00     | RP_FROZEN_WITHIN_120_HOURS |

        Rule: I should be able to remove one or more products from the selected carton.
        Rule: I should receive a success message once the products are successfully removed.
        @ui @DIS-385
        Scenario Outline: Remove products from carton
            Given I have a shipment created with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>" and Location Code as "<Location Code>".
            And The Minimum Number of Units in Carton is configured as "3" products for the customer code "<Customer Code>" and product type "<Product Type>".
            And I have a "OPEN" carton with the "PACKED" unit numbers as "<unit_number1>,<unit_number2>" and product codes as "<product_code1>,<product_code2>" and product types "<product_type>".
            And I navigate to the Manage Carton Products page for the carton sequence number 1.
            When I select the product "<unit_number1>" with product code "<product_code1>".
            And I choose to remove products.
            Then I should see a "SUCCESS" message: "Product successfully removed".
            And  I should not see the product in the packed list with unit number "<unit_number1>" and product code "<product_code1>".
            And  I should see the product in the packed list with unit number "<unit_number2>" and product code "<product_code2>".
            Examples:
                | Customer Code | Product Type               | Carton Tare Weight | Shipment Date | Transportation Reference Number | Location Code    | unit_number1    | product_code1 | unit_number2    | product_code2 | product_type               |
                | 409           | RP_NONINJECTABLE_LIQUID_RT | 1000               | <tomorrow>    | DIS-385                         | 123456789_DIS385 | W036898385819   | E2488V00      | W036898385820   | E2488V00      | RP_NONINJECTABLE_LIQUID_RT |
                | 408           | RP_FROZEN_WITHIN_120_HOURS | 1000               | <tomorrow>    | DIS-385                         | 123456789_DIS385 | W036898385821   | E6022V00      | W036898385822   | E6022V00      | RP_FROZEN_WITHIN_120_HOURS |
