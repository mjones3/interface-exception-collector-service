@AOA-89
Feature: Generate the Unacceptable Products Report

    Background:
        Given I have removed from the database all the configurations for the location "123456789_DIS356".
        And I have removed from the database all shipments which code contains with "DIS35600".
        And I have removed from the database all shipments from location "123456789" with transportation ref number "DIS-356".
        And The location "123456789_DIS356" is configured with prefix "DIS_356", shipping code "DIS35600", carton prefix "BPM" and prefix configuration "Y".
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

        Rule: I should be able to see the list of products that were flagged as unacceptable.
        Rule: I should be able to see the reason for the product to be flagged as unacceptable.
        Rule: I should be able to see the list of cartons that were flagged unacceptable.
        Rule: I should be able to see the status of the report.
        Rule: I should be able to see the date and time that the report was generated.
        Rule: The system must request the user to repack all the cartons that has unacceptable products.
        Rule: The system must indicate the cartons with unacceptable products in the shipment.
        Rule: I should not be able to close a shipment with products that do not exist in the system.
        Rule: I should not be able to close a shipment with shipped products.
        Rule: I should not be able to close a shipment with discarded products.
        Rule: I should not be able to close a shipment with quarantined products.
        Rule: I should not be able to close a shipment with expired products.
        @api @DIS-356
        Scenario: Generate unacceptable summary report with flagged products
            Given I have a shipment created with the Customer Code as "409" , Product Type as "RP_FROZEN_WITHIN_120_HOURS", Carton Tare Weight as "100", Shipment Date as "<tomorrow>", Transportation Reference Number as "DIS-356" and Location Code as "123456789_DIS356".
            And The Minimum Number of Units in Carton is configured as "4" products for the customer code "409" and product type "RP_FROZEN_WITHIN_120_HOURS".
            And I have a closed carton with the unit numbers as "W036898786905,W036898786757,W036898786758,W036898786756" and product codes as "E6022V00,E6022V00,E6022V00,E6022V00" which become unacceptable.
            When I request to close the shipment with ship date as "<ship_date>"
            Then I should receive a "SUCCESS" message response "Close Shipment is in progress".
            And The shipment status should be "PROCESSING"
            And The system process the unacceptable units report.
            When I request the last created shipment data.
            Then The find shipment response should have the following information:
                | Information          | Value        |
                | Shipment Number      | DIS_356DIS356|
                | Total Cartons        | 1            |
                | Carton Number Prefix | BPMMH1       |
                | Sequence Number      | 1            |
                | Carton Status        | REPACK       |
                | Shipment Status      | IN_PROGRESS  |
            When I request to print the Unacceptable Products Report.
            Then The Unacceptable Products Report status should be "COMPLETED_FAILED"
            And The Unacceptable Products Report should contain:
                | Response property | Information Type       | Information Value                                                                                                                                                                                                                          |
                | reportTitle       | Report Title           | Unacceptable Product Report                                                                                                                                                                                                                |
                | shipmentNumber    | Shipment Number Prefix | BPMMH                                                                                                                                                                                                                                      |
                | unitNumbers       | Unit Number            | W036898786905,W036898786757,W036898786758,W036898786756                                                                                                                                                                                    |
                | productCodes      | Product Code           | E6022V00,E6022V00,E6022V00,E6022V00                                                                                                                                                                                                        |
                | cartonNumbers     | Carton Number Prefix   | BPMMH1,BPMMH1,BPMMH1,BPMMH1                                                                                                                                                                                                                                    |
                | cartonSequences   | Carton Sequence        | 1,1,1,1                                                                                                                                                                                                                                    |
                | reasons           | Reason for Failure     | This product is not in the inventory and cannot be shipped,This product is discarded and cannot be shipped,This product is quarantined and cannot be shipped,This product is expired and has been discarded. Place in biohazard container. |


        Rule: I should not be able to close a shipment with products flagged as unacceptable.
        @api @DIS-356
        Scenario Outline: Attempt to close shipment with unacceptable products
            Given I have a shipment created with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>" and Location Code as "<Location Code>".
            And The Minimum Number of Units in Carton is configured as "<configured_min_products>" products for the customer code "<Customer Code>" and product type "<Product Type>".
            And I have a closed carton with the unit numbers as "<unit_number>" and product codes as "<product_code>" which become unacceptable.
            When I request to close the shipment with ship date as "<ship_date>"
            Then I should receive a "SUCCESS" message response "Close Shipment is in progress".
            And The shipment status should be "PROCESSING"
            And The system process the unacceptable units report.
            When I request to close the shipment with ship date as "<tomorrow>"
            Then I should receive a "WARN" message response "Shipment cannot be closed with open cartons".
            Examples:
                | Customer Code | Product Type               | Carton Tare Weight | Shipment Date | Transportation Reference Number | Location Code | configured_min_products | unit_number                 | product_code       | Shipment Date |
                | 409           | RP_NONINJECTABLE_LIQUID_RT | 1000               | <tomorrow>    | DIS-347                         | 123456789     | 2                       | W036898786808,W036898786809 | E2488V00, E2488V00 | <tomorrow>    |


        Rule: The shipment status must be updated to Closed if there are no unacceptable products after running the unsuitable report.
        Rule: I should be able to see a message indicating that are no unacceptable products if the unsuitable report is passed successfully.
        @api @DIS-356
        Scenario: Generate report for shipment with no unacceptable products
            Given I have a shipment created with the Customer Code as "409" , Product Type as "RP_FROZEN_WITHIN_120_HOURS", Carton Tare Weight as "100", Shipment Date as "<tomorrow>", Transportation Reference Number as "DIS-356" and Location Code as "123456789_DIS356".
            And The Minimum Number of Units in Carton is configured as "4" products for the customer code "409" and product type "RP_FROZEN_WITHIN_120_HOURS".
            And I have a closed carton with the unit numbers as "W036898786800" and product codes as "E6022V00".
            When I request to close the shipment with ship date as "<tomorrow>"
            Then I should receive a "SUCCESS" message response "Close Shipment is in progress".
            And The shipment status should be "PROCESSING"
            And The system process the unacceptable units report.
            When I request the last created shipment data.
            Then The find shipment response should have the following information:
                | Information          | Value  |
                | Total Cartons        | 1      |
                | Carton Number Prefix | BPMMH1 |
                | Sequence Number      | 1      |
                | Carton Status        | CLOSED |
                | Shipment Status      | CLOSED |
            When I request to print the Unacceptable Products Report.
            Then I should see a message "The shipment contains no defective products" indicating there are not unacceptable products in the shipment.
            And The Unacceptable Products Report status should be "COMPLETED"



        Rule: I should be able to see the list of products that were flagged as unacceptable.
        Rule: I should be able to see the reason for the product to be flagged as unacceptable.
        Rule: I should be able to see the list of cartons that were flagged unacceptable.
        Rule: I should be able to see the status of the report.
        Rule: I should be able to see the date and time that the report was generated.
        Rule: The system must request the user to repack all the cartons that has unacceptable products.
        Rule: The system must indicate the cartons with unacceptable products in the shipment.
        Rule: I should not be able to close a shipment with products that do not exist in the system.
        Rule: I should not be able to close a shipment with shipped products.
        Rule: I should not be able to close a shipment with discarded products.
        Rule: I should not be able to close a shipment with quarantined products.
        Rule: I should not be able to close a shipment with expired products.
        @ui @DIS-356
        Scenario: Generate unacceptable summary report with flagged products
            Given I have a shipment created with the Customer Code as "409" , Product Type as "RP_FROZEN_WITHIN_120_HOURS", Carton Tare Weight as "100", Shipment Date as "<tomorrow>", Transportation Reference Number as "DIS-356" and Location Code as "123456789_DIS356".
            And The Minimum Number of Units in Carton is configured as "4" products for the customer code "409" and product type "RP_FROZEN_WITHIN_120_HOURS".
            And I have a closed carton with the unit numbers as "W036898786905,W036898786757,W036898786758,W036898786756" and product codes as "E6022V00,E6022V00,E6022V00,E6022V00" which become unacceptable.
            When I request to close the shipment with ship date as "<ship_date>"
            Then I should receive a "SUCCESS" message response "Close Shipment is in progress".
            And The shipment status should be "PROCESSING"
            And The system process the unacceptable units report.
            When I navigate to the shipment details page for the last shipment created.
            Then I should see the following shipment information:
                | Field                      | Value                         |
                | Shipment Number Prefix     | BPM2765                       |
                | Customer Code              | 410                           |
                | Customer Name              | BIO PRODUCTS                  |
                | Product Type               | RP NONINJECTABLE REFRIGERATED |
                | Shipment Status            | IN_PROGRESS                   |
                | Shipment Date              | <tomorrow>                    |
                | Transportation Ref. Number | DIS335                        |
                | Total Cartons              | 1                             |
                | Carton Status              | REPACK                        |
            And I should see the unacceptable units report information:
                | Field      | Value            |
                | Last Run   | 12/08/2023 13:30 |
                | View  Icon | enabled          |
            When I choose to open the unacceptable units report.
            Then I should see the following unacceptable units report information:
                | Information Type       | Information Value                                                                                                                                                                                                                          |
                | Report Title           | Unacceptable Product Report                                                                                                                                                                                                                |
                | Shipment Number Prefix | BPMMH                                                                                                                                                                                                                                      |
                | Unit Number            | W036898786905,W036898786757,W036898786758,W036898786756                                                                                                                                                                                    |
                | Product Code           | E6022V00,E6022V00,E6022V00,E6022V00                                                                                                                                                                                                        |
                | Carton Number Prefix   | BPMMH1,BPMMH1,BPMMH1,BPMMH1                                                                                                                                                                                                                                    |
                | Carton Sequence        | 1,1,1,1                                                                                                                                                                                                                                    |
                | Reason for Failure     | This product is not in the inventory and cannot be shipped,This product is discarded and cannot be shipped,This product is quarantined and cannot be shipped,This product is expired and has been discarded. Place in biohazard container. |



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
