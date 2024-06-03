Feature: View Pick List

    Rule: I should be able to view the line order in the pick list.
    Rule: I should be able to see the order number and customer details in the pick list.
    Rule: I should be able to view the number of products that are ready to be filled in an order.

    Scenario Outline: View Shipment details.
        Given The shipment details are order Number "<orderNumber>" , customer ID "<Customer ID>" , Customer Name "<Customer Name>" , Product Details : Quantities "<Quantity>" , Blood Types : "<BloodType>" , Product Families "<ProductFamily>".
        And I have received a shipment fulfillment request with above details.
        And I am on the Shipment Fulfillment Details page.
        When I choose to view the Pick List.
        Then I am able to view the correct Order Details.
        And I am able to view the correct Shipment Details.
        And I should see a message "<shortDateMessage>" indicating There are no suggested short-dated products.

        Examples:
        |orderNumber | Customer ID | Customer Name      | Quantity | BloodType | ProductFamily                                              |shortDateMessage                             |
        |1           | 1           | Testing Customer   | 10,5,8   | AP,BP,OP  |Transfusable Plasma,Transfusable Plasma,Transfusable Plasma|There are no suggested short-dated products. |


    Rule: I should be able to view the line order in the pick list with short date products.
        Scenario Outline: View Shipment details with short date products.
            Given The shipment details are order Number "<orderNumber>" , customer ID "<Customer ID>" , Customer Name "<Customer Name>" , Product Details : Quantities "<Quantity>" , Blood Types : "<BloodType>" , Product Families "<ProductFamily>" , Short Date Products "<UnitNumber>" , Product Code "<ProductCode>".
            And I have received a shipment fulfillment request with above details.
            And I am on the Shipment Fulfillment Details page.
            When I choose to view the Pick List.
            Then I am able to view the correct Order Details.
            And I am able to view the correct Shipment Details with short date products.

            Examples:
                |orderNumber | Customer ID | Customer Name    | Quantity | BloodType | ProductFamily                                             | UnitNumber                 | ProductCode   |
                |2           | 1           | Testing Customer | 10,5,23  | AP,AN,OP  |Transfusable Plasma,Transfusable Plasma,Transfusable Plasma|W036810946400,W036810946401 |E246300,E255500|
