@ui
Feature: Access Shipment Details Page

    Background:
        Given I cleaned up from the database, all shipments with order number "999996,999997,999998,999999,999990,999991,999992".

    Rule: I should be able to view order information, shipping information, and order criteria( Pick List)
        @DIS-148
        Scenario Outline: Selecting and accessing a shipment details
            Given The shipment details are order Number "<orderNumber>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>".
            And I have received a shipment fulfillment request with above details.
            When I am on the Shipment Fulfillment Details page.
            Then I can see the Order Information, the Shipping Information, and Order Criteria.
            And I can see the order comment "DISTRIBUTION COMMENTS".
            When I choose to fill product of family "<expectedFamily>" and blood type "<expectedBloodType>".
            Then I can see the order comment "DISTRIBUTION COMMENTS".
            And I can navigate back to the Order "<orderNumber>" Details page.

          Examples:
              | orderNumber | Customer ID | Customer Name | Quantity | BloodType | ProductFamily                                                                          | expectedFamily               | expectedBloodType |
              | 999996      | 999996      | Tampa         | 10,5,23  | AP,AN,OP  | PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE                            | PLASMA TRANSFUSABLE          | AP                |
              | 999991      | 999996      | Tampa         | 2,2,10   | ABP,AN,OP | RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED | RED BLOOD CELLS LEUKOREDUCED | ABP               |



    Rule:I should have the option to Fill the shipment
        @DIS-48
        Scenario Outline: Filling the shipment
            Given The shipment details are order Number "<orderNumber>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>".
            And I have received a shipment fulfillment request with above details.
            When I am on the Shipment Fulfillment Details page.
            Then I should have an option to fill the products in the shipment.

            Examples:
                | orderNumber | Customer ID | Customer Name | Quantity | BloodType | ProductFamily                                                                          |
                | 999998      | 999998      | Tampa         | 10,5,23  | AP,AN,OP  | PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE                            |
                | 999992      | 999998      | Tampa         | 10,5,23  | ABP,AN,OP | RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED |




    Rule: I should be able to view the pending log of products to be filled in the shipment
        @DIS-48
        Scenario Outline: Pending log of products
            Given The shipment details are order Number "<orderNumber>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>".
            And I have received a shipment fulfillment request with above details.
            When I am on the Shipment Fulfillment Details page.
            Then I should see zero products are filled out of the total number of products to be filled.

            Examples:
                | orderNumber | Customer ID | Customer Name | Quantity | BloodType | ProductFamily                                              |
                | 999999      | 999999      | Tampa         | 10,5,23  | AP,AN,OP  | PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE|



    Rule: I should not be able to see the product shipping details when no products are shipped
        @DIS-48
        Scenario Outline: No products shipped details displayed
            Given The shipment details are order Number "<orderNumber>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>".
            And I have received a shipment fulfillment request with above details.
            When I am on the Shipment Fulfillment Details page.
            Then I should not be able to see any product shipped details.
            Examples:
                | orderNumber | Customer ID | Customer Name | Quantity | BloodType | ProductFamily                                               |
                | 999990      | 999990      | Tampa         | 10,5,23  | AP,AN,OP  | PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE |

