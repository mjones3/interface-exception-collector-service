@api @R20-663 @AOA-244
Feature: Order Created Outbound Event
    As an Event Bridge Service,
    I want to be able to consume the Order Created event once the Biopro order object is created
    so that I can publish it on the outbound events topic.

    Rule: I should be able to receive a Order Created event once a Biopro order object is created.
    Rule: I should be able to produce a Order Created outbound event.
    Scenario: Consume Order Created event and Produce Order Created outbound event
        Given The Order Created event is triggered.
        When The Order Created event is received.
        Then The Order Created outbound event is produced.
        And The Order Created outbound event is posted in the outbound events topic.
