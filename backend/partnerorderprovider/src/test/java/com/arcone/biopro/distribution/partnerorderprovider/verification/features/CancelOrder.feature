@api @AOA-152
Feature: As a BioPro system,
    I want to receive a canceled order through the third-party application,
    so that I can process the canceled order request in the BioPro application.

    Rule: The interface message must include the order number, cancellation reason, date, and time.
    Rule: The interface message should include the employee who canceled the order if it is available.
    @DIS-263
    Scenario Outline: Receive a Partner cancel order request
        Given I have a Partner cancel order request payload "<JsonPayloadName>".
        When I send a request to the Partner Cancel Order Inbound Interface.
        Then The response code should be <responseCode>.
        And The response status should be "<status>".
        Examples:
            | JsonPayloadName                                                     | responseCode | status   |
            | inbound-test-files/cancel-order-scenario-happy-path.json            | 202          | ACCEPTED |
            | inbound-test-files/cancel-order-scenario-no-employee-code-path.json | 202          | ACCEPTED |

    Rule: The canceled order request must be rejected if the schema validations fail.
    @DIS-263
    Scenario Outline: Validate Partner cancel order inbound request
        Given I have a Partner cancel order request payload "<JsonPayloadName>".
        When I send a request to the Partner Cancel Order Inbound Interface.
        Then The response code should be <responseCode>.
        And The error message should be "<errorMessage>".
        Examples:
            | JsonPayloadName                                   | responseCode | errorMessage                                                            |
            | inbound-test-files/cancel-order-scenario-001.json | 400          | $.externalId: null found, string expected                               |
            | inbound-test-files/cancel-order-scenario-002.json | 400          | $.cancelDate: null found, string expected                               |
            | inbound-test-files/cancel-order-scenario-003.json | 400          | $.cancelReason: null found, string expected                             |
            | inbound-test-files/cancel-order-scenario-004.json | 400          | $.cancelDate: does not match the regex pattern ^(\\d{4}-\\d{2}-\\d{2})$ |

