@api @R20-663 @AOA-244
Feature: Order Rejected Outbound Event
    As an Event Bridge Service,
    I want to be able to consume the Order Rejected event once a Biopro order operation is rejected
    so that I can publish it on the outbound events topic.

    Rule: I should be able to receive a Order Rejected event once a Biopro order operation is rejected.
    Rule: I should be able to produce a Order Rejected outbound event.
    Scenario: Consume Order Rejected event and Produce Order Rejected outbound event
        Given The Order Rejected event is triggered.
        When The Order Rejected event is received.
        Then The Order Rejected outbound event is produced.
        And The Order Rejected outbound event is posted in the outbound events topic.