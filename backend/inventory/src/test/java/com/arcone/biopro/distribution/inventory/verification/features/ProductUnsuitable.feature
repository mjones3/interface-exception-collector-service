# Feature Unit Number reference: W036825010000
@api @AOA-106 @LAB-356 @skipOnPipeline
Feature: Product Unsuitable Event - API
    As an inventory service I want to update the status of an inventory after receiving an Product Unsuitable event.

    Scenario Outline: Flag an inventory as unsuitable after receiving a Product Unsuitable event.
        Given I have the following inventories:
            | Unit Number   | Product Code   | Status    | Unsuitable reason |
            | <Unit Number> | <Product Code> | <Status>  | Empty             |
            | <Unit Number> | E4693V00       | AVAILABLE | Empty             |
        When I received a Product Unsuitable event with unit number "<Unit Number>", product code "<Product Code>" and reason "<Unsuitable reason>"
        Then the inventories should be:
            | Unit Number   | Product Code   | Status    | Unsuitable reason   |
            | <Unit Number> | <Product Code> | <Status>  | <Unsuitable reason> |
            | <Unit Number> | E4693V00       | AVAILABLE | Empty               |
        Examples:
            | Unit Number   | Product Code | Status     | Unsuitable reason              |
            | W036825010001 | E4689V00     | AVAILABLE  | POSITIVE_REACTIVE_TEST_RESULTS |
            | W036825010002 | E4689V00     | IN_TRANSIT | POSITIVE_REACTIVE_TEST_RESULTS |
            | W036825010003 | E4689V00     | DISCARDED  | POSITIVE_REACTIVE_TEST_RESULTS |
            | W036825010004 | E4689V00     | SHIPPED    | POSITIVE_REACTIVE_TEST_RESULTS |

    Scenario Outline: Do not flag an inventory as unsuitable after receiving a Product Unsuitable event.
        Given I have the following inventories:
            | Unit Number   | Product Code   | Status    | Unsuitable reason |
            | <Unit Number> | <Product Code> | <Status>  | Empty             |
            | <Unit Number> | E4693V00       | AVAILABLE | Empty             |
        When I received a Product Unsuitable event with unit number "<Unit Number>", product code "<Product Code>" and reason "<Unsuitable reason>"
        Then the inventories should be:
            | Unit Number   | Product Code   | Status    | Unsuitable reason   |
            | <Unit Number> | <Product Code> | <Status>  | <Unsuitable reason> |
            | <Unit Number> | E4693V00       | AVAILABLE | Empty               |
        Examples:
            | Unit Number   | Product Code | Status    | Unsuitable reason |
            | W036825010005 | E4689V00     | CONVERTED | Empty             |
