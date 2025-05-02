@AOA-89
Feature: Close Carton

    Background:
        Given I have removed from the database all shipments from location "123456789" with transportation ref number "DIS-342".


        # UI close carton happy path is covered as part of verifyCarton.feature first scenario

        Rule: I should only be able to close a carton if the minimum numbers of products has been reached.
        Rule: The system should automatically update the status of the carton to closed once the carton is successfully closed.
        @api @DIS-342
        Scenario Outline: Successfully closing a carton with minimum required products
            Given I have an empty carton created with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>" and Location Code as "<Location Code>".
            And The Minimum Number of Units in Carton is configured as "<configured_min_products>" products for the customer code "<Customer Code>" and product type "<Product Type>".
            And I have verified product with the unit number "<unit_number>", product code "<product_code>" and product type "<product_type>".
            When I request to close the carton.
            Then I should receive a "SUCCESS" message response "Carton closed successfully".
            And The carton status should be "CLOSED".
            And The status of all "verified" products in the carton should be updated to "VERIFIED".
            Examples:
                | Customer Code | Product Type               | Carton Tare Weight | Shipment Date | Transportation Reference Number | Location Code | configured_min_products | unit_number                 | product_code       | product_type                                           |
                | 409           | RP_NONINJECTABLE_LIQUID_RT | 1000               | <tomorrow>    | DIS-342                         | 123456789     | 2                       | W036898786808,W036898786809 | E2488V00, E2488V00 | RP_NONINJECTABLE_LIQUID_RT, RP_NONINJECTABLE_LIQUID_RT |


        Rule: I should not be able to close a carton if the verify process is not completed.
        @api @DIS-342
        Scenario Outline: Attempting to close an unverified carton
            Given I have an empty carton created with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>" and Location Code as "<Location Code>".
            And The Minimum Number of Units in Carton is configured as "<configured_min_products>" products for the customer code "<Customer Code>" and product type "<Product Type>".
            And I pack a "valid" product with the unit number "<unit_number>", product code "<product_code>" and product type "<product_type>".
            When I request to close the carton.
            Then I should receive a "SYSTEM" message response "Close Carton error. Contact Support.".
            And The carton status should be "OPEN".
            And The status of all "packed" products in the carton should be updated to "PACKED".
            Examples:
                | Customer Code | Product Type              | Carton Tare Weight | Shipment Date | Transportation Reference Number | Location Code | configured_min_products | unit_number   | product_code | product_type              |
                | 408           | RP_FROZEN_WITHIN_24_HOURS | 1000               | <tomorrow>    | DIS-342                         | 123456789     | 1                       | W036898786801 | E2534V00     | RP_FROZEN_WITHIN_24_HOURS |


        @api @DIS-342
        Scenario Outline: Verifying carton cannot be modified after closing
            Given I have an empty carton created with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>" and Location Code as "<Location Code>".
            And The Minimum Number of Units in Carton is configured as "<configured_min_products>" products for the customer code "<Customer Code>" and product type "<Product Type>".
            And I pack a "valid" product with the unit number "<unit_number>", product code "<product_code>" and product type "<product_type>".
            And I have verified product with the unit number "<unit_number>", product code "<product_code>" and product type "<product_type>".
            When I request to close the carton.
            Then I should receive a "SUCCESS" message response "Carton closed successfully".
            And The carton status should be "CLOSED".
            And The status of all "verified" products in the carton should be updated to "VERIFIED".
            When I request to close the carton.
            Then I should receive a "SYSTEM" message response "Close Carton error. Contact Support.".
            And The carton status should be "CLOSED".
            And The status of all "verified" products in the carton should be updated to "VERIFIED".
            Examples:
                | Customer Code | Product Type              | Carton Tare Weight | Shipment Date | Transportation Reference Number | Location Code | configured_min_products | unit_number   | product_code | product_type              |
                | 408           | RP_FROZEN_WITHIN_24_HOURS | 1000               | <tomorrow>    | DIS-342                         | 123456789     | 1                       | W036898786801 | E2534V00     | RP_FROZEN_WITHIN_24_HOURS |




