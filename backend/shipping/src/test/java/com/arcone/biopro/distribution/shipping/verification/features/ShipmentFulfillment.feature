@api
Feature: Shipment fulfillment request

        Rule: I should be able to receive the shipment fulfillment request.
        Rule: I should be able to persist the shipment fulfilled request on the local store.
        @DIS-65 @DIS-57
        Scenario: Receive shipment fulfillment request
            Given I have no shipment fulfillment requests.
            When I receive a shipment fulfillment request event.
            Then The shipment request will be available in the Distribution local data store and I can fill the shipment.


        Rule: I should be able to list the pending shipment fulfillment request.
        Rule: I should be able to view the shipment fulfillment details.
        @DIS-65 @DIS-57
        Scenario Outline: View shipment's fulfillment details
            Given I have a shipment request persisted.
            When I retrieve the shipment list.
            Then I am able to see the requests.
            When I retrieve one shipment by shipment id.
            Then I am able to view the shipment fulfillment details.
            And The item attribute "Product Family" contains "PLASMA_TRANSFUSABLE".
            And The item attribute "Blood Type" contains "<Group Value>".
            And The item attribute "Product Quantity" contains "<Quantity>".
            And The item attribute "Shipment Id" is not empty.
            Examples:
                | Group Value | Quantity |
                | A           | 10       |
                | B           | 5        |
                | O           | 8        |


        Rule: I should be able to receive the shipment fulfillment request with short date product details.
        Rule: I should be able to persist with the short date shipment fulfilled request on the local store.
        @DIS-65
        Scenario: Receive shipment fulfillment request
            Given I have no shipment fulfillment requests.
            When I receive a shipment fulfillment request event.
            Then The shipment request will be available in the Distribution local data store and I can fill the shipment.


        Rule: I should be able to list the pending shipment fulfillment request.
        Rule: I should be able to view the shipment fulfillment details with short date products.
        @DIS-59
        Scenario Outline: View shipment's fulfillment details
            Given I have a shipment request persisted.
            When I retrieve the shipment list.
            Then I am able to see the requests.
            When I retrieve one shipment by shipment id.
            Then I am able to view the shipment fulfillment details.
            And The fulfillment request attribute "Order Number" is not empty.
            And The item attribute "Shipment Id" is not empty.
            And The item attribute "Product Family" contains "PLASMA_TRANSFUSABLE".
            And The item attribute "Blood Type" contains "<Group Value>".
            And The item attribute "Product Quantity" contains "<Quantity>".
            And The short date item attribute "Unit Number" contains "<Unit Number>".
            And The short date item attribute "Product Code" contains "<Product Code>".
            Examples:
                | Group Value | Quantity | Unit Number   | Product Code |
                | A           | 10       | W036810946300 | E086900      |
                | B           | 5        | W036810946301 | E070700      |
