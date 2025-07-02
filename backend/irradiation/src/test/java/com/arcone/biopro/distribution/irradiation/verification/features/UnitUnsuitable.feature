# Feature Unit Number reference: W777725017000
@api @LAB-292 @AOA-106 @skipOnPipeline @cleanUpAll
Feature: Unit Unsuitable Event - API
    As an inventory service I want to update the status of an inventory after receiving an Unsuitable event.

    Scenario Outline: Update the status of multiple inventories with different product code but same division code after receiving a Unit Unsuitable event.
        Given I have the following inventories:
            | Unit Number   | Product Code          | Status          | Unsuitable reason |
            | <Unit Number> | <First Product Code>  | <First Status>  | Empty             |
            | <Unit Number> | <Second Product Code> | <Second Status> | Empty             |
        When I received a Unit Unsuitable event with unit number "<Unit Number>" and reason "POSITIVE_REACTIVE_TEST_RESULTS"
        Then the inventory statuses should be updated as follows:
            | Unit Number   | Product Code          | Status          | Unsuitable reason   |
            | <Unit Number> | <First Product Code>  | <First Status>  | <Unsuitable reason> |
            | <Unit Number> | <Second Product Code> | <Second Status> | <Unsuitable reason> |
        Examples:
            | Unit Number   | First Product Code | Second Product Code | First Status | Second Status | Unsuitable reason              |
            | W777725017001 | E4689V00           | E4693V00            | AVAILABLE    | AVAILABLE     | POSITIVE_REACTIVE_TEST_RESULTS |
            | W777725017002 | RBC                | E070100             | AVAILABLE    | AVAILABLE     | POSITIVE_REACTIVE_TEST_RESULTS |
            | W777725017003 | E1624VA0           | E1624VB0            | AVAILABLE    | SHIPPED       | POSITIVE_REACTIVE_TEST_RESULTS |
            | W777725017004 | E1624VA0           | E1624VB0            | IN_TRANSIT   | DISCARDED     | POSITIVE_REACTIVE_TEST_RESULTS |
            | W777725017005 | E1624VA0           | RBC                 | CONVERTED    | CONVERTED     | Empty                          |
