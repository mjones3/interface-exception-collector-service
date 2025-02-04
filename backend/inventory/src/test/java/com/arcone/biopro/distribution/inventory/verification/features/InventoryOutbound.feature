@api @R20-333
Feature: Inventory Outbound Interface

    Background:
        Given I have the following inventories:
            | Unit Number   | Product Code |
            | W036824311111 | E162400      |
            | W036824311112 | E162400      |
            | W036824311113 | E162400      |
            | W036824311114 | E162400      |
            | W036824311115 | E162400      |

    Rule: As an Inventory Service, I want to be able to produce the inventory updated event once the status is changed so that I can publish it to the inventory updated topic.
        Scenario: Produce an inventory updated event for label applied event
            When I received a Label Applied event for the following products:
                | Unit Number   | Product Code | Is licensed |
                | W036824311111 | E162400      | true        |
            Then the inventory updated event should be produced with the "LABEL_APPLIED" value in the payload for the following units:
                | Unit Number   | Product Code |
                | W036824111111 | E1624V00     |

        Scenario: Produce an inventory updated event for quarantine applied and removed
            When I received an Apply Quarantine event for unit "W036824111112" and product "E1624V00" with reason "Quarantine Reason" and id "1"
            Then the inventory updated event should be produced with the "QUARANTINE_APPLIED" value in the payload for the following units:
                | Unit Number   | Product Code |
                | W036824111112 | E1624V00     |
            When I received a Remove Quarantine event for unit "W036824111112" and product "E1624V00" with reason "Quarantine Reason" and id "2"
            Then the inventory updated event should be produced with the "QUARANTINE_REMOVED" value in the payload for the following units:
                | Unit Number   | Product Code |
                | W036824111112 | E1624V00     |

        Scenario: Produce an inventory updated event for shipped products
            When I received a Shipment Completed event with shipment type "CUSTOMER" for the following units:
                | Unit Number   | Product Code |
                | W036824111113 | E1624V00     |
            Then the inventory updated event should be produced with the "SHIPPED" value in the payload for the following units:
                | Unit Number   | Product Code |
                | W036824111113 | E1624V00     |

        Scenario: Produce an inventory updated event for discarded products
            When I received a Discard Created event for the following products:
                | Unit Number   | Product Code | Reason         | Comment Length |
                | W036824111114 | E1624V00     | Discard Reason | 25             |

            Then the inventory updated event should be produced with the "DISCARDED" value in the payload for the following units:
                | Unit Number   | Product Code |
                | W036824111114 | E1624V00     |

        Scenario: Produce an inventory updated event for stored products
            When I received a Product Stored event for the following products:
                | Unit Number   | Product Code |
                | W036824111115 | E1624V00     |

            Then the inventory updated event should be produced with the "STORED" value in the payload for the following units:
                | Unit Number   | Product Code |
                | W036824111115 | E1624V00     |
