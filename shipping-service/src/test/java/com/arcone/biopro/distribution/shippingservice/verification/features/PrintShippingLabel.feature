Feature: Print Shipping Label
    As a DT, I want to be able to view and print the shipping label, so I can ensure that the box is going to the correct location and place it in the box.

    Rule: I should be able to view and print the shipping label in pdf format when the shipment is completed.
    Rule: I should be able to reprint the shipping label if needed.
    Rule: I should be able to view the shipping details on the label.
        Scenario Outline: Print the Shipping Label
            Given The shipment details are Order Number "<Order Number>", Location Code "<Location Code>" Customer ID "<Customer ID>", Customer Name "<Customer Name>", Department "<Department>", Address Line 1 "<Address Line 1>", Address Line 2 "<Address Line 2>", Address Complement "<Address Complement>".
            And I have completed a shipment with above details.
            And I am on the Shipment Fulfillment Details page.
            When I choose to print the Shipping Label.
            Then I am able to see the Shipping Label content.
            And I am able to Print or generate a PDF

            Examples:
                | Order Number | Location Code | Customer ID | Customer Name        | Department    | Address Line 1 | Address Line 2 | Address Complement |
                | 5            | 3             | 1           | Random Hospital Inc. | Blood Banking | Street 1       | Suite 2        | 900 Hope Way       |

    Rule: I should not be able to view and print the shipping label in pdf format when the shipment is not completed.
        Scenario Outline: Print the Shipping Label with incomplete Shipment
            Given The shipment details are Order Number "<Order Number>", Location Code "<Location Code>" Customer ID "<Customer ID>", Customer Name "<Customer Name>", Department "<Department>", Address Line 1 "<Address Line 1>", Address Line 2 "<Address Line 2>", Address Complement "<Address Complement>".
            And I have an incomplete shipment with above details.
            When I enter the Shipment Fulfillment Details page.
            Then I should not be able to print the Shipping Label.

            Examples:
                | Order Number | Location Code | Customer ID | Customer Name        | Department    | Address Line 1 | Address Line 2 | Address Complement |
                | 6            | 3             | 1           | Random Hospital Inc. | Blood Banking | Street 1       | Suite 2        | 900 Hope Way       |
