@api @AOA-19
Feature: Receive internal transfer orders through a third party application

    Rule: I should be able to receive an internal transfer order request.
    Rule: I should be able to inform the label status of the products.
    Rule: I should be able to inform the shipment to location code for the internal transfer.
    Rule: I should be able to inform the quarantine status for internal transfers.
    @api @DIS-439
    Scenario Outline: Receive a Partner Internal Transfer order inbound request
        Given I have a Partner Internal Transfer order with Location Code as "<LocationCode>", Shipment Type as "<ShipmentType>", Ship To Location code as "<ShipToLocationCode>", Label Status as "<LabelStatus>" and Quarantine Products as "<QuarantineProducts>".
        When I send a request to the Partner Order Inbound Interface.
        Then The response status code should be <responseCode>.
        And The Order status should be "<status>".
        Examples:
            |LocationCode | ShipmentType      | ShipToLocationCode | LabelStatus | QuarantineProducts | responseCode | status   |
            |  123456789  | INTERNAL_TRANSFER |  234567891         | LABELED     |   false            |   202        | ACCEPTED |
            |  123456789  | INTERNAL_TRANSFER |  234567891         | UNLABELED   |   true             |   202        | ACCEPTED |

    Rule: The shipping customer code should not be required for internal transfers.
    Rule: The billing customer code should not be required for internal transfers.
    @api @DIS-439
    Scenario Outline: Validate Partner Internal Transfer order inbound request
        Given I have a Partner Internal Transfer order with Location Code as "<LocationCode>", Shipment Type as "<ShipmentType>", Ship To Location code as "<ShipToLocationCode>", Label Status as "<LabelStatus>" and Quarantine Products as "<QuarantineProducts>".
        When I send a request to the Partner Order Inbound Interface.
        Then The response status code should be <responseCode>.
        And The error message should be "<errorMessage>".
        Examples:
            |LocationCode | ShipmentType      | ShipToLocationCode | LabelStatus      | QuarantineProducts  | responseCode | errorMessage                                                                                              |
           |  123456789  | INTERNAL_TRANSFER |  234567891         | <null>           |   false             |   400        |  $.labelStatus: does not have a value in the enumeration [LABELED, UNLABELED]                              |
           |  123456789  | INTERNAL_TRANSFER |  234567891         | <empty_string>   |   true              |   400        |  $.labelStatus: does not have a value in the enumeration [LABELED, UNLABELED]                              |
           |  123456789  | INVALID           |  234567891         | UNLABELED        |   false             |   400        |  $.shipmentType: does not have a value in the enumeration [CUSTOMER, INTERNAL_TRANSFER]                    |
           |  123456789  | INTERNAL_TRANSFER |  <null>            | LABELED          |   true              |   400        |  $.shipToLocationCode: null found, string expected                                                         |
           |  123456789  | INTERNAL_TRANSFER |  234567891         | LABELED          |   <null_value>      |   400        |  $.quarantinedProducts: null found, but [boolean] is required                                              |
           |  <null>     | INTERNAL_TRANSFER |  234567891         | LABELED          |   false             |   400        |  $.locationCode: null found, string expected                                                               |









