Feature: Fill Product
    As a distribution technician,
    I want to be able to enter the unit number and product code,
    so that I can fill a shipment.

    Scenario Outline: Entering an unit number to fill an order
        Given I am at the shipment details page of some order.
        When I choose to fill a product.
        And I fill the Unit Number "<UN>", Product Code "<Code>".
        And I choose visual inspection as "<Inspection>".
        Then I should see a record containing "<UN>", "<Code>", "<Product Description>", and "<Inspection>" in the filled products table.

        Examples:
            | UN               | Code       | Inspection   | Product Description |
            | W036898786754    | E0701V00   | Satisfactory |                     |
            | =W03689878675500 | =<E0701V00 | Satisfactory |                     |


    Scenario Outline: Entering an unsuitable product
        Given I am at the shipment details page of some order.
        When I choose to fill a product which is "<Condition>".
        And I fill the Unit Number "<UN>", Product Code "<Code>".
        And I choose visual inspection as "<Inspection>".
        Then I must see the error message "<Message>".
        And I should not see the product added to the filled products table.

        Examples:
            | Condition            | UN               | Code       | Inspection   | Message                   |
            | Expired              | W036898786756    | E0701V00   | Satisfactory | Expired error message     |
            | Discarded            | =W03689878675700 | =<E0713V00 | Satisfactory | Discarded error message   |
            | Quarantined          | W036898786758    | E0707V00   | Satisfactory | Quarantined error message |
            | Non existent         | =W03689878675900 | =<E0701V00 | Satisfactory | Non existent error        |
            | Wrong Blood Type     | W036898786760    | E0713V00   | Satisfactory | Order criteria error      |
            | Wrong Product Family | =W03689878676100 | =<E0701V00 | Satisfactory | Order criteria error      |
            | Shipped              | W036898786762    | E0703V00   | Satisfactory | Shipped product error     |
            | Different Location   | =W03689878676300 | =<E0703V00 | Satisfactory | Product not found error   |


    Scenario Outline: Filling a product which is already filled in another shipment
        Given I have the order "<Order>", which contains "<UN>", "<Code>", and "<Inspection>".
        And  I am at the shipment details page of the order "<Order 2>".
        When I choose to fill a product.
        And I fill the Unit Number "<UN>" and Product Code "<Code>" of a product which is part of another shipment.
        And I choose visual inspection as "<Inspection>".
        Then I must see the error message "<Message>".

        Examples:
            | Order | Order 2 | UN            | Code     | Inspection   | Message |
            | 1001  | 1002    | W036898786755 | E0701V00 | Satisfactory | TBD     |
