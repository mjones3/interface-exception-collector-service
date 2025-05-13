# Feature Unit Number reference: W036825008000
@api @LAB-427
Feature: Product Modified event
    Rule: As an inventory service I want to listen to product modified event so that I can modified and create a new inventory and manage it's statuses
        Scenario Outline: Create the inventory as AVAILABLE and UNLABELED and modified it's parent after receiving a Product Modified event.
            Given I have the following inventories:
                | Unit Number   | Product Code          | Status    | ABO/RH |
                | <Unit Number> | <Parent Product Code> | AVAILABLE | OP     |

            When I received a Product Modified event for the following products:
                | Unit Number   | Product Code   | Product Description | Parent Product Code | Product Family               | Expiration Date | Expiration Time | Modification Location | Modification Date | Volume | Weight |
                | <Unit Number> | <Product Code> | APH LR RBC FRZ C1   | E067800             | RED_BLOOD_CELLS_LEUKOREDUCED | 02/15/2026      | 23:59           | 1FS                   | 02/15/2025        | 250    | 234    |

            Then the parent inventory statuses should be updated as follows:
                | Unit Number   | Product Code          | Status |
                | <Unit Number> | <Parent Product Code> | MODIFIED |

            And the inventory statuses should be updated as follows:
                | Unit Number   | Product Code   | Status    | Is Labeled | Temperature Category | ABO/RH | Volume | Weight | Modification Location | Expiration Date  | Modification Date    |
                | <Unit Number> | <Product Code> | AVAILABLE | false      | FROZEN               | OP     | 250    | 234    | 1FS                   | 2026-02-15T23:59 | 2025-02-15T00:00:00Z |

            Examples:
                | Unit Number   | Parent Product Code | Product Code |
                | W036825008001 | E067800             | E510600      |
                | W036825008002 | E0678V00            | E510500      |
