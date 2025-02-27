@api @AOA-152
Feature: Modify order through the third-party application

    Rule: The following order elements can be modified via third-party application
    Delivery Type, Will Call, Products (add/remove/update), Product family, Quantity
    Blood Type, Comments, Ship from Location, Product Category, Shipping Method, Order Comments.
    @DIS-262
    Scenario Outline: Receive a Partner modify order request
        Given I have a Partner modify order payload "<JsonPayloadName>".
        When I send a request to the Partner modify order interface to modify the order "<External ID>".
        Then The response status code should be <responseCode>.
        And The response status should be "<status>".
        Examples:
            | External ID | JsonPayloadName                                                        | responseCode | status   |
            | EXT123      | inbound-test-files/modify-order-inbound-scenario-happy-path.json       | 202          | ACCEPTED |
            | EXT124      | inbound-test-files/modify-order-inbound-scenario-no-employee-code.json | 202          | ACCEPTED |
            | EXT125      | inbound-test-files/modify-order-inbound-scenario-no-modify-date.json   | 202          | ACCEPTED |


    Rule: The modified order request must be rejected if the schema validations fail.
    @DIS-262
    Scenario Outline: Validate Partner modify order inbound request
        Given I have a Partner modify order payload "<JsonPayloadName>".
        When I send a request to the Partner modify order interface to modify the order "<External ID>".
        Then The response status code should be <responseCode>.
        And The error message should be "<errorMessage>".
        Examples:
            | External ID | JsonPayloadName                                            | responseCode | errorMessage                                                                                         |
            | EXT123      | inbound-test-files/modify-order-inbound-scenario-0002.json | 400          | $.deliveryType: does not have a value in the enumeration [DATE_TIME, SCHEDULED, STAT, ROUTINE, ASAP] |
            | EXT123      | inbound-test-files/modify-order-inbound-scenario-0003.json | 400          | $.orderItems[0].quantity: string found, integer expected                                             |
            | EXT123      | inbound-test-files/modify-order-inbound-scenario-0004.json | 400          | $.desiredShippingDate: does not match the regex pattern ^(\\d{4}-\\d{2}-\\d{2})$                     |
            | EXT123      | inbound-test-files/modify-order-inbound-scenario-0005.json | 400          | $.modifyDate: does not match the regex pattern ^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})$         |
            | EXT123      | inbound-test-files/modify-order-inbound-scenario-0006.json | 400          | $.locationCode: is missing but it is required                                                        |
