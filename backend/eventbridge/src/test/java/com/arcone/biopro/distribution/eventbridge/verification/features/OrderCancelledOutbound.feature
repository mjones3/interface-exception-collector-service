@api @R20-663 @AOA-244
Feature: Order Cancelled Outbound Event
    As an Event Bridge Service,
    I want to be able to consume the Order Cancelled event once the Biopro order object is cancelled
    so that I can publish it on the outbound events topic.

    Rule: I should be able to receive a Order Cancelled event once a Biopro order object is cancelled.
    Rule: I should be able to produce a Order Cancelled outbound event.
    Scenario: Consume Order Cancelled event and Produce Order Cancelled outbound event
        Given The Order Cancelled event is triggered.
        When The Order Cancelled event is received.
        Then The Order Cancelled outbound event is produced.
        And The Order Cancelled outbound event is posted in the outbound events topic.