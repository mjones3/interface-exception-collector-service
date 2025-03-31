# Feature Unit Number reference: W036825005000
@api
Feature: Kafka listeners

    Scenario Outline: Application is listening and creating/updating inventory from kafka events
        Given I have the following inventories:
            | Unit Number   | Product Code | Status    |
            | <Unit Number> | E0869VA0     | AVAILABLE |
        When I receive an event "<Event>" event for unit number "<Unit Number>"
        Then The inventory status is "<Status>"
        And the expected fields for "<Event>" are stored

        @LAB-80 @AOA-75
        Examples:
            | Event             | Status    | Unit Number   |
            | Product Discarded | DISCARDED | W036825005001 |

        @LAB-79 @AOA-75 @LAB-254
        Examples:
            | Event               | Status    | Unit Number   |
            | Quarantine Removed  | AVAILABLE | W036825005002 |
            | Product Recovered   | AVAILABLE | W036825005003 |
            | Product Quarantined | AVAILABLE | W036825005004 |
            | Quarantine Updated  | AVAILABLE | W036825005005 |

    @LAB-96 @AOA-75 @LAB-116
    Scenario Outline: Application is listening storage events from kafka
        Given I am listening the "<Event>" event for "<Unit Number>"
        When I receive a "<Event>" message with unit number "<Unit Number>", product code "<Product Code>" and location "<Location>"
        Then For unit number "<Unit Number>" and product code "<Product Code>" the device stored is "<Device Storage>" and the storage location is "<Storage Location>"

        Examples:
            | Event          | Unit Number   | Product Code | Location | Device Storage | Storage Location        |
            | Product Stored | W036825005006 | E0869VA0     | Miami    | Freezer001     | Bin001,Shelf002,Tray001 |
