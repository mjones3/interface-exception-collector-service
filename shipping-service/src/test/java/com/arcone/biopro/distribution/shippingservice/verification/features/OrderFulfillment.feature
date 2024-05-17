Feature: Order fulfillment request

  Rule: I should be able to receive the order fulfillment request.
    Rule: I should be able to persist with the order fulfilled request on the local store.
    Scenario: Receive order fulfillment request
      Given I have no order fulfillment requests.
      When I receive an order fulfillment request event.
      Then The order request will be available in the Distribution local data store and I can fill the order.

  Rule: I should be able to list the pending order fulfillment request.
    Rule: I should be able to view the order fulfillment details.
    Scenario Outline: View order's fulfillment details
      Given I have an order request persisted.
      When I retrieve the order pending list.
      Then I am able to see the pending requests.
      When I retrieve one order by order number.
      Then I am able to view the order fulfillment details.
      And The attribute <Family> contains <Family Value>.
      And The attribute <Blood Group> contains <Group Value>.
      And The attribute <Product Quantity> contains <Quantity>.
      And The attribute <Order Number> contains <Number>.
      Examples:
        | Family        | Family Value        | Blood Group | Group Value | Product Quantity | Quantity | Order Number | Number |
        | productFamily | Transfusable Plasma | bloodType   | B           | productQuantity  | 3        | orderNumber  | 5      |
        | productFamily | Transfusable Plasma | bloodType   | A           | productQuantity  | 3        | orderNumber  | 5      |
        | productFamily | Transfusable Plasma | bloodType   | O           | productQuantity  | 3        | orderNumber  | 5      |
        | productFamily | Transfusable Plasma | bloodType   | AB          | productQuantity  | 3        | orderNumber  | 5      |
