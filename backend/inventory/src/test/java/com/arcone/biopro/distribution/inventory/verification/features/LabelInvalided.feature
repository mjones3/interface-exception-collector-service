# Feature Unit Number reference: W036825006000
@api @LAB-433
Feature: Label Invalided Event
    Rule: As an inventory service I want to listen to label invalided event so that I can update the inventory removing label and licensing flag
        Scenario Outline: Update the inventory information about label adn license flags after receiving a Label Invalided Event.
            Given I have the following inventories:
                | Unit Number   | Product Code   | Is licensed | Is Labeled |
                | <Unit Number> | <Product Code> | YES         | YES        |

            When I received a Label Invalided event for the following products:
                | Unit Number   | Product Code   |
                | <Unit Number> | <Product Code> |

            Then the inventory statuses should be updated as follows:
                | Unit Number   | Product Code   | Status    | Is Labeled | Is licensed |
                | <Unit Number> | <Product Code> | AVAILABLE | false      | false       |

            Examples:
                | Unit Number   | Product Code |
                | W036825006001 | E162400      |
                | W036825006002 | E162400      |
