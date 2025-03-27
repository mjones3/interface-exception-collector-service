@api @LAB-399
Feature: Product Completed Event - API
    Rule: As an inventory service I want to save the volume after receiving a product completed event.
        Scenario Outline: Add information into the anticoagulant volume when receiving product completed event
            Given I have the following inventories:
                | Unit Number   | Product Code   | Status    |
                | <Unit Number> | <Product Code> | AVAILABLE |

            When I received a Product Completed event for the following products:
                | Unit Number   | Product Code   | Volume           |
                | <Unit Number> | <Product Code> | <Payload Volume> |

            Then the inventory volume should be updated as follows:
                | Unit Number   | Product Code   | Status    | Volume   |
                | <Unit Number> | <Product Code> | AVAILABLE | <Volume> |

            Examples:
                | Unit Number   | Product Code | Payload Volume | Volume |
                | W036824211111 | E765000      | 50             | 50     |
                | W036824211112 | E765000      |                | 0      |
