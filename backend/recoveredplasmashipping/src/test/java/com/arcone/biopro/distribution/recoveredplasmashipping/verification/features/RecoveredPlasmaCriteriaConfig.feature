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


