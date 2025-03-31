# Feature Unit Number reference: W036825002000
@api @LAB-312
Feature: Discard Created event
    Rule: As an inventory service I want to listen to discard created event so that I can change the status to DISCARDED
        Scenario Outline: Update the inventory status to DISCARDED after receiving a Discard Created event with comments
            Given I have the following inventories:
                | Unit Number   | Product Code   | Status    |
                | <Unit Number> | <Product Code> | AVAILABLE |

            When I received a Discard Created event for the following products:
                | Unit Number   | Product Code   | Reason   | Comment Length   |
                | <Unit Number> | <Product Code> | <Reason> | <Comment Length> |

            Then the inventory statuses should be updated as follows:
                | Unit Number   | Product Code   | Status    |
                | <Unit Number> | <Product Code> | DISCARDED |

            Examples:
                | Unit Number   | Product Code | Reason  | Comment Length |
                | W036825002001 | E765000      | OTHER   | 25             |
                | W036825002002 | E453200      | EXPIRED | 2000           |

