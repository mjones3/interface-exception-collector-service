@api @R20-333 @AOA-25
Feature: Inventory Outbound Interface
    Rule: As an inventory service I want to create an inventory update event every time an inventory is created/updated
        Scenario Outline: Produce an inventory updated event every time an inventory is create/update
            Given I have the following inventories:
                | Unit Number   | Product Code   |
                | <Unit Number> | <Product Code> |

            When When I received a "<Event>" event for the following products
                | Unit Number   | Product Code   | Is licensed   | Reason   | Shipment type   |
                | <Unit Number> | <Product Code> | <Is licensed> | <Reason> | <Shipment type> |

            Then the inventory updated event should be produced with the "<Update Type>" value in the payload for the following units:
                | Unit Number   | Final Product Code   |
                | <Unit Number> | <Final Product Code> |

            Examples:
                | Event              | Unit Number   | Product Code | Final Product Code | Is licensed | Shipment type | Reason            | Update Type        |
                | Label Applied      | W036824111111 | E162400      | E1624V00           | true        |               |                   | LABEL_APPLIED      |
                | Apply Quarantine   | W036824111112 | E1624V00     | E1624V00           |             |               | Quarantine Reason | QUARANTINE_APPLIED |
                | Remove Quarantine  | W036824311113 | E1624V00     | E1624V00           |             |               | Quarantine Reason | QUARANTINE_REMOVED |
                | Shipment Completed | W036824311114 | E1624V00     | E1624V00           |             | CUSTOMER      |                   | SHIPPED            |
                | Discard Created    | W036824311115 | E1624V00     | E1624V00           |             |               | Discard Reason    | DISCARDED          |
                | Product Stored     | W036824311116 | E1624V00     | E1624V00           |             |               |                   | STORED             |
