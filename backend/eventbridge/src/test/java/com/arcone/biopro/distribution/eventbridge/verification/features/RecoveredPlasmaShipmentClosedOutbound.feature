@api @AOA-89
Feature: Recovered Plasma Shipping Outbound interface

    Rule: I should be able to receive a Recovered Plasma shipment completed outbound event once a Biopro shipment is closed.
    Rule: I should be able to produce a Recovered Plasma shipment completed outbound event.
    @api @DIS-348
    Scenario: Consume Recovered Plasma Shipment Closed and Produce Recovered Plasma Shipment Closed Outbound Event
        Given The Recovered Plasma shipment closed event is triggered.
        When The Recovered Plasma shipment closed event is received.
        Then The Recovered Plasma shipment closed outbound event is produced.
        And The Recovered Plasma shipment closed outbound event is posted in the outbound events topic.
