Feature: Shipment Completed Outbound Event
    As an Event Bridge Service,
    I want to be able to consume the shipment completed event once the Biopro shipment is completed
    so that I can publish it on the outbound events topic.

    Rule: I should be able to receive a shipment completed outbound event once a Biopro shipment is completed.
    Rule: I should be able to produce a shipment completed outbound event.
    @API @DIS-189
    Scenario: Consume Shipment Completed and Produce Shipment Completed Outbound Event
        Given The shipment completed event is triggered.
        When The shipment completed event is received.
        Then The shipment completed outbound event is produced
        And The shipment completed outbound event is posted in the outbound events topic.
