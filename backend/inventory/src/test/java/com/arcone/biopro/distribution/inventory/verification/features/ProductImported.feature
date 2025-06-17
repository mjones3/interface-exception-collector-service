# Feature Unit Number reference:W777725015000
@api @LAB-503 @cleanUpAll
Feature: Product Imported event
    Rule: As an inventory service I want to listen to product imported event so that I can create the inventory and manage its statuses
        Scenario Outline: Create the imported inventory from Product Imported event.

            When I received a Product Imported event for the following products:
                | Temperature Category   | Location   | Unit Number   | Product Code   | Product Family   | Expiration Date   | Product Description   | Abo Rh   | Licensed   | Quarantines   |
                | <Temperature Category> | <Location> | <Unit Number> | <Product Code> | <Product Family> | <Expiration Date> | <Product Description> | <Abo Rh> | <Licensed> | <Quarantines> |

            Then the inventory statuses should be updated as follows:
                | Status    | Temperature Category   |  | Location   | Unit Number   | Product Code   | Product Family   | Expiration Date   | Product Description   | Abo Rh   | Licensed   | Is Labeled | Is licensed |
                | AVAILABLE | <Temperature Category> |  | <Location> | <Unit Number> | <Product Code> | <Product Family> | <Expiration Date> | <Product Description> | <Abo Rh> | <Licensed> | true          | <Licensed>  |
            And the properties should be added:
                | Unit Number   | Product Code   | Properties            |
                | <Unit Number> | <Product Code> | IMPORTED, QUARANTINED |

            Examples:
                | Temperature Category | Location  | Unit Number   | Product Description | Product Code | Expiration Date     | Temperature Category | Product Family             | Abo Rh | Licensed | Quarantines      |
                | FROZEN               | 123456789 | W777725015001 | CP2D PLS MI 120H    | E765000      | 2011-12-03T09:15:30 | FROZEN               | RP_FROZEN_WITHIN_120_HOURS | AP     | true     | OTHER, HEMOLYSIS |


        Scenario Outline: Create the imported inventory from Product Imported event with just mandatory information.

            When I received a Product Imported event for the following products:
                | Temperature Category   | Location   | Unit Number   | Product Code   | Product Family   | Expiration Date   | Product Description   | Abo Rh   |
                | <Temperature Category> | <Location> | <Unit Number> | <Product Code> | <Product Family> | <Expiration Date> | <Product Description> | <Abo Rh> |

            Then the inventory statuses should be updated as follows:
                | Status    | Temperature Category   |  | Location   | Unit Number   | Product Code   | Product Family   | Expiration Date   | Product Description   | Abo Rh   | Is Labeled | Is licensed   |
                | AVAILABLE | <Temperature Category> |  | <Location> | <Unit Number> | <Product Code> | <Product Family> | <Expiration Date> | <Product Description> | <Abo Rh> | true          | <Is Licensed> |
            And the properties should be added:
                | Unit Number   | Product Code   | Properties            |
                | <Unit Number> | <Product Code> | IMPORTED, QUARANTINED |

            Examples:
                | Temperature Category | Location  | Unit Number   | Product Description | Product Code | Expiration Date     | Temperature Category | Product Family             | Abo Rh | Is Licensed |
                | FROZEN               | 123456789 | W777725015002 | CP2D PLS MI 120H    | E765000      | 2011-12-03T09:15:30 | FROZEN               | RP_FROZEN_WITHIN_120_HOURS | AP     | false       |

