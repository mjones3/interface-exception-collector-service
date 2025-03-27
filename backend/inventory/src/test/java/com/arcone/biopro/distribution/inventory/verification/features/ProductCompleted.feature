# Feature Unit Number reference: W036825007000
@api @LAB-399
Feature: Product Completed Event - API
    Rule: As an inventory service I want to save the volume after receiving a product completed event.
        Scenario Outline: Add information into the anticoagulant volume when receiving product completed event
            Given I have the following inventories:
                | Unit Number   | Product Code   | Status    |
                | <Unit Number> | <Product Code> | AVAILABLE |

            When I received a Product Completed event for the following products:
                | Unit Number   | Product Code   | Volume         | Anticoagulant Volume   |
                | <Unit Number> | <Product Code> | <Event Volume> | <Event Anticoagulant Volume> |

            Then the inventory volume should be updated as follows:
                | Unit Number   | Product Code   | Status    | Volume   | Anticoagulant Volume   |
                | <Unit Number> | <Product Code> | AVAILABLE | <Volume> | <Anticoagulant Volume> |

            Examples:
                | Unit Number   | Product Code | Event Volume | Volume | Event Anticoagulant Volume | Anticoagulant Volume |
                | W036825007001 | E765000      | 450          | 450    | 50                         | 50                   |
                | W036825007002 | E765000      |              | 0      |                            | 0                    |
