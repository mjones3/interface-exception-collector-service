# Feature Unit Number reference: W777725004000
@api @AOA-25
Feature: Inventory Outbound Interface
    Rule: As an inventory service I want to create an inventory update event every time an inventory is created/updated
        Scenario Outline: Produce an inventory updated event every time an inventory is create/update
            Given I have the following inventories:
                | Unit Number   | Product Code   | Location   |
                | <Unit Number> | <Product Code> | <Location> |

            When I received a "<Event>" event for the following products:
                | Unit Number   | Product Code         | Is licensed   | Reason   | Reason Id   | Shipment type   | Device Storage   | Storage Location   | Location   | Expiration Date   | Expiration Time | Modification Location | Modification Date | Volume | Weight | Product Description | Parent Product Code | Product Family               | Abo Rh   |
                | <Unit Number> | <Final Product Code> | <Is licensed> | <Reason> | <Reason Id> | <Shipment type> | <Device Storage> | <Storage Location> | <Location> | <Expiration Date> | 23:59           | 1FS                   | 02/15/2025        | 250    | 234    | APH LR RBC FRZ C1   | <Product Code>      | RED_BLOOD_CELLS_LEUKOREDUCED | <Abo Rh> |

            Then the inventory updated event should be produced with the "<Update Type>" value in the payload for the following units:
                | Unit Number   | Final Product Code   | Status Included   |
                | <Unit Number> | <Final Product Code> | <Status Included> |

            @R20-333
            Examples:
                | Event              | Unit Number   | Product Code | Final Product Code | Is licensed | Shipment type | Reason            | Reason Id | Device Storage | Storage Location        | Location   | Update Type        | Expiration Date |
                | Label Applied      | W777725004001 | E162400      | E1624V00           | true        |               |                   |           |                |                         |            | LABEL_APPLIED      | 02/15/2026      |
                | Apply Quarantine   | W777725004002 | E1624V00     | E1624V00           |             |               | Quarantine Reason | 1         |                |                         |            | QUARANTINE_APPLIED | 02/15/2026      |
                | Remove Quarantine  | W777725004003 | E1624V00     | E1624V00           |             |               | Quarantine Reason | 2         |                |                         |            | QUARANTINE_REMOVED | 02/15/2026      |
                | Shipment Completed | W777725004004 | E1624V00     | E1624V00           |             | CUSTOMER      |                   |           |                |                         |            | SHIPPED            | 02/15/2026      |
                | Discard Created    | W777725004005 | E1624V00     | E1624V00           |             |               | Discard Reason    | 1         |                |                         |            | DISCARDED          | 02/15/2026      |
                | Product Stored     | W777725004006 | E1624V00     | E1624V00           |             |               |                   |           | Freezer001     | Bin001,Shelf002,Tray001 | location_1 | STORED             | 02/15/2026      |

            @LAB-408
            Examples:
                | Event                           | Unit Number   | Product Code | Final Product Code | Is licensed | Shipment type | Reason | Reason Id | Device Storage | Storage Location        | Location   | Update Type |Expiration Date  |
                | Recovered Plasma Carton Packed  | W777725004007 | E1624V00     | E1624V00           |             |               |        |           | Freezer001     | Bin001,Shelf002,Tray001 | location_1 | PACKED      |02/15/2026  |
                | Recovered Plasma Carton Removed | W777725004008 | E1624V00     | E1624V00           |             |               |        |           | Freezer001     | Bin001,Shelf002,Tray001 | location_1 | UNPACKED    | 02/15/2026 |

            @LAB-390
            Examples:
                | Event                          | Unit Number   | Product Code | Final Product Code | Is licensed | Shipment type | Reason | Reason Id | Device Storage | Storage Location        | Location   | Update Type |Expiration Date  |
                | Recovered Plasma Carton Closed | W777725004009 | E1624V00     | E1624V00           |             |               |        |           | Freezer001     | Bin001,Shelf002,Tray001 | location_1 | SHIPPED     |02/15/2026  |

            @LAB-503
            Examples:
                | Event            | Unit Number  | Product Code | Final Product Code | Is licensed | Abo Rh | Reason | Reason Id | Device Storage | Storage Location | Location | Update Type   | Expiration Date     |
                | Product Imported | W777725004010 | E1624V00     | E1624V00           | true        | OP     |        |           |                |                  |          | LABEL_APPLIED | 2011-12-03T09:15:30 |

            @R20-804
            Examples:
                | Event             | Unit Number   | Product Code | Final Product Code | Is licensed | Shipment type | Reason            | Reason Id | Device Storage | Storage Location | Location | Expiration Date | Status Included      |
                | Label Applied     | W777725004051 | E162400      | E1624V00           | true        |               |                   |           |                |                  |          | 02/15/2026      | LABELED              |
                | Apply Quarantine  | W777725004052 | E1624V00     | E1624V00           |             |               | Quarantine Reason | 1         |                |                  |          | 02/15/2026      | LABELED, QUARANTINED |
                | Remove Quarantine | W777725004053 | E1624V00     | E1624V00           |             |               | Quarantine Reason | 2         |                |                  |          | 02/15/2026      | LABELED              |



