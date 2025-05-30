# Feature Unit Number reference: W777725008000
@api @LAB-483
Feature: Product Modified event
    Rule: As an inventory service I want to listen to product modified event so that I can modified and create a new inventory and manage it's statuses
        Scenario Outline: Create the inventory as AVAILABLE and UNLABELED and modified it's parent after receiving a Product Modified event.
            Given I have the following inventories:
                | Unit Number   | Product Code          | Status   | ABO/RH   |
                | <Unit Number> | <Parent Product Code> | <Status> | <ABO/RH> |

            When I received a Product Modified event for the following products:
                | Unit Number   | Product Code   | Product Description   | Parent Product Code   | Product Family   | Expiration Date   | Expiration Time   | Modification Location   | Modification Date   | Volume   | Weight   |
                | <Unit Number> | <Product Code> | <Product Description> | <Parent Product Code> | <Product Family> | <Expiration Date> | <Expiration Time> | <Modification Location> | <Modification Date> | <Volume> | <Weight> |

            Then the parent inventory statuses should be updated as follows:
                | Unit Number   | Product Code          | Status   |
                | <Unit Number> | <Parent Product Code> | MODIFIED |

            And the inventory statuses should be updated as follows:
                | Unit Number   | Product Code   | Status    | Is Labeled | Temperature Category   | ABO/RH   | Volume   | Weight   | Modification Location   | Expiration Date        | Modification Date        |
                | <Unit Number> | <Product Code> | AVAILABLE | false      | <Temperature Category> | <ABO/RH> | <Volume> | <Weight> | <Modification Location> | <Full Expiration Date> | <Full Modification Date> |

            Examples:
                | Unit Number   | Parent Product Code | Product Code | Product Family               | Product Description | Expiration Date | Expiration Time | Modification Location | Modification Date | Volume | Weight | Status    | ABO/RH | Temperature Category | Full Expiration Date | Full Modification Date |
                | W777725008001 | E067800             | E510600      | RED_BLOOD_CELLS_LEUKOREDUCED | APH LR RBC FRZ C1   | 02/15/2026      | 23:59           | 1FS                   | 02/15/2025        | 250    | 234    | AVAILABLE | OP     | FROZEN               | 2026-02-15T23:59     | 2025-02-15T00:00:00Z   |
                | W777725008002 | E0678V00            | E510500      | RED_BLOOD_CELLS_LEUKOREDUCED | APH LR RBC FRZ C1   | 02/15/2026      | 23:59           | 1FS                   | 02/15/2025        | 250    | 234    | AVAILABLE | OP     | FROZEN               | 2026-02-15T23:59     | 2025-02-15T00:00:00Z   |
                | W777725008003 | E0678V00            | E414000      | RED_BLOOD_CELLS_LEUKOREDUCED | WSH APH LR RBC C1   | 02/15/2026      | 23:59           | 1FS                   | 02/15/2025        | 322    | 300    | AVAILABLE | AP     | REFRIGERATED         | 2026-02-15T23:59     | 2025-02-15T00:00:00Z   |
                | W777725008004 | E0678V00            | E456600      | RED_BLOOD_CELLS_LEUKOREDUCED | WSH APH LR RBC C1   | 02/15/2026      | 23:59           | 1FS                   | 02/15/2025        | 322    | 300    | AVAILABLE | AP     | REFRIGERATED         | 2026-02-15T23:59     | 2025-02-15T00:00:00Z   |
                | W777725008005 | E0678V00            | E456700      | RED_BLOOD_CELLS_LEUKOREDUCED | WSH APH LR RBC C2   | 02/15/2026      | 23:59           | 1FS                   | 02/15/2025        | 322    | 300    | AVAILABLE | AP     | REFRIGERATED         | 2026-02-15T23:59     | 2025-02-15T00:00:00Z   |
                | W777725008006 | E0678V00            | E516000      | RED_BLOOD_CELLS_LEUKOREDUCED | WSH LR RBC          | 02/15/2026      | 23:59           | 1FS                   | 02/15/2025        | 322    | 300    | AVAILABLE | AP     | REFRIGERATED         | 2026-02-15T23:59     | 2025-02-15T00:00:00Z   |

