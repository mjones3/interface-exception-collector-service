@api @AOA-89
Feature: Recovered Plasma Criteria Configuration

    Background:
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

    @DIS-333
    Scenario: Check Available Recovered Plasma Product Type Criteria configurations
        Given The following product types are defined as Recovered Plasma Product Type Criteria
        | Customer Code | Product Type               | Product Type Description   |
        | 408           | RP_FROZEN_WITHIN_120_HOURS | RP FROZEN WITHIN 120 HOURS |
        | 408           | RP_FROZEN_WITHIN_24_HOURS  | RP FROZEN WITHIN 24 HOURS  |
        | 408           | RP_NONINJECTABLE_FROZEN    | RP NONINJECTABLE FROZEN    |
        | 409           | RP_NONINJECTABLE_LIQUID_RT | RP NONINJECTABLE LIQUID RT |
        | 409           | RP_FROZEN_WITHIN_72_HOURS  | RP FROZEN WITHIN 72 HOURS  |
        When I request to list all Product Types by customer code "408"
        Then the response should contain the following product types
        | Product Type               | Product Type Description   |
        | RP_FROZEN_WITHIN_120_HOURS | RP FROZEN WITHIN 120 HOURS |
        | RP_FROZEN_WITHIN_24_HOURS  | RP FROZEN WITHIN 24 HOURS  |
        | RP_NONINJECTABLE_FROZEN    | RP NONINJECTABLE FROZEN    |
        When I request to list all Product Types by customer code "409"
        Then the response should contain the following product types
        | Product Type               | Product Type Description   |
        | RP_NONINJECTABLE_LIQUID_RT | RP NONINJECTABLE LIQUID RT |

    @DIS-340
    Scenario Outline: Check Recovered Plasma Shipping Criteria Configurations
        Given The following Recovered Plasma Shipping Criteria are defined:
            | Customer Code | Product Type                  | Product Code | Min Vol | Min. Number of Units in Carton | Max. Number of Units in Carton |
            | 408           | RP_FROZEN_WITHIN_120_HOURS    | E6022V00     | 165     |          20                    |       20                       |
            | 408           | RP_FROZEN_WITHIN_24_HOURS     | E2534V00     | 200     |          20                    |       20                       |
            | 408           | RP_NONINJECTABLE_FROZEN       | E2603V00     | <null>  |          25                    |       30                       |
            | 409           | RP_NONINJECTABLE_LIQUID_RT    | E2488V00     | <null>  |          15                    |       20                       |
            | 409           | RP_FROZEN_WITHIN_72_HOURS     | E5880V00     | <null>  |          15                    |       20                       |
            | 410           | RP_NONINJECTABLE_REFRIGERATED | E6170V00     | <null>  |          15                    |       20                       |
        When I request to list Recovered Plasma Shipping Criteria by customer code "<Customer Code>" and product type "<Product Type>"
        Then the response should contain the Recovered Plasma Shipping Criteria Configurations like Product Code as "<Product Code>"  Min Vol as "<Min Vol>" Min. Number of Units in Carton as "<Min. Number of Units in Carton>" and Max. Number of Units in Carton as "<Max. Number of Units in Carton>"
            Examples:
            | Customer Code | Product Type                  | Product Code | Min Vol | Min. Number of Units in Carton | Max. Number of Units in Carton |
            | 408           | RP_FROZEN_WITHIN_120_HOURS    | E6022V00     | 165     |          20                    |       20                       |
            | 408           | RP_FROZEN_WITHIN_24_HOURS     | E2534V00     | 200     |          20                    |       20                       |
            | 408           | RP_NONINJECTABLE_FROZEN       | E2603V00     | <null>  |          25                    |       30                       |
            | 409           | RP_NONINJECTABLE_LIQUID_RT    | E2488V00     | <null>  |          15                    |       20                       |
            | 409           | RP_FROZEN_WITHIN_72_HOURS     | E5880V00     | <null>  |          15                    |       20                       |
            | 410           | RP_NONINJECTABLE_REFRIGERATED | E6170V00     | <null>  |          15                    |       20                       |
