@api @R20-663 @AOA-244
Feature: Order Modified Outbound Event
    As an Event Bridge Service,
    I want to be able to consume the Order Modified event once the Biopro order object is modified
    so that I can publish it on the outbound events topic.

    Rule: I should be able to receive a Order Modified event once a Biopro order object is modified.
    Rule: I should be able to produce a Order Modified outbound event.
    Scenario: Consume Order Modified event and Produce Order Modified outbound event
        Given The Order Modified event is triggered.
        When The Order Modified event is received.
        Then The Order Modified outbound event is produced.
        And The Order Modified outbound event is posted in the outbound events topic.