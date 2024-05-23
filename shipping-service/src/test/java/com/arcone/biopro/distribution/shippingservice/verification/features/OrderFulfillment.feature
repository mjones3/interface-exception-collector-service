Feature: Shipment fulfillment request

    Rule: I should be able to receive the shipment fulfillment request.
        Rule: I should be able to persist the shipment fulfilled request on the local store.
        Scenario: Receive shipment fulfillment request
            Given I have no shipment fulfillment requests.
            When I receive a shipment fulfillment request event.
            Then The shipment request will be available in the Distribution local data store and I can fill the order.

    Rule: I should be able to list the pending shipment fulfillment request.
        Rule: I should be able to view the shipment fulfillment details.
        Scenario Outline: View shipment's fulfillment details
            Given I have a shipment request persisted.
            When I retrieve the shipment list.
            Then I am able to see the requests.
            When I retrieve one shipment by order number.
            Then I am able to view the shipment fulfillment details.
            And The attribute "Product Family" contains "Transfusable Plasma".
            And The attribute "Blood Type" contains "<Group Value>".
            And The attribute "Product Quantity" contains "<Quantity>".
            And The attribute "Order Number" is not empty.
            Examples:
                | Group Value | Quantity |
                | AP          | 10       |
                | AN          | 5        |
                | OP          | 8        |
