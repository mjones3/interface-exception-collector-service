# Feature Unit Number reference: W777725020000
@api @LAB-397 @skipOnPipeline @cleanUpAll
Feature: Recovered Plasma Carton Unpacked Event - API
    As an inventory service I want to update the status of an inventory after receiving an Recovered Plasma Carton Unpacked event.

    Scenario Outline: Update the status of multiple inventories with different product after receiving a Recovered Plasma Carton Unpacked event.
        Given I have the following inventories:
            | Unit Number   | Product Code          | Status | Carton Number   |
            | <Unit Number> | <First Product Code>  | PACKED | <Carton Number> |
            | <Unit Number> | <Second Product Code> | PACKED | <Carton Number> |
        When I received a Recovered Plasma Carton Unpacked Event for carton number "<Carton Number>"
            | Unit Number   | Product Code          |
            | <Unit Number> | <First Product Code>  |
            | <Unit Number> | <Second Product Code> |
        Then the inventory statuses should be updated as follows:
            | Unit Number   | Product Code          | Status    |
            | <Unit Number> | <First Product Code>  | AVAILABLE |
            | <Unit Number> | <Second Product Code> | AVAILABLE |
        Examples:
            | Carton Number | Unit Number   |  | First Product Code | Second Product Code |
            | CN1001        | W777725020001 |  | E4689V00           | E4693V00            |
            | CN1002        | W777725020002 |  | E1624VA0           | E070100             |
