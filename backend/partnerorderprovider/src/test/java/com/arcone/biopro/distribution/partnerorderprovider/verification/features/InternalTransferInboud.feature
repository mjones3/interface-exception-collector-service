@api @AOA-19
Feature: Receive internal transfer orders through a third party application

    Rule: I should be able to receive an internal transfer order request.
    Rule: I should be able to inform the label status of the products.
    Rule: I should be able to inform the quarantine status for products in the internal transfer orders.
    Rule: The billing customer code should not be required for internal transfers.
    @api @DIS-439
    Scenario Outline: Receive a Partner Internal Transfer order inbound request
        Given I have a Partner Internal Transfer order with Location Code as "<LocationCode>", Shipment Type as "<ShipmentType>", Ship To Location code as "<shipping Customer Code>", Label Status as "<LabelStatus>", Quarantine Products as "<QuarantineProducts>" and Billing Customer Code as "<BillingCustomerCode>".
        When I send a request to the Partner Order Inbound Interface.
        Then The response status code should be <responseCode>.
        And The Order status should be "<status>".
        Examples:
            |LocationCode | ShipmentType      | shipping Customer Code | LabelStatus | QuarantineProducts| BillingCustomerCode  | responseCode | status   |
            |  123456789  | INTERNAL_TRANSFER |  234567891             | LABELED     |   false           | <null>               |   202        | ACCEPTED |
            |  123456789  | INTERNAL_TRANSFER |  234567891             | UNLABELED   |   true            | 234567891            |   202        | ACCEPTED |


    @api @DIS-439
    Scenario Outline: Validate Partner Internal Transfer order inbound request
        Given I have a Partner Internal Transfer order with Location Code as "<LocationCode>", Shipment Type as "<ShipmentType>", Ship To Location code as "<shipping Customer Code>", Label Status as "<LabelStatus>", Quarantine Products as "<QuarantineProducts>" and Billing Customer Code as "<BillingCustomerCode>".
        When I send a request to the Partner Order Inbound Interface.
        Then The response status code should be <responseCode>.
        And The error message should be "<errorMessage>".
        Examples:
            |LocationCode | ShipmentType      | shipping Customer Code | LabelStatus      | QuarantineProducts| BillingCustomerCode | responseCode | errorMessage                                                                                              |
           |  123456789  | INTERNAL_TRANSFER |  234567891              | <null>           |   false           | <null>              |   400        |  $.labelStatus: does not have a value in the enumeration [LABELED, UNLABELED]                              |
           |  123456789  | INTERNAL_TRANSFER |  234567891              | <empty_string>   |   true            | <null>              |   400        |  $.labelStatus: does not have a value in the enumeration [LABELED, UNLABELED]                              |
           |  123456789  | INVALID           |  234567891              | UNLABELED        |   false           | <null>              |   400        |  $.shipmentType: does not have a value in the enumeration [CUSTOMER, INTERNAL_TRANSFER]                    |
           |  123456789  | INTERNAL_TRANSFER |  <null>                 | LABELED          |   true            | 234567891           |   400        |  $.shippingCustomerCode: null found, but [string] is required                                                        |
           |  123456789  | INTERNAL_TRANSFER |  234567891              | LABELED          |   <null_value>    | 234567891           |   400        |  $.quarantinedProducts: null found, but [boolean] is required                                              |
           |  <null>     | INTERNAL_TRANSFER |  234567891              | LABELED          |   false           | 234567891           |   400        |  $.locationCode: null found, string expected                                                               |









