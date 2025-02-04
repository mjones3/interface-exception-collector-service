@api @AOA-152
Feature: Partner Order Inbound Interface
    As a partner blood center system,
    I want to send the order request,
    so that my order can be processed by the BioPro system.

    Rule: BioPro should be able to accept and create orders sent by the Order Provider for all priorities (STAT, ASAP, ROUTINE, SCHEDULED, DATE_TIME) that does not have a Desired Shipping Date indicated.
    @DIS-91 @DIS-252 @bug @DIS-285
    Scenario Outline: Receive a Partner order inbound request
        Given I have a Partner order "<JsonPayloadName>".
        When I send a request to the Partner Order Inbound Interface.
        Then The response status should be <responseCode>.
        And The Order status should be "<status>".
        Examples:
            | JsonPayloadName                                                                  | responseCode | status  |
            | inbound-test-files/order-inbound-scenario-happy-path.json                        | 201          | CREATED |
            | inbound-test-files/order-inbound-scenario-aph-rbc-products-path.json             | 201          | CREATED |
            | inbound-test-files/order-inbound-scenario-rbc-path.json                          | 201          | CREATED |
            | inbound-test-files/order-inbound-scenario-whole-blood-path.json                  | 201          | CREATED |
            | inbound-test-files/order-inbound-scenario-whole-blood-leukoreduced-path.json     | 201          | CREATED |
            | inbound-test-files/order-inbound-scenario-dis-285-desire_ship_date_null.json     | 201          | CREATED |
            | inbound-test-files/order-inbound-scenario-dis-285-desire_ship_date_no_field.json | 201          | CREATED |


    @DIS-91
    Scenario Outline: Validate Partner order inbound request
        Given I have a Partner order "<JsonPayloadName>".
        When I send a request to the Partner Order Inbound Interface.
        Then The response status should be <responseCode>.
        And The error message should be "<errorMessage>".
        Examples:
            | JsonPayloadName                                     | responseCode | errorMessage                                                                                         |
            | inbound-test-files/order-inbound-scenario-0001.json | 400          | $.orderStatus: null found, string expected                                                           |
            | inbound-test-files/order-inbound-scenario-0002.json | 400          | $.deliveryType: does not have a value in the enumeration [DATE_TIME, SCHEDULED, STAT, ROUTINE, ASAP] |
            | inbound-test-files/order-inbound-scenario-0003.json | 400          | $.orderItems[0].quantity: string found, integer expected                                             |
            | inbound-test-files/order-inbound-scenario-0004.json | 400          | $.desiredShippingDate: does not match the regex pattern ^(\\d{4}-\\d{2}-\\d{2})$                     |
