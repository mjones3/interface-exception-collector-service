# Feature Unit Number reference: W777725001000
@api @LAB-256 @AOA-152 @LAB475 @AOA-83
Feature: CheckIn Completed Event
    Rule: As an inventory service I want to listen to check-in completed event so that I can create the inventory
        Scenario Outline: Create the inventory as AVAILABLE and UNLABELED after receiving a Product Created event.
            When I received a CheckIn Completed event for the following products:
                | Unit Number   | Product Code   | Collection Location   | Collection TimeZone   |
                | <Unit Number> | <Product Code> | <Collection Location> | <Collection TimeZone> |

            Then the inventory statuses should be updated as follows:
                | Unit Number   | Product Code   | Status    | Is Labeled | Location              | Collection Location   | Collection TimeZone   |
                | <Unit Number> | <Product Code> | AVAILABLE | false      | <Collection Location> | <Collection Location> | <Collection TimeZone> |

            Examples:
                | Unit Number   | Product Code | Collection Location | Collection TimeZone |
                | W777725001001 | WHOLEBLOOD   | LOCATION_A          | America/New_York    |
                | W777725001002 | PLASAPHP     | LOCATION_B          | America/Chicago     |
                | W777725001003 | RBCAPH       | LOCATION_C          | America/Los_Angeles |
