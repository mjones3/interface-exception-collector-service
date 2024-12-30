@api @LAB-256 @AOA-152
Feature: CheckIn Completed Event
    Rule: As an inventory service I want to listen to check-in completed event so that I can create the inventory
        Scenario Outline: Create the inventory as AVAILABLE and UNLABELED after receiving a Product Created event.
            When I received a CheckIn Completed event for the following products:
                | Unit Number   | Product Code   |
                | <Unit Number> | <Product Code> |

            And the inventory statuses should be updated as follows:
                | Unit Number   | Product Code   | Status    | Is Labeled |
                | <Unit Number> | <Product Code> | AVAILABLE | false      |

            Examples:
                | Unit Number   | Product Code |
                | W036824411111 | WHOLEBLOOD   |
                | W036824411112 | PLASAPHP     |
                | W036824411113 | RBCAPH       |

