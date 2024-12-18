#    Manual Test of Shipping Outbound Interface
#    Date of testing: 12/16/2024
#    Documented by: Ruby Dizon, German Berros
#    Result: PASSED – Working as expected

@disabled @ui @api @AOA-100
Feature: After a shipment has been completed, shipping details information needs to be sent to third-party applications for Apheresis Plasma and Apheresis Red cells.

  As a system administrator,
  I want to ensure that shipment completion events for Apheresis Plasma and Apheresis Red Cells are sent to an external Kafka topic and confirmed via a response event,
  So that third-party applications receive the necessary shipping details for further processing and the system can log successful deliveries.

  @DIS-233
  Scenario Outline: Event successfully published to Kafka topic
    Given a shipment for “<Product Family>” has been completed,
    When the system generates the “Shipment Completed” event,
    Then the event should be successfully published to the Kafka topic with the correct payload.
    And the system should receive a response event confirming successful receipt.

    Examples:
        | Product Family       |
        | Apheresis Plasma     |
        | Apheresis Red Cells. |

#    NOTE: Should we differentiate in internal/external Kafka topic or just leave it as Kafka topic?
  @DIS-233
  Scenario: Handling invalid payloads in Kafka topic
    Given the “Shipment Completed” payload is invalid (e.g., missing required fields or incorrect format),
    When the system attempts to publish the event to the Kafka topic,
    Then the system should reject the payload,
    And log an error message indicating the validation failure.

#    NOTE: How do we manipulate the payload to test this scenario adding invalid data before it gets to the external.
  @DIS-233
  Scenario Outline: External service shutdown prevents event delivery
    Given a shipment for “<Product Family>” has been completed,
    And the external Kafka topic is inaccessible due to the external service being shut down,
    When the system attempts to forward the event,
    Then the system should detect the failure to deliver the event,
    And log an error indicating the external Kafka topic is unreachable.

    Examples:
        | Product Family       |
        | Apheresis Plasma     |
        | Apheresis Red Cells. |
