# Feature Unit Number reference: W777725006000
@api @LAB-253 @AOA-152 @skipOnPipeline
Feature: Label Applied Event
    Rule: As an inventory service I want to listen to label applied event so that I can update the inventory information about license, product code with 6th digit and labeled status
        Scenario Outline: Update the inventory information about license, product code with 6th digit and labeled status after receiving a Label Applied Event.
            Given I have the following inventories:
                | Unit Number   | Product Code   | Is licensed |
                | <Unit Number> | <Product Code> | MISSING     |

            When I received a Label Applied event for the following products:
                | Unit Number   | Product Code         | Is licensed   |
                | <Unit Number> | <Final Product Code> | <Is licensed> |

            Then the inventory statuses should be updated as follows:
                | Unit Number   | Product Code         | Status    | Is Labeled | Is licensed   |
                | <Unit Number> | <Final Product Code> | AVAILABLE | true       | <Is licensed> |

            Examples:
                | Unit Number   | Product Code | Final Product Code | Is licensed |
                | W777725006001 | E162400      | E1624V00           | true        |
                | W777725006002 | E162400      | E1624V00           | false       |
