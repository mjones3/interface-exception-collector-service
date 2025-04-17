@api @AOA-89
Feature: Recovered Plasma Criteria Configuration

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
