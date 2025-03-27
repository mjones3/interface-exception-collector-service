@api
Feature: Customer Configuration

    @DIS-331
    Scenario: Check Available Recovered Plasma Shipping Customers
        Given The following customers are defined as Recovered Plasma Shipping customers.
        | Code | Name               | Customer Type    | State | City            |
        | 408  | Prothya            | RECOVERED_PLASMA | NJ    | North Brunswick |
        | 409  | Southern Biologics | RECOVERED_PLASMA | FL    | Tallahassee     |
        | 410  | Bio Products       | RECOVERED_PLASMA | GA    | Atlanta         |
        When I request to list all Recovered Plasma Shipping Customers.
        Then the response should contain all Recovered Plasma Shipping Customers.
