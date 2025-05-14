@AOA-89
Feature: Close Shipment

    Background:
        Given I have removed from the database all the configurations for the location "123456789_DIS347".
        And I have removed from the database all shipments which code contains with "DIS34700".
        And I have removed from the database all shipments from location "123456789" with transportation ref number "DIS-347".
        And The location "123456789_DIS347" is configured with prefix "DIS_347", shipping code "DIS34700", carton prefix "BPM" and prefix configuration "Y".
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


    Rule: I should required to provide a ship date when closing the shipment.
    Rule: I should be able to close a shipment only if all the products in the shipment are acceptable products.
    Rule: The system should trigger the generation of the unsuitable products report before closing the shipment.
    Rule: The status of shipment must be updated to processing while the unsuitable report is running.
    Rule: The system should begin the generation of the unsuitable products report when the shipment is closed.
    @api @DIS-347
    Scenario Outline: Successfully closing a shipment
        Given I have a shipment created with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>" and Location Code as "<Location Code>".
        And The Minimum Number of Units in Carton is configured as "<configured_min_products>" products for the customer code "<Customer Code>" and product type "<Product Type>".
        And I have a closed carton with the unit numbers as "<unit_number>" and product codes as "<product_code>".
        When I request to close the shipment with ship date as "<Shipment Date>"
        Then I should receive a "SUCCESS" message response "Close Shipment is in progress".
        And The shipment status should be "PROCESSING"
        Examples:
            | Customer Code | Product Type              | Carton Tare Weight | Shipment Date | Transportation Reference Number | Location Code | configured_min_products | unit_number                 | product_code       |
            | 409           | RP_NONINJECTABLE_LIQUID_RT | 1000              | <tomorrow>    | DIS-347                         | 123456789     | 2                       | W036898347808,W036898347809 | E2488V00, E2488V00 |


    Rule: I should required to provide a ship date when closing the shipment.
    Rule: I should be requested to confirm the close of the shipment.
    Rule: I should be able to close a shipment only if all the products in the shipment are acceptable products.
    Rule: The system should trigger the generation of the unsuitable products report before closing the shipment.
    Rule: The status of shipment must be updated to processing while the unsuitable report is running.
    Rule: The system should not allow any modifications while the unsuitable products report is being generated.
    Rule: The system should begin the generation of the unsuitable products report when the shipment is closed.
    @ui @DIS-347
    Scenario Outline: Successfully closing a shipment with valid ship date
        Given I have a shipment created with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>" and Location Code as "<Location Code>".
        And The Minimum Number of Units in Carton is configured as "<configured_min_products>" products for the customer code "<Customer Code>" and product type "<Product Type>".
        And I have a closed carton with the unit numbers as "<unit_number>" and product codes as "<product_code>".
        When I navigate to the shipment details page for the last shipment created.
        Then The close shipment option should be "enabled".
        When I choose to close the shipment.
        Then I should see a "Close Shipment" message: "The shipment will be closed. Are you sure you want to continue?".
        And I should have the shipment date as "<Shipment Date>".
        When I confirm to close the shipment.
        Then I should see a "SUCCESS" message: "Close Shipment is in progress".
        And The shipment status should be updated to "CLOSED"
        And The close shipment option should be "disabled".
        And The Add Carton button should be "disabled".
        And I close the acknowledgment message.
        And I should see a "SYSTEM" static message: "Close Shipment is in progress.".
        Examples:
            | Customer Code | Product Type              | Carton Tare Weight | Shipment Date | Transportation Reference Number | Location Code | configured_min_products | unit_number                 | product_code       | Shipment Date|
            | 409           | RP_NONINJECTABLE_LIQUID_RT | 1000               | <tomorrow>    | DIS-347                         | 123456789     | 2                      | W036898347808,W036898347809 | E2488V00, E2488V00 | <tomorrow>   |




    Rule: I should not be able to enter a shipment date in the past.
    @api @DIS-347
    Scenario Outline: Attempting to close shipment with unacceptable dates
        Given I have a shipment created with the Customer Code as "<Customer Code>" , Product Type as "<Product Type>", Carton Tare Weight as "<Carton Tare Weight>", Shipment Date as "<Shipment Date>", Transportation Reference Number as "<Transportation Reference Number>" and Location Code as "<Location Code>".
        And The Minimum Number of Units in Carton is configured as "<configured_min_products>" products for the customer code "<Customer Code>" and product type "<Product Type>".
        And I have a closed carton with the unit numbers as "<unit_number>" and product codes as "<product_code>".
        When I request to close the shipment with ship date as "<ship_date>"
        Then I should receive a API "<error_type>" error message response "<error_message>".
        When I request the last created shipment data again.
        Then The shipment status should be "IN_PROGRESS"
        Examples:
            | error_type       | error_message                   | ship_date   | Customer Code | Product Type              | Carton Tare Weight | Shipment Date | Transportation Reference Number | Location Code | configured_min_products | unit_number                 | product_code      | Shipment Date|
            | WARN             | Ship date cannot be in the past | 2024-04-01  | 409           | RP_NONINJECTABLE_LIQUID_RT | 1000               | <tomorrow>   | DIS-347                        | 123456789     | 2                      | W036898347808,W036898347809 | E2488V00, E2488V00 | <tomorrow>   |
            | ValidationError  | is not a valid 'Date'           | <null>      | 409           | RP_NONINJECTABLE_LIQUID_RT | 1000               | <tomorrow>   | DIS-347                        | 123456789     | 2                      | W036898347808,W036898347809 | E2488V00, E2488V00 | <tomorrow>   |
            | ValidationError  | is not a valid 'Date'           | 01-01-01    | 409           | RP_NONINJECTABLE_LIQUID_RT | 1000               | <tomorrow>   | DIS-347                        | 123456789     | 2                      | W036898347808,W036898347809 | E2488V00, E2488V00 | <tomorrow>   |

    Rule: I should not be able to close the shipment if the all the cartons in the shipment are not closed.
    @api @DIS-347
    Scenario: Attempting to close shipment with unclosed cartons
        Given I have an empty carton created with the Customer Code as "408" , Product Type as "RP_FROZEN_WITHIN_120_HOURS", Carton Tare Weight as "1000", Shipment Date as "<tomorrow>", Transportation Reference Number as "DIS-347" and Location Code as "123456789".
        And The Minimum Number of Units in Carton is configured as "1" products for the customer code "408" and product type "RP_FROZEN_WITHIN_120_HOURS".
        And I pack an "acceptable" product with the unit number "W036898786800", product code "E6022V00" and product type "RP_FROZEN_WITHIN_120_HOURS".
        When I request to close the shipment with ship date as "<tomorrow>"
        Then I should receive a "WARN" message response "Shipment cannot be closed".
        When I request the last created shipment data again.
        And The shipment status should be "IN_PROGRESS"

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
