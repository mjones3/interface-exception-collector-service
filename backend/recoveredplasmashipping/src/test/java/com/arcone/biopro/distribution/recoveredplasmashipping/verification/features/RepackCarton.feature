@AOA-89
Feature: Repack Carton

    Background:
        Given I have removed from the database all the configurations for the location "123456789_DIS355".
        And I have removed from the database all shipments which code contains with "DIS35500".
        And I have removed from the database all shipments from location "123456789" with transportation ref number "DIS-355".
        And The location "123456789_DIS355" is configured with prefix "DIS_355", shipping code "DIS35500", carton prefix "BPM" and prefix configuration "Y".
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


        Rule: I should be able to identify when a carton needs to be repacked.
        Rule: I should be requested to confirm the repack of a carton.
        Rule: I should be required to provide repack reason comments.
        Rule: The system should remove all products once I confirm the repack.
        Rule: The system should not remove products when I cancel the repack.
        Rule: I should be able to add products in the same carton.
        @ui @DIS-355
        Scenario: Successfully repack a carton.
            Given I have a shipment created with the Customer Code as "409" , Product Type as "RP_NONINJECTABLE_LIQUID_RT", Carton Tare Weight as "1000", Shipment Date as "<tomorrow>", Transportation Reference Number as "DIS-355" and Location Code as "123456789_DIS355".
            And The Minimum Number of Units in Carton is configured as "2" products for the customer code "409" and product type "RP_NONINJECTABLE_LIQUID_RT".
            And I have a closed carton with the unit numbers as "W036898355905,W036898355757" and product codes as "E6022V00,E6022V00" and product types "RP_NONINJECTABLE_LIQUID_RT,RP_NONINJECTABLE_LIQUID_RT" which were flagged as repack.
            And I navigate to the shipment details page for the last shipment created.
#            Then I should see the following shipment information:
#                | Field                      | Value                      |
#                | Shipment Number Prefix     | DIS_355DIS355              |
#                | Customer Code              | 409                        |
#                | Customer Name              | SOUTHERN BIOLOGICS         |
#                | Product Type               | RP NONINJECTABLE LIQUID RT |
#                | Shipment Status            | IN PROGRESS                |
#                | Shipment Date              | <tomorrow>                 |
#                | Transportation Ref. Number | DIS-355                    |
#                | Total Cartons              | 1                          |
#                | Carton Status              | BPMMH1,1,REPACK            |

            And The repack option should be available for the carton number prefix "BPMMH1" and sequence number "1" and status "REPACK".
            When I choose to repack the carton number prefix "BPMMH1" and sequence number "1" and status "REPACK".
            Then I should see a "Repack carton" message: "Are you sure you want to repack this carton? All products will be removed.".
            When I choose cancel the repack carton.
            Then The status of the carton number prefix "BPMMH1" and sequence number "1" should be "REPACK".
            When I choose to repack the carton number prefix "BPMMH1" and sequence number "1" and status "REPACK".
            Then I should see a "Repack carton" message: "Are you sure you want to repack this carton? All products will be removed.".
            And I enter reason comments "comments".
            When I confirm to repack the carton.
            Then I should see a "SUCCESS" message: "Products successfully removed".
            And I should be redirected to the Manage Carton Products page.
            And The product unit number "<unit_number>" and product code "<product_code>" "should not" be packed in the carton.


        Rule: I should be able to identify when a carton needs to be repacked.
        Rule: I should be requested to confirm the repack of a carton.
        Rule: I should be required to provide repack reason comments.
        Rule: The system should remove all products once I confirm the repack.
        Rule: I should be able to add products in the same carton.
        @api @DIS-355
        Scenario Outline: Successfully repack a carton with API
            Given I have a shipment created with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>" and Location Code as "<Location Code>".
            And The Minimum Number of Units in Carton is configured as "<configured_min_products>" products for the customer code "<Customer Code>" and product type "<Product Type>".
            And I have a closed carton with the unit numbers as "<unit_number>" and product codes as "<product_code>" and product types "<product_type>" which were flagged as repack.
#            When I request to repack the carton with reason "Products damaged during handling".
#            Then I should receive a "SUCCESS" message response "Products successfully removed".
#            Then The products unit number "<unit_number>" and product code "<product_code>" "should not" be packed in the carton.
#            And The carton status should be "OPEN".
            Examples:
                | Customer Code | Product Type               | Carton Tare Weight | Shipment Date | Transportation Reference Number | Location Code    | configured_min_products | unit_number                 | product_code      | product_type                                          | Shipment Date |
                | 409           | RP_NONINJECTABLE_LIQUID_RT | 1000               | <tomorrow>    | DIS-355                         | 123456789_DIS355 | 2                       | W036898355905,W036898355757 | E6022V00,E6022V00 | RP_NONINJECTABLE_LIQUID_RT,RP_NONINJECTABLE_LIQUID_RT | <tomorrow>    |



        Rule: I should not be able to repack a closed carton with acceptable products.
        @api @DIS-355
        Scenario: Attempt to repack a closed carton
            Given I have a shipment created with the Customer Code as "409" , Product Type as "RP_NONINJECTABLE_LIQUID_RT", Carton Tare Weight as "1000", Shipment Date as "<tomorrow>", Transportation Reference Number as "DIS-355" and Location Code as "123456789_DIS355".
            And The Minimum Number of Units in Carton is configured as "2" products for the customer code "409" and product type "RP_NONINJECTABLE_LIQUID_RT".
            And I have a closed carton with the unit numbers as "W036898355905,W036898355757" and product codes as "E6022V00,E6022V00" and product types "RP_NONINJECTABLE_LIQUID_RT,RP_NONINJECTABLE_LIQUID_RT".
            When I request to repack the carton with reason "Products damaged during handling".
            Then I should receive a "WARN" message response "Carton cannot be repacked".
            When I request the last carton created.
            Then The carton status should be "CLOSED".


        Rule: I should be required to provide repack reason.
        @api @DIS-355
        Scenario Outline: Attempt to repack a carton with invalid reason
            Given I have a shipment created with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>" and Location Code as "<Location Code>".
            And The Minimum Number of Units in Carton is configured as "<configured_min_products>" products for the customer code "<Customer Code>" and product type "<Product Type>".
            And I have a closed carton with the unit numbers as "<unit_number>" and product codes as "<product_code>" and product types "<product_type>" which were flagged as repack.
            When I request to repack the carton with reason "<comments>".
            Then I should receive a "<message_type>" message response "<Message>".
            When I request the last carton created.
            Then The carton status should be "<carton_status>".
            And The products unit number "<unit_number>" and product code "<product_code>" "should" be packed in the carton.
            Examples:
                | comments                   | message_type | Message                             | carton_status | Customer Code | Product Type               | Carton Tare Weight | Shipment Date | Transportation Reference Number | Location Code    | configured_min_products | unit_number                 | product_code      | product_type                                          | Shipment Date |
                | <null>                     | WARN         | Reason comments is required         | REPACK        | 409           | RP_NONINJECTABLE_LIQUID_RT | 1000               | <tomorrow>    | DIS-355                         | 123456789_DIS355 | 2                       | W036898355905,W036898355757 | E6022V00,E6022V00 | RP_NONINJECTABLE_LIQUID_RT,RP_NONINJECTABLE_LIQUID_RT | <tomorrow>    |
                | <comment_greater_than_250> | WARN         | Reason comments cannot exceed : 250 | REPACK        | 409           | RP_NONINJECTABLE_LIQUID_RT | 1000               | <tomorrow>    | DIS-355                         | 123456789_DIS355 | 2                       | W036898355905,W036898355757 | E6022V00,E6022V00 | RP_NONINJECTABLE_LIQUID_RT,RP_NONINJECTABLE_LIQUID_RT | <tomorrow>    |


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
