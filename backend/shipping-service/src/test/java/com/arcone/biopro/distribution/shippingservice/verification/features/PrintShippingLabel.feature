Feature: Print Shipping Label
    As a DT, I want to be able to view and print the shipping label, so I can ensure that the box is going to the correct location and place it in the box.

    Background:
        Given I cleaned up from the database the packed item that used the unit number "W036898786810".
        And I cleaned up from the database, all shipments with order number "456, 432".

    Rule: I should be able to view and print the shipping label in pdf format when the shipment is completed.
        Rule: I should be able to reprint the shipping label if needed.
    Rule: I should be able to view the shipping details on the label.
        Scenario Outline: Print the Shipping Label
            Given The shipment details are Order Number <Order Number>, Location Code <Location Code>, Customer ID <Customer ID>, Customer Name "<Customer Name>", Department "<Department>", Address Line 1 "<Address Line 1>", Address Line 2 "<Address Line 2>", Unit Number "<Unit Number>", Product Code "<Product Code>", Product Family "<Product Family>", Blood Type "<Blood Type>", Expiration "<Expiration>", Quantity <Quantity>.
            And I received a shipment fulfillment request with above details.
            And I have filled the shipment with the unit number "<Unit Number>" and product code "<Product Code>".
            And I have completed a shipment with above details.
            And I am on the Shipment Fulfillment Details page for order <Order Number>.
            When I choose to print the Shipping Label.
            Then I am able to see the Shipping Label content.

            Examples:
                | Order Number | Location Code | Customer ID | Customer Name        | Department    | Address Line 1 | Address Line 2 | Unit Number   | Product Code | Product Family      | Blood Type | Expiration | Quantity |
                | 456          | 3             | 1           | Random Hospital Inc. | Blood Banking | Street 1       | Suite 2        | W036898786810 | E4697V00     | Transfusable Plasma | AP         | 04-09-2025 | 1        |

    Rule: I should not be able to view and print the shipping label in pdf format when the shipment is not completed.
        Scenario Outline: Print the Shipping Label with incomplete Shipment
            Given The shipment details are Order Number <Order Number>, Location Code <Location Code>, Customer ID <Customer ID>, Customer Name "<Customer Name>", Department "<Department>", Address Line 1 "<Address Line 1>", Address Line 2 "<Address Line 2>", Unit Number "<Unit Number>", Product Code "<Product Code>", Product Family "<Product Family>", Blood Type "<Blood Type>", Expiration "<Expiration>", Quantity <Quantity>.
            And I have an open shipment with above details.
            When I enter the Shipment Fulfillment Details page for order <Order Number>.
            Then I should not be able to print the Shipping Label.

            Examples:
                | Order Number | Location Code | Customer ID | Customer Name        | Department    | Address Line 1 | Address Line 2 | Unit Number   | Product Code | Product Family      | Blood Type | Expiration | Quantity |
                | 432          | 3             | 1           | Random Hospital Inc. | Blood Banking | Street 1       | Suite 2        | W036810946400 | E246300      | Transfusable Plasma | AP         | 04-09-2025 | 1        |

