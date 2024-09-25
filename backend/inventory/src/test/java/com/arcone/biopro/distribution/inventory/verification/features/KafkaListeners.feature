@api
Feature: Kafka listeners

    Scenario Outline: Application is listening and creating/updating inventory from kafka events
        Given I am listening the "<Event>" event
        When I receive an event "<Event>" event
        Then The inventory status is "<Status>"
        And the expected fields for "<Event>" are stored

        Examples:
            | Event               | Status      |
            | Label Applied       | AVAILABLE   |
            | Shipment Completed  | SHIPPED     |
            | Product Discarded   | DISCARDED   |
            | Product Quarantined | QUARANTINED |
            | Quarantine Updated  | QUARANTINED |
            | Quarantine Removed  | AVAILABLE   |
            | Product Recovered   | AVAILABLE   |


    Scenario Outline: Application is listening storage events from kafka
        Given I am listening the "<Event>" event for "<Unit Number>"
        When I receive a "<Event>" message with unit number "<Unit Number>", product code "<Product Code>" and location "<Location>"
        Then For unit number "<Unit Number>" and product code "<Product Code>" the device stored is "<Device Storage>" and the storage location is "<Storage Location>"

        Examples:
            | Event          | Unit Number   | Product Code | Location | Device Storage | Storage Location        |
            | Product Stored | W123452622168 | E0869VA0     | Miami    | Freezer001     | Bin001,Shelf002,Tray001 |
