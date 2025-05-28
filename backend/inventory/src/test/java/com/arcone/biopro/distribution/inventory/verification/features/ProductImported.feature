# Feature Unit Number reference: W777725008000
@api @LAB-253 @AOA-152 @LAB-503
Feature: Product Imported event
    Rule: As an inventory service I want to listen to product created event so that I can create the inventory, convert its parent if any, and manage its statuses
        Scenario Outline: Create the inventory from Product Imported event.


            When I received a Product Imported event for the following products:
                | Temperature Category   | Location   | Unit Number   | Product Code   | Product Family   | Expiration Date   | Product Description   | Abo Rh   | Licensed   | Quarantines   |
                | <Temperature Category> | <Location> | <Unit Number> | <Product Code> | <Product Family> | <Expiration Date> | <Product Description> | <Abo Rh> | <Licensed> | <Quarantines> |

            Then the parent inventory statuses should be updated as follows:
                | Unit Number   | Product Code          | Status    |
                | <Unit Number> | <Parent Product Code> | CONVERTED |

            And the inventory statuses should be updated as follows:
                | Unit Number   | Product Code   | Status    | Is Labeled | Temperature Category   |
                | <Unit Number> | <Product Code> | AVAILABLE | false      | <Temperature Category> |

            Examples:
                | Temperature Category | Location  | Unit Number   | Product Description | Product Code | Expiration Date     | Temperature Category | Product Family             | Abo Rh | Licensed | Quarantines      |
                | FROZEN               | 123456789 | W777725008001 | CP2D PLS MI 120H    | E765000      | 2011-12-03T09:15:30 | FROZEN               | RP_FROZEN_WITHIN_120_HOURS | AP     | true     | OTHER, HEMOLYSIS |
