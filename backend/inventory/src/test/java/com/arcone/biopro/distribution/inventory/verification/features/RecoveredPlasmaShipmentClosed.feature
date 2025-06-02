# Feature Unit Number reference: W777725013000
@api @LAB-470 @skipOnPipeline
Feature: Recovered Plasma Shipment Closed Event - API
    As an inventory service I want to update the status of an inventory after receiving an Recovered Plasma Shipment Closed event.

    Scenario Outline: Update the status of multiple inventories with different product after receiving a Recovered PlasmaShipment Closed event.
        Given I have the following inventories:
            | Unit Number   | Product Code    | Status |
            | <Unit Number> | <Product Code>  | PACKED |

        When I received a Recovered Plasma Shipment Closed Event
            | Unit Number   | Product Code    |Carton Number  |
            | <Unit Number> | <Product Code>  |<Carton Number>|

        Then the inventory statuses should be updated as follows:
            | Unit Number   | Product Code          | Status  |
            | <Unit Number> | <Product Code>        | SHIPPED |

        Examples:
            | Carton Number | Unit Number   | Product Code |
            | CN1001        | W777725013001 | E4689V00     |
            | CN1002        | W777725013002 | E1624VA0     |
