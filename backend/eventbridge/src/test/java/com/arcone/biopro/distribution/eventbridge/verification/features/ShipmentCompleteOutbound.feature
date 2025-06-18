@api @AOA-100
Feature: Shipment Completed Outbound Event
    As an Event Bridge Service,
    I want to be able to consume the shipment completed event once the Biopro shipment is completed
    so that I can publish it on the outbound events topic.

    Rule: I should be able to receive a shipment completed outbound event once a Biopro shipment is completed.
    Rule: I should be able to produce a shipment completed outbound event.
    @api @DIS-189
    Scenario: Consume Shipment Completed and Produce Shipment Completed Outbound Event
        Given The shipment completed event is triggered withe the payload as "shipment-completed-event-automation.json".
        When The shipment completed event is received.
        Then The shipment completed outbound event is produced
        And The shipment completed outbound event is posted in the outbound events topic.

    Rule: The system should outbound events imported shipped products.
    @api @DIS-427
    Scenario Outline: Consume Shipment Completed and Produce Shipment Completed Outbound Event - Imported Products
        Given The shipment completed event is triggered withe the payload as "<payloadFileName>".
        When The shipment completed event is received.
        Then The shipment completed outbound event is produced
        And The shipment completed outbound event is posted in the outbound events topic.
        Examples:
        |payloadFileName|
        | shipment-completed-event-automation-imported-products.json                      |
        | shipment-completed-event-automation-imported-products-collection-date-null.json |
