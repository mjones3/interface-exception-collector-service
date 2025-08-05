# Feature Unit Number reference: W777725007000
@api @LAB-399 @MGF1-290 @cleanUpAll
Feature: Product Completed Event - API
    Rule: As an inventory service I want to save the volume after receiving a product completed event.
        Scenario Outline: Add information into the volume when receiving product completed event
            Given I have the following inventories:
                | Unit Number   | Product Code   | Status    |
                | <Unit Number> | <Product Code> | AVAILABLE |

            When I received a Product Completed event for the following products:
                | Unit Number   | Product Code   | Volume   | Anticoagulant Volume   |
                | <Unit Number> | <Product Code> | <Volume> | <Anticoagulant Volume> |

            Then the inventory volume should be updated as follows:
                | Unit Number   | Product Code   | Status    | Volume   | Anticoagulant Volume   | Properties   |
                | <Unit Number> | <Product Code> | AVAILABLE | <Volume> | <Anticoagulant Volume> | <Properties> |

            Examples:
                | Unit Number   | Product Code | Volume | Anticoagulant Volume | Properties  |
                | W777725007001 | E765000      | 450    | 50                   | COMPLETED=Y |

        @LAB475 @AOA-83
    Rule: As an inventory service I want to map ABO/Rh for product labels to the test result included in the product completed event.
        Scenario Outline: Map ABO/Rh for product labels to test result
            Given I have the following inventories:
                | Unit Number   | Product Code   | Status    | ABO/Rh |
                | <Unit Number> | <Product Code> | AVAILABLE |        |

            When I received a Product Completed event for the following products:
                | Unit Number   | Product Code   | Volume   | Anticoagulant Volume   | ABO/Rh   |
                | <Unit Number> | <Product Code> | <Volume> | <Anticoagulant Volume> | <ABO/Rh> |

            Then the inventory statuses should be updated as follows:
                | Unit Number   | Product Code   | Status    | Volume   | Anticoagulant Volume   | ABO/Rh   |
                | <Unit Number> | <Product Code> | AVAILABLE | <Volume> | <Anticoagulant Volume> | <ABO/Rh> |

            Examples:
                | Unit Number   | Product Code | Volume | Anticoagulant Volume | ABO/Rh |
                | W777725007002 | E765000      | 450    | 50                   | OP     |
