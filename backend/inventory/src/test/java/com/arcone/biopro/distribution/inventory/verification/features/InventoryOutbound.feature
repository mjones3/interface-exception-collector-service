@api @R20-333
Feature: Inventory Outbound Interface
    Rule: As an Inventory Service, I want to be able to produce the inventory updated event once the status is changed so that I can publish it to the inventory updated topic.
        Scenario: Produce an inventory updated event for label applied event
            Given I have the following inventories:
                | Unit Number   | Product Code   |
                | W036824311111 | E162400 |
            When I received a Label Applied event for the following products:
                | Unit Number   | Product Code         | Is licensed   |
                | <Unit Number> | <Final Product Code> | <Is licensed> |
            Then the inventory updated event should be produced with the "LABEL_APPLIED" value in the payload with unit "W036824311111" and "E1624V00"


        Scenario: Produce an inventory updated event for quarantine applied and removed
            Given I have the following inventories:
                | Unit Number   | Product Code   |
                | W036824311111 | E162400 |
            When I received an Apply Quarantine event for unit "W036824111111" and product "E1624V00" with reason "Quarantine Reason" and id "1"
            Then the inventory updated event should be produced with the "QUARANTINE_APPLIED" value in the payload with unit "W036824311111" and "E1624V00"
            When I received a Remove Quarantine event for unit "W036824111111" and product "E1624V00" with reason "Quarantine Reason" and id "2"
            Then the inventory updated event should be produced with the "QUARANTINE_REMOVED" value in the payload with unit "W036824311111" and "E1624V00"





        Scenario: Produce an inventory updated event for shipped products
            Given I have the following inventories:
                | Unit Number   | Product Code | Status    |
                | W036824111111 | E1624V00     | AVAILABLE |
                | W036824111112 | E1624V00     | AVAILABLE |
                | W036824111113 | E1624V00     | AVAILABLE |
                | W036824111114 | E1624V00     | AVAILABLE |
            When I received a Shipment Completed event with shipment type "CUSTOMER" for the following units:
                | Unit Number   | Product Code |
                | W036824111111 | E1624V00     |
                | W036824111112 | E1624V00     |
                | W036824111113 | E1624V00     |
            Then the inventory updated event should be produced with the "QUARANTINE_REMOVED" value in the payload for the following units:
                | Unit Number   | Product Code |
                | W036824111111 | E1624V00     |
                | W036824111112 | E1624V00     |
                | W036824111113 | E1624V00     |

        Scenario Outline: Update the inventory status to DISCARDED after receiving a Discard Created event with comments
            Given I have the following inventories:
                | Unit Number   | Product Code   | Status    |
                | <Unit Number> | <Product Code> | AVAILABLE |

            When I received a Discard Created event for the following products:
                | Unit Number   | Product Code   | Reason   | Comment Length   |
                | <Unit Number> | <Product Code> | <Reason> | <Comment Length> |

            Then the inventory updated event should be produced with the "PRODUCT_DISCARDED" value in the payload for the following units:
                | Unit Number   | Product Code |
                | W036824111111 | E1624V00     |
                | W036824111112 | E1624V00     |
                | W036824111113 | E1624V00     |

            Examples:
                | Unit Number   | Product Code | Reason  | Comment Length |
                | W036824211111 | E765000      | OTHER   | 25             |
                | W036824211112 | E453200      | EXPIRED | 2000           |


            #TODO: Product stored
