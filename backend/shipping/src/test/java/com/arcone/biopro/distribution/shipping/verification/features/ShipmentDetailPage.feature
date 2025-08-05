@ui @AOA-6 @AOA-152 @AOA-19
Feature: Access Shipment Details Page

    Background:
        Given I cleaned up from the database, all shipments with order number "999996,999997,999998,999999,999990,999991,999992,999998300,4440003,4440004,4440005".

    Rule: I should be able to view order information, shipping information, and order criteria( Pick List)
        @DIS-148 @DIS-195
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
    Rule:I should be able to manage a product(s) from the shipment until the shipment is completed.
        @DIS-48 @DIS-300
        Scenario Outline: Filling the shipment
            Given The shipment details are order Number "<orderNumber>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>".
            And I have received a shipment fulfillment request with above details.
            When I am on the Shipment Fulfillment Details page.
            Then I should have an option to fill the products in the shipment for "<ProductFamily>" and "<BloodType>" line items.

            Examples:
                | orderNumber | Customer ID | Customer Name | Quantity | BloodType | ProductFamily                                                                          |
                | 999998      | 999998      | Tampa         | 10,5,23  | AP,AN,OP  | PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE                            |
                | 999992      | 999998      | Tampa         | 10,5,23  | ABP,AN,OP | RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED |




    Rule: I should be able to view the pending log of products to be filled in the shipment
        @DIS-48 @DIS-201
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


    Rule:I should be able to manage a product(s) from the shipment until the shipment is completed.
    Rule:I should be able to manage a product(s) when all requested product(s) for given line item are already filled.
    Rule:I should not be able to manage(add/remove) a product(s) once the shipment is completed.
        @DIS-300
        Scenario Outline: Being able to manage the product(s) based on shipment status.
            Given I have a shipment for order "<Order Number>" with the units "<Units>" and product codes "<Product Codes>" of family "<Product Family>" and blood type "<Blood Type>" "packed", out of <Quantity Requested> requested.
            And  The shipment status is "<Shipment Status>".
            When I am on the Shipment Fulfillment Details page.
            Then I "<Display Manage Option>" have an option to manage the products in the shipment for "<Product Family>" and "<Blood Type>" line item.

            Examples:
                | Order Number | Quantity Requested | Blood Type | Product Family               | Shipment Status | Units                       | Product Codes     | Display Manage Option |
                | 999998300    | 1                  | AP         | PLASMA_TRANSFUSABLE          | OPEN            | W812530106086               | E0685V00          | should                |
                | 999998300    | 8                  | AP         | WHOLE_BLOOD                  | OPEN            | W822530103010,W822530103011 | E0685V00,E0685V00 | should                |
                | 999998300    | 10                 | ABP        | RED_BLOOD_CELLS_LEUKOREDUCED | COMPLETED       | W812530106089               | E0685V00          | should not            |


        Rule:I should be able to see internal transfer details.
        @ui @DIS-444
        Scenario Outline: Selecting and accessing an Internal Transfer shipment details
            Given The shipment details are order Number "<orderNumber>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>", Temperature Category as "<Category>", Shipment Type defined as "<Shipment Type>", Label Status as "<Label Status>" and Quarantined Products as "<Quarantined Products>".
            And I have received a shipment fulfillment request with above details.
            When I am on the Shipment Fulfillment Details page.
            Then I can see the Order Information, the Shipping Information, and Order Criteria.
            And I can see the Internal Transfer details with Shipment type as "<Expected Shipment Type>", Label Status as "<Label Status>", Quarantined Products as "<Expected Quarantined Products>".
            When I choose to fill product of family "<expectedFamily>" and blood type "<expectedBloodType>".
            Then I can see the order comment "DISTRIBUTION COMMENTS".
            And I can navigate back to the Order "<orderNumber>" Details page.
            Examples:
                | orderNumber | Customer ID | Customer Name             | Quantity | BloodType | ProductFamily                                                                           | expectedFamily              | expectedBloodType | Category | Shipment Type    | Label Status | Quarantined Products | Expected Shipment Type | Expected Quarantined Products |
                | 4440003     | DL1         | Distribution and Labeling | 10,5,23  | AP,AN,OP  | PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE                            | PLASMA TRANSFUSABLE          | AP                | FROZEN   | INTERNAL_TRANSFER | LABELED      | false                | INTERNAL TRANSFER      |        NO                      |
                | 4440004     | 234567891   | MDL Hub 2                 | 2,2,10   | ABP,AN,OP | RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED | RED BLOOD CELLS LEUKOREDUCED | ABP               | FROZEN   | INTERNAL_TRANSFER | LABELED      | true                 | INTERNAL TRANSFER      |        YES                     |
                | 4440005     | DO1         | Distribution Only         | 10,5,23  | AP,AN,OP  | PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE                            | PLASMA TRANSFUSABLE          | AP                | FROZEN   | INTERNAL_TRANSFER | UNLABELED    | true                 | INTERNAL TRANSFER      |        YES                    |

