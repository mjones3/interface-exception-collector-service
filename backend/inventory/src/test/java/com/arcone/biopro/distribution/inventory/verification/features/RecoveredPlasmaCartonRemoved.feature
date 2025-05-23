# Feature Unit Number reference: W036825013000
@api @LAB-408 @skipOnPipeline
Feature: Recovered Plasma Carton Removed Event - API
    As an inventory service I want to update the status of an inventory after receiving an Recovered Plasma Carton Removed event.

    Scenario Outline: Update the status of multiple inventories with different product after receiving a Recovered Plasma Carton Removed event.
        Given I have the following inventories:
            | Unit Number   | Product Code          | Status | Carton Number   |
            | <Unit Number> | <First Product Code>  | PACKED | <Carton Number> |
            | <Unit Number> | <Second Product Code> | PACKED | <Carton Number> |
        When I received a Recovered Plasma Carton Removed Event for carton number "<Carton Number>"
            | Unit Number   | Product Code          |
            | <Unit Number> | <First Product Code>  |
            | <Unit Number> | <Second Product Code> |
        Then the inventory statuses should be updated as follows:
            | Unit Number   | Product Code          | Status    |
            | <Unit Number> | <First Product Code>  | AVAILABLE |
            | <Unit Number> | <Second Product Code> | AVAILABLE |
        Examples:
            | Carton Number | Unit Number   |  | First Product Code | Second Product Code |
            | CN1001        | W036825013001 |  | E4689V00           | E4693V00            |
            | CN1002        | W036825013002 |  | E1624VA0           | E070100             |
