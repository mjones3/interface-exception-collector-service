@api @R20-526 @AOA-25
Feature: Inventory Updated Outbound Event
    As an Event Bridge Service,
    I want to be able to consume the Inventory Updated event once the Biopro inventory object is created or updated
    so that I can publish it on the outbound events topic.

    Rule: I should be able to receive a Inventory Updated event once a Biopro inventory object is created or updated.
    Rule: I should be able to produce a Inventory Updated outbound event.
    Scenario: Consume Inventory Updated event and Produce Inventory Updated outbound event
        Given The Inventory Updated event is triggered.
        When The Inventory Updated event is received.
        Then The Inventory Updated outbound event is produced.
        And The Inventory Updated outbound event is posted in the outbound events topic.
