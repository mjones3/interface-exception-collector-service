Feature: Shipment Completed Outbound Event
    As a Partner,
    I want to be able to consume the shipment completed event once the Biopro shipment is completed
    so that I can be notified when the shipment is completed in the Biopro application.

    Rule: I should be able to receive a shipment completed outbound event once a Biopro shipment is completed.
    Scenario: Application is up and running
        Given The shipment completed event is triggered.
        When The shipment completed event is received.
        Then The shipment completed outbound event can be consumed.
