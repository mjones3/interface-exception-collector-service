@api @LAB-253 @AOA-152
Feature: Product Created event
    Rule: As an inventory service I want to listen to product created event so that I can create the inventory, convert it's parent if any, and manage it's statuses
        Scenario Outline: Create the inventory as AVAILABLE and UNLABELED and convert it's parent after receiving a Product Created event.
            Given I have the following inventories:
                | Unit Number   | Product Code          | Status    |
                | <Unit Number> | <Parent Product Code> | AVAILABLE |

            When I received a Product Created event for the following products:
                | Unit Number   | Product Code   | Parent Product Code   |
                | <Unit Number> | <Product Code> | <Parent Product Code> |

            Then the parent inventory statuses should be updated as follows:
                | Unit Number   | Product Code          | Status    |
                | <Unit Number> | <Parent Product Code> | CONVERTED |

            And the inventory statuses should be updated as follows:
                | Unit Number   | Product Code   | Status    | Is Labeled |
                | <Unit Number> | <Product Code> | AVAILABLE | false      |

            Examples:
                | Unit Number   | Parent Product Code | Product Code |
                | W036824211111 | PLASAPHP            | E765000      |
                | W036824211112 | RBCAPH              | E453200      |
                | W036824211113 | WHOLEBLOOD          | E011200      |
                | W036824211113 | WHOLEBLOOD          | RBC          |
                | W036824211113 | WHOLEBLOOD          | PLASMA       |
                | W036824211113 | RBC                 | E016700      |
                | W036824211113 | PLASMA              | E070100      |
