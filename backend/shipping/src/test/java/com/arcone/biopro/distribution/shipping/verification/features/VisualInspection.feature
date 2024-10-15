Feature: Record Unsatisfactory Visual Inspection
    As a distribution technician,
    I want to record the unacceptable visual inspection of the products,
    so I can meet the AABB standards
    and prevent the shipping of unacceptable products.

        Background:
            Given I cleaned up from the database the packed item that used the unit number "W036898786803,W812530106091".
            And I cleaned up from the database, all shipments with order number "200,201".

        Rule: I should be able to see the list of the discard reasons configured for distribution.
        Rule: I should be able to select the configured reasons for failed visual inspection.
        Rule: I should not be able to continue the process and discard the product if the visual Inspection is unacceptable.
        Scenario Outline: Verify Failed Visual Inspection Discard Form
            Given The shipment details are order Number "<Order Number>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>".
            And The visual inspection configuration is "enabled".
            And I have received a shipment fulfillment request with above details.
            And I am on the Shipment Fulfillment Details page for order <Order Number>.
            And I choose to fill product of family "<Family>" and blood type "<Type>".
            When I add the unit "<UN>" with product code "<Code>".
            And I define visual inspection as "Unsatisfactory".
            Then I should see the discard form.
            And I should see all the configured discard reasons.
            When I choose to cancel the discard form.
            Then I should see the discard form is closed.
            And I should not see the unit "<UN>" with product code "<Code>" added to the filled products table.

            Examples:
                | Order Number | Customer ID | Customer Name    | Quantity | BloodType | ProductFamily                                                                          | Family                       | Type | UN               | Code       |
                | 200          | 1           | Testing Customer | 10,5,8   | A,B,O     | PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE                            | PLASMA TRANSFUSABLE          | A    | =W03689878680300 | =<E7650V00 |
                | 201          | 1           | Testing Customer | 8,5,8    | AP,BP,OP  | RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED | RED BLOOD CELLS LEUKOREDUCED | AP   | =W81253010609100 | =<E0685V00 |


        Rule: I should be able to record the failed visual inspection products in the shipment.
        Rule: I should be able to record the reason for failed visual inspection.
        Rule: I should be able to see an acknowledgement message indicating that the product must be discarded if the visual Inspection is unacceptable. (Trigger discard in shipping).
        Rule: I should be able to submit when all the required information is completed.
        Scenario Outline: Record Visual Inspection for Unsatisfactory Products
            Given The shipment details are order Number "<Order Number>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>".
            And The visual inspection configuration is "enabled".
            And I have received a shipment fulfillment request with above details.
            And I am on the Shipment Fulfillment Details page for order <Order Number>.
            And I choose to fill product of family "<Family>" and blood type "<Type>".
            When I add the unit "<UN>" with product code "<Code>".
            And I define visual inspection as "Unsatisfactory".
            Then I should see the discard form.
            And I select the "<Reason>" reason for discard the product.
            When I choose to submit the discard form.
            Then I should see a "Acknowledgment Message" message: "This product has been discarded for failed visual inspection in the system. Place in biohazard container.".

            Examples:
                | Order Number | Customer ID | Customer Name    | Quantity | BloodType | ProductFamily                                                                          | Family                       | Type | UN               | Code       | Reason     |
                | 200          | 1           | Testing Customer | 10,5,8   | A,B,O     | PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE                            | PLASMA TRANSFUSABLE          | A    | =W03689878680300 | =<E7650V00 | AGGREGATES |
                | 201          | 1           | Testing Customer | 8,5,8    | AP,BP,OP  | RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED | RED BLOOD CELLS LEUKOREDUCED | AP   | =W81253010609100 | =<E0685V00 | CLOTTED    |




        Rule: I should be required to enter comments if the reason selected is OTHER.
        Scenario Outline: Record Visual Inspection for Unsatisfactory Products Required Comments
            Given The shipment details are order Number "<Order Number>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>".
            And The visual inspection configuration is "enabled".
            And I have received a shipment fulfillment request with above details.
            And I am on the Shipment Fulfillment Details page for order <Order Number>.
            And I choose to fill product of family "<Family>" and blood type "<Type>".
            When I add the unit "<UN>" with product code "<Code>".
            And I define visual inspection as "Unsatisfactory".
            Then I should see the discard form.
            When I select the "OTHER" reason for discard the product.
            Then The comments field should be required.
            And The submit option should be "disabled".
            When I fill the comments field with "test discard comments".
            Then The submit option should be "disabled".

            Examples:
                | Order Number | Customer ID | Customer Name    | Quantity | BloodType | ProductFamily                                                                          | Family                       | Type | UN               | Code       |
                | 200          | 1           | Testing Customer | 10,5,8   | A,B,O     | PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE                            | PLASMA TRANSFUSABLE          | A    | =W03689878680300 | =<E7650V00 |
                | 201          | 1           | Testing Customer | 8,5,8    | AP,BP,OP  | RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED | RED BLOOD CELLS LEUKOREDUCED | AP   | =W81253010609100 | =<E0685V00 |

