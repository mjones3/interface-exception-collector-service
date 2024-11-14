@ui 
Feature: Complete Shipment Feature
    As a distribution technician, I want to complete a shipment, so I can ship products to the customer.

    Background:
        Given I cleaned up from the database the packed item that used the unit number "W036898786802,W812530106086,W812530106089,W036824705327,W812530106090".
        And I cleaned up from the database, all shipments with order number "108,109,110,111,112,113,114,115,116,117".

        Rule: I should be able to complete a shipment whenever at least one product is filledRule: I should be able to view the list of packed products added once it is filled on the Shipment Fulfillment Details page.
        Rule: I should see a success message when the shipment is completed.
        Rule: I should be able to view the shipping details of the products once it is shipped on the Shipment Fulfillment Details page.
        Rule: I should not be able to see the pending log once the order is completely filled, shipped, or closed. (This is going to be tested on Shipment Fulfillment Details page and Fill Shipment page)
        Rule: I should be able to view the pending log of products to be filled for each line item on the Shipment Fulfillment Details page.
        Rule: I should be able to complete the shipment process without second verification if configured by the blood center.

        @DST-202 @DIS-162 @DIS-156 @DIS-56 @DIS-25 @DIS-21 @DIS-201
        Scenario Outline: Complete Shipment with suitable products.
            Given The shipment details are order Number "<Order Number>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>".
            And The check digit configuration is "disabled".
            And The visual inspection configuration is "<Inspection Config>".
            And The second verification configuration is "disabled".
            And I have received a shipment fulfillment request with above details.
            And I am on the Shipment Fulfillment Details page for order <Order Number>.
            And I choose to fill product of family "<Family>" and blood type "<Type>".
            When I add the unit "<UN>" with product code "<Code>".
            And I define visual inspection as "Satisfactory", if needed.
            Then I should see the list of packed products added including "<UN>" and "<Code>".
            And I should see the inspection status as "Satisfactory", if applicable.
            When I choose to return to the shipment details page.
            And I should not see the verify products option available.
            And I choose to complete the Shipment.
            Then I should see a "Success" message: "Shipment Completed".
            And I am able to view the total of <Quantity Shipped> products shipped.
            And I am not able to view the pending log of products.

            Examples:
                | Order Number | Customer ID | Customer Name    | Quantity | BloodType | ProductFamily                                                                          | Family                       | Type | UN               | Code       | Quantity Shipped | Inspection Config |
                | 108          | 1           | Testing Customer | 10,5,8   | A,B,O     | PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE                            | PLASMA TRANSFUSABLE          | A    | =W03689878680200 | =<E7648V00 | 1                | enabled           |
                | 109          | 1           | Testing Customer | 10,5,8   | ABP,BP,OP | RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED | RED BLOOD CELLS LEUKOREDUCED | ABP  | W812530106086    | E0685V00   | 1                | enabled           |
                | 116          | 1           | Testing Customer | 10,5,8   | ABP,BP,OP | RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED | RED BLOOD CELLS LEUKOREDUCED | ABP  | W812530106086    | E0685V00   | 1                | disabled          |


        @DIS-69
        Scenario Outline: Fill product with check digit <Check Digit Config> and visual inspection <Inspection Config>
            Given The shipment details are order Number "<Order Number>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>".
            And The check digit configuration is "<Check Digit Config>".
            And The visual inspection configuration is "<Inspection Config>".
            And I have received a shipment fulfillment request with above details.
            And I am on the Shipment Fulfillment Details page for order <Order Number>.
            And I choose to fill product of family "<Family>" and blood type "<Type>".
            When I type the unit "<UN>", digit "<Digit>", and product code "<Code>".
            Then I can "<Message Type>" message "<Message Content>".
            And I am able to proceed with the product filling process.

            Examples:
                | Order Number | Customer ID | Customer Name    | Quantity | BloodType | ProductFamily                                                                          | Message Content | Message Type      | Family                       | Type | Code       | UN               | Check Digit Config | Digit | Inspection Config |
                | 110          | 1           | Testing Customer | 10,5,8   | A,B,O     | PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE                            |                 | not see any error | PLASMA TRANSFUSABLE          | A    | E7648V00   | W036824705327    | enabled            | 2     | enabled           |
                | 113          | 1           | Testing Customer | 10,5,8   | AP,BP,OP  | RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED |                 | not see any error | RED BLOOD CELLS LEUKOREDUCED | AP   | =<E0685V00 | =W81253010608900 | enabled            |       | enabled           |
                | 114          | 1           | Testing Customer | 10,5,8   | AP,BP,OP  | RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED |                 | not see any error | RED BLOOD CELLS LEUKOREDUCED | AP   | E0685V00   | W812530106090    | disabled           |       | enabled           |
                | 115          | 1           | Testing Customer | 10,5,8   | AP,BP,OP  | RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED |                 | not see any error | RED BLOOD CELLS LEUKOREDUCED | AP   | E0685V00   | W812530106090    | disabled           |       | disabled          |

        @DIS-69
        Scenario Outline: Fill product with check digit invalid or empty
            Given The shipment details are order Number "<Order Number>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>".
            And The check digit configuration is "<Check Digit Config>".
            And The visual inspection configuration is "<Inspection Config>".
            And I have received a shipment fulfillment request with above details.
            And I am on the Shipment Fulfillment Details page for order <Order Number>.
            And I choose to fill product of family "<Family>" and blood type "<Type>".
            When I type the unit "<UN>", digit "<Digit>".
            Then I can "<Message Type>" message "<Message Content>".

            Examples:
                | Order Number | Customer ID | Customer Name    | Quantity | BloodType | ProductFamily                                                                          | Message Content         | Message Type         | Family                       | Type | Code     | UN            | Check Digit Config | Digit | Inspection Config |
                | 111          | 1           | Testing Customer | 10,5,8   | AP,BP,OP  | RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED | Check Digit is Invalid  | see an error message | RED BLOOD CELLS LEUKOREDUCED | AP   | E0685V00 | W812530106087 | enabled            | F     | disabled          |
                | 112          | 1           | Testing Customer | 10,5,8   | AP,BP,OP  | RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED | Check Digit is Required | see an error message | RED BLOOD CELLS LEUKOREDUCED | AP   | E0685V00 | W812530106088 | enabled            |       | disabled          |


        @DST-202
        Rule: I should be able to start the second verification process if configured by the blood center.
        Scenario Outline: Second Verification of Shipment with suitable products.
            Given The shipment details are order Number "<Order Number>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>".
            And The check digit configuration is "disabled".
            And The visual inspection configuration is "<Inspection Config>".
            And The second verification configuration is "enabled".
            And I have received a shipment fulfillment request with above details.
            And I am on the Shipment Fulfillment Details page for order <Order Number>.
            And I choose to fill product of family "<Family>" and blood type "<Type>".
            When I add the unit "<UN>" with product code "<Code>".
            And I define visual inspection as "Satisfactory", if needed.
            Then I should see the list of packed products added including "<UN>" and "<Code>".
            And I should see the inspection status as "Satisfactory", if applicable.
            When I choose to return to the shipment details page.
            Then I should see the verify products option available.
            And I should not see the complete shipment option available.

            Examples:
                | Order Number | Customer ID | Customer Name    | Quantity | BloodType | ProductFamily                                               | Family              | Type | UN               | Code       | Inspection Config |
                | 117          | 1           | Testing Customer | 10,5,8   | A,B,O     | PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE | PLASMA TRANSFUSABLE | A    | =W03689878680200 | =<E7648V00 | enabled           |




