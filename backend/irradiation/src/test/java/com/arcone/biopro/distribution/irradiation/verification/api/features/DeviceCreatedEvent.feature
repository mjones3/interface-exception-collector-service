@api
Feature: Device Created Event
    Rule: As an irradiation service I want to listen to device created event that are irradiator types so that I can create the device
        Scenario Outline: Create the device after receiving a Device Created event.
            When I received a Device Created event with the following:
                | Id   | Location   | Device Category   | Status   |
                | <Id> | <Location> | <Device Category> | <Status> |

            Then the device should be created as follows:
                | Device ID   | Location   | Status   |
                | <Device ID> | <Location> | <Status> |

            Examples:
                | Id            | Location  | Device Category | Status   | Device ID     |
                | W777725001001 | 123456789 | IRRADIATOR      | ACTIVE   | W777725001001 |
                | W777725001002 | 123456789 | IRRADIATOR      | INACTIVE | W777725001002 |

        Scenario Outline: Skip device creation for non-irradiator types.
            When I received a Device Created event with the following:
                | Id   | Location   | Device Category   | Status   |
                | <Id> | <Location> | <Device Category> | <Status> |

            Then the device should not be created and a message should be logged

            Examples:
                | Id            | Location  | Device Category | Status |
                | W777725001003 | 123456789 | PRINTER         | ACTIVE |
