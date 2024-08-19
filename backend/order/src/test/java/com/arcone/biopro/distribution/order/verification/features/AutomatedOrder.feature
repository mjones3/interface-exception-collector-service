Feature: Validate order
    As a system, I want to validate the customer order
    so that I can save the order or decline the order based on the validation result.

    Background:
        Given I cleaned up from the database the orders with external ID "114117922233599,114117922233500,114117922233511,114117922233512,114117922233513,114117922233514,114117922233515,114117922233516,114117922233517,114117922233518,114117922233519,114117922233520".

    Scenario: Creating a BioPro order from a valid order inbound request
        Given I have received an order inbound request with externalId "114117922233500" and content "order-inbound-scenario-1-happy-path.json".
        When The system process the order request.
        Then A biopro Order will be available in the Distribution local data store.


    Scenario Outline: Creating a BioPro order from an invalid order inbound request
        Given I have received an order inbound request with externalId "<External ID>" and content "<JsonPayloadName>".
        When The system process the order request.
        Then A biopro Order will not be available in the Distribution local data store.
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

    Scenario Outline: Creating a BioPro order with duplicated external ID
        Given I have received an order inbound request with externalId "114117922233599" and content "order-inbound-scenario-2-duplicated_external_id.json".
        And   I have received an order inbound request with externalId "<External ID>" and content "<JsonPayloadName>".
        When The system process the order request.
        Then The duplicated biopro Order will not be available in the Distribution local data store.

        Examples:
            | External ID     | JsonPayloadName                                      |
            | 114117922233599 | order-inbound-scenario-2-duplicated_external_id.json |
