Feature: Print Packing List
    As a DT, I want to be able to print the packing list, so that I can know the products that are placed in the box.

    Background:
        Given I cleaned up from the database the packed item that used the unit number "W036898786811".

    Rule: I should be able to print the packing slip in the PDF format for the shipment when it is completed.
        Rule: I should be able to see the product details that are placed in the box when the shipment is completed.
    Rule: I should be able to reprint the packing slip, if needed.
        Scenario Outline: Print the Packing List
            Given The shipment details are Order Number <Order Number>, Location Code <Location Code>, Customer ID <Customer ID>, Customer Name "<Customer Name>", Department "<Department>", Address Line 1 "<Address Line 1>", Address Line 2 "<Address Line 2>", Unit Number "<Unit Number>", Product Code "<Product Code>", Product Family "<Product Family>", Blood Type "<Blood Type>", Expiration "<Expiration>", Quantity <Quantity>.
            And I received a shipment fulfillment request with above details.
            And I have filled the shipment with the unit number "<Unit Number>" and product code "<Product Code>".
            And I have completed a shipment with above details.
            And I am on the Shipment Fulfillment Details page for order <Order Number>.
            When I choose to print the Packing Slip.
            Then I am able to see the Packing Slip content.

            Examples:
                | Order Number | Location Code | Customer ID | Customer Name        | Department            | Address Line 1 | Address Line 2 | Unit Number   | Product Code | Product Family      | Blood Type | Expiration | Quantity | Unit Number   | Product Code |
                | 456          | 3             | 1           | Random Hospital Inc. | Testing Blood Banking | Street N1      | Suite N2       | W036898786811 | E4701V00     | Transfusable Plasma | AP         | 04-09-2025 | 1        | W036898786810 | E4697V00     |

    Rule: I should not be able to print the packing slip in the PDF format for the shipment when it is open.
        Scenario Outline: Print the Packing List with an open Shipment
            Given The shipment details are Order Number <Order Number>, Location Code <Location Code>, Customer ID <Customer ID>, Customer Name "<Customer Name>", Department "<Department>", Address Line 1 "<Address Line 1>", Address Line 2 "<Address Line 2>", Unit Number "<Unit Number>", Product Code "<Product Code>", Product Family "<Product Family>", Blood Type "<Blood Type>", Expiration "<Expiration>", Quantity <Quantity>.
            And I have an open shipment with above details.
            When I enter the Shipment Fulfillment Details page for order <Order Number>.
            Then I should not be able to print the Packing List.

            Examples:
                | Order Number | Location Code | Customer ID | Customer Name        | Department    | Address Line 1 | Address Line 2 | Unit Number   | Product Code | Product Family      | Blood Type | Expiration | Quantity |
                | 432          | 3             | 1           | Random Hospital Inc. | Blood Banking | Street 1       | Suite 2        | W036810946400 | E246300      | Transfusable Plasma | AP         | 04-09-2025 | 1        |
