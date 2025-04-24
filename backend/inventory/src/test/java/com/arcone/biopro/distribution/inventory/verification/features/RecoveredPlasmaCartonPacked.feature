# Feature Unit Number reference: W036825013000
@api @LAB-292 @AOA-106
Feature: Unit Unsuitable Event - API
    As an inventory service I want to update the status of an inventory after receiving an Unsuitable event.

    Scenario Outline: Update the status of multiple inventories with different product code but same division code after receiving a Unit Unsuitable event.
        Given I have the following inventories:
            | Unit Number   | Product Code          |
            | <Unit Number> | <First Product Code>  |
            | <Unit Number> | <Second Product Code> |
        When I received a Recovered Plasma Carton Packed Event for carton number "<Carton Number>"
            | Unit Number   | Product Code          |
            | <Unit Number> | <First Product Code>  |
            | <Unit Number> | <Second Product Code> |
        Then the inventory statuses should be updated as follows:
            | Unit Number   | Product Code          | Status |
            | <Unit Number> | <First Product Code>  | PACKED |
            | <Unit Number> | <Second Product Code> | PACKED |
        Examples:
            | Carton Number | Unit Number   |  | First Product Code | Second Product Code |
            | CN1001        | W036825013001 |  | E4689V00           | E4693V00            |
            | CN1002        | W036825013002 |  | E1624VA0           | E070100             |
