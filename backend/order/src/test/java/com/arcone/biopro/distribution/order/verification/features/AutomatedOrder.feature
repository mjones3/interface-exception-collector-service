@api @AOA-152
Feature: Validate order

    Background:
        Given I cleaned up from the database the orders with external ID "114117922233599,114117922233500,114117922233511,114117922233512,114117922233513,114117922233514,114117922233515,114117922233516,114117922233517,114117922233518,114117922233519,114117922233520,114117922233521,114117922233522,114117922233523,114117922233524,114117922233525,114117922233526,114117922233527".

    @DIS-161 @DIS-92 @DIS-253
    Scenario Outline: Creating a BioPro order from a valid order inbound request
        Given I have received an order inbound request with externalId "<External ID>" and content "<JsonPayloadName>".
        When The system process the order request.
        Then A biopro Order will be available in the Distribution local data store.
        Examples:
            | External ID     | JsonPayloadName                                              |
            | 114117922233500 | order-inbound-scenario-1-happy-path.json                     |
            | 114117922233521 | order-inbound-scenario-aph-rbc-product.json                  |
            | 114117922233522 | order-inbound-scenario-whole-blood-product.json              |
            | 114117922233523 | order-inbound-scenario-whole-blood-leukoreduced-product.json |
            | 114117922233524 | order-inbound-scenario-red-blood-cells-product.json          |

    @DIS-92
    Scenario Outline: Creating a BioPro order from an invalid order inbound request
        Given I have received an order inbound request with externalId "<External ID>" and content "<JsonPayloadName>".
        When The system process the order request.
        Then A biopro Order "should not" be available in the Distribution local data store.
        Examples:
            | External ID     | JsonPayloadName                                          |
            | 0-@             | order-inbound-scenario-3-invalid_external_id.json        |
            | 114117922233511 | order-inbound-scenario-4-invalid_order_status.json       |
            | 114117922233514 | order-inbound-scenario-7-invalid_shipment_type.json      |
            | 114117922233515 | order-inbound-scenario-8-invalid_blood_type.json         |
            | 114117922233516 | order-inbound-scenario-9-invalid_category.json           |
            | 114117922233517 | order-inbound-scenario-10-invalid_shipping_customer.json |
            | 114117922233518 | order-inbound-scenario-11-invalid_billing_customer.json  |
            | 114117922233519 | order-inbound-scenario-12-invalid_family.json            |
            | 114117922233520 | order-inbound-scenario-13_invalid_quantity.json          |

    @DIS-92
    Scenario Outline: Creating a BioPro order with duplicated external ID
        Given I have received an order inbound request with externalId "114117922233599" and content "order-inbound-scenario-2-duplicated_external_id.json".
        And   I have received an order inbound request with externalId "<External ID>" and content "<JsonPayloadName>".
        When The system process the order request.
        Then The duplicated biopro Order will not be available in the Distribution local data store.

        Examples:
            | External ID     | JsonPayloadName                                      |
            | 114117922233599 | order-inbound-scenario-2-duplicated_external_id.json |

    Rule: BioPro should have some validations to not allow any orders that have a Desired Shipping Date from the past.
    Rule: BioPro should be able to accept and create orders sent by the Order Provider for all priorities (STAT, ASAP, ROUTINE, SCHEDULED, DATE_TIME) that does not have a Desired Shipping Date indicated.
    @DIS-136 @bug @DIS-285
    Scenario Outline: Creating a BioPro order with an invalid shipping date
        Given I have received an order inbound request with externalId "<External ID>", content "<JsonPayloadName>", and desired shipping date "<Date>".
        When The system process the order request.
        Then A biopro Order "<CreateOrder>" be available in the Distribution local data store.
        Examples:
           | External ID     | JsonPayloadName                          | Date         | CreateOrder |
           | 114117922233500 | order-inbound-scenario-1-happy-path.json | 2020-01-01   | should not  |
           | 114117922233500 | order-inbound-scenario-1-happy-path.json | 2020-07-33   | should not  |
           | 114117922233526 | order-inbound-scenario-1-happy-path.json | NULL_VALUE   | should      |
           | 114117922233527 | order-inbound-scenario-1-happy-path.json | CURRENT_DATE | should      |
