Feature: Access Shipment Details Page

    Background:
        Given I cleaned up from the database, all shipments with order number "999996, 999997, 999998, 999999, 999990".

    Rule: I should be able to view order information, shipping information, and order criteria( Pick List)
        Scenario Outline: Selecting and accessing a shipment details
            Given The shipment details are order Number "<orderNumber>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>".
            And I have received a shipment fulfillment request with above details.
            When I am on the Shipment Fulfillment Details page.
            Then I can see the Order Information, the Shipping Information, and Order Criteria.

          Examples:
                |orderNumber | Customer ID | Customer Name | Quantity | BloodType | ProductFamily                                              |
                |999996      | 999996      | Tampa         | 10,5,23   | AP,AN,OP  |Transfusable Plasma,Transfusable Plasma,Transfusable Plasma|


    Rule: I should have the option to view the pick list
        Scenario Outline: View the Pick List
            Given The shipment details are order Number "<orderNumber>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>".
            And I have received a shipment fulfillment request with above details.
            When I am on the Shipment Fulfillment Details page.
            Then I should have an option to view the Pick List.

            Examples:
                |orderNumber | Customer ID | Customer Name | Quantity | BloodType | ProductFamily                                              |
                |999997      | 999997      | Tampa         | 10,5,23   | AP,AN,OP  |Transfusable Plasma,Transfusable Plasma,Transfusable Plasma|



    Rule:I should have the option to Fill the shipment
        Scenario Outline: Filling the shipment
            Given The shipment details are order Number "<orderNumber>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>".
            And I have received a shipment fulfillment request with above details.
            When I am on the Shipment Fulfillment Details page.
            Then I should have an option to fill the products in the shipment.

            Examples:
                |orderNumber | Customer ID | Customer Name | Quantity | BloodType | ProductFamily                                              |
                |999998      | 999998      | Tampa         | 10,5,23   | AP,AN,OP  |Transfusable Plasma,Transfusable Plasma,Transfusable Plasma|




    Rule: I should be able to view the pending log of products to be filled in the shipment
        Scenario Outline: Pending log of products
            Given The shipment details are order Number "<orderNumber>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>".
            And I have received a shipment fulfillment request with above details.
            # And no products have been filled.
            # TODO there is no option to validate this now, update with fill order implementation
            When I am on the Shipment Fulfillment Details page.
            Then I should see zero products are filled out of the total number of products to be filled.

            Examples:
                |orderNumber | Customer ID | Customer Name | Quantity | BloodType | ProductFamily                                              |
                |999999      | 999999      | Tampa         | 10,5,23   | AP,AN,OP  |Transfusable Plasma,Transfusable Plasma,Transfusable Plasma|



    Rule: I should not be able to see the product shipping details when no products are shipped
        Scenario Outline: No products shipped details displayed
            Given The shipment details are order Number "<orderNumber>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>".
            And I have received a shipment fulfillment request with above details.
            # And no products have been shipped.
            # TODO there is no option to validate this now, update with fill order implementation
            When I am on the Shipment Fulfillment Details page.
            Then I should not be able to see any product shipped details.
            Examples:
                |orderNumber | Customer ID | Customer Name | Quantity | BloodType | ProductFamily                                              |
                |999990      | 999990      | Tampa         | 10,5,23   | AP,AN,OP  |Transfusable Plasma,Transfusable Plasma,Transfusable Plasma|

