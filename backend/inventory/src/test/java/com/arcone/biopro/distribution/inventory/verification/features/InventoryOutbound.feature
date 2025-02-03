@api @R20-333
Feature: Inventory Outbound Interface
    Rule: As an Inventory Service, I want to be able to produce the inventory updated event once the status is changed so that I can publish it to the inventory updated topic.
        Scenario Outline: Produce an inventory updated event when specific events occur
            When I make a <Status Update Type> update on an inventory record
            Then the inventory updated event should be produced with the "<Update Type Property>" value in the payload

            Examples:
                | Status Update Type | Update Type Property |
                | Label applied      | CREATED              |
                | Quarantine applied | QUARANTINE_APPLIED   |
                | Quarantine removed | QUARANTINE_REMOVED   |
                | Product stored     | STORED               |
                | Product shipped    | SHIPPED              |
                | Product discarded  | DISCARDED            |

