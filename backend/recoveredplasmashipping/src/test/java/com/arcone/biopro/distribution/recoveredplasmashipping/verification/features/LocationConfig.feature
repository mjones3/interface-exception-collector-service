@api
Feature: Location Configuration

    @DIS-332
    Scenario: Check Available Recovered Plasma Shipping Locations
        Given The following locations are defined as Recovered Plasma Shipping Locations
        | Location Code | Location Name |
        | 123456789     |  MDL Hub 1    |
        | 234567891     |  MDL Hub 2    |
        When I request to list all Recovered Plasma Shipping Locations
        Then the response should contain all Recovered Plasma Shipping Locations
