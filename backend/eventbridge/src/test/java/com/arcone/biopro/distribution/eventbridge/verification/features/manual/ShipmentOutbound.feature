#Date of testing: 12/18/2024
#Manually tested and documented by: Ruby Dizon, German Berros
#Supported by: Allan Morelli Braga, Jose Rosello
#Result: PASSED – Working as expected

@disabled @ui @api @AOA-100
Feature: After a shipment has been completed, shipping details information needs to be sent to third-party applications for Apheresis Plasma and Apheresis Red cells.

    As a system administrator,
    I want to ensure that shipment completed events for Apheresis Plasma and Apheresis Red Cells are sent to a partner cluster Kafka topic and confirmed via a response event,
    So that third-party applications receive the shipping details for further processing and the system can log successful deliveries.

    @DIS-233
    Scenario Outline: Event successfully published to Kafka topic
        Given a shipment for “<Product Family>” has been completed,
        When the system generates the “Shipment Completed” event,
        Then the event should be successfully published to the partner cluster Kafka topic with the correct payload,
        And the system should receive a response event confirming successful receipt.
        Examples:
            | Product Family      |
            | Apheresis Plasma    |
            | Apheresis Red Cells |


    @DIS-233
    Scenario Outline: Event Bridge service shutdown prevents event delivery
        Given the event bridge service is unavailable,
        When a shipment for “<Product Family>” is completed,
        Then the Shipment Completed event should be queued up
        When the event bridge service is available,
        Then the Shipment Outbound event should be posted in the external Kafka topic.
        Examples:
            | Product Family      |
            | Apheresis Plasma    |
            | Apheresis Red Cells |
