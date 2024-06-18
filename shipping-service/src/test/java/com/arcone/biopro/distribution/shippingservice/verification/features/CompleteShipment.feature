Feature: Complete Shipment Feature
    As a distribution technician, I want to complete a shipment, so I can ship products to the customer.

    Rule: I should be able to complete a shipment whenever at least one product is filledRule: I should be able to view the list of packed products added once it is filled on the Shipment Fulfillment Details page.
        Rule: I should see a success message when the shipment is completed.
    Rule: I should be able to view the shipping details of the products once it is shipped on the Shipment Fulfillment Details page.
        Rule: I should not be able to see the pending log once the order is completely filled, shipped, or closed. (This is going to be tested on Shipment Fulfillment Details page and Fill Shipment page)
    Rule: I should be able to view the pending log of products to be filled for each line item on the Shipment Fulfillment Details page.
        Scenario Outline: Complete Shipment with suitable products.
            Given The shipment details are order Number "<Order Number>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>".
            And I have received a shipment fulfillment request with above details.
            And I am on the Shipment Fulfillment Details page for order <Order Number>.
            And I choose to fill product of family "<Family>" and blood type "<Type>".
            When I add the unit "<UN>" with product code "<Code>".
            And I define visual inspection as "<Visual Inspection>".
            Then I should see the list of packed products added including "<UN>" and "<Code>".
            When I choose to return to the shipment details page.
            And I choose to complete the Shipment.
            Then I should see a "Success" message: "<Message>".
            And I am able to view the total of <Quantity Shipped> products shipped.
            And I am not able to view the pending log of products.

            Examples:
                | Order Number | Customer ID | Customer Name    | Quantity | BloodType | ProductFamily                                               | Message            | Family              | Type | UN            | Code     | Visual Inspection | Quantity Shipped |
                | 10           | 1           | Testing Customer | 10,5,8   | A,B,O     | Transfusable Plasma,Transfusable Plasma,Transfusable Plasma | Shipment Completed | Transfusable Plasma | A    | W036898786802 | E7648V00 | Satisfactory      | 1                |
