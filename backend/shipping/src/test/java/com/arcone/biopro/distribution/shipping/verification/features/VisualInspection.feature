Feature: Record Unsatisfactory Visual Inspection


    Rule: I should be able to see the list of the discard reasons configured for distribution.
        Rule: I should be able to select the configured reasons for failed visual inspection.
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

#        [  ] Dialog's title for unsatisfactory visual inspection;
#        [  ] Dialog's description for unsatisfactory visual inspection;

            Examples:
                | Order Number | Customer ID | Customer Name    | Quantity | BloodType | ProductFamily                                                                          | Family                       | Type | UN               | Code       |
                | 200          | 1           | Testing Customer | 10,5,8   | A,B,O     | PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE                            | PLASMA TRANSFUSABLE          | A    | =W03689878680300 | =<E7650V00 |
                | 201          | 1           | Testing Customer | 8,5,8    | AP,BP,OP  | RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED | RED BLOOD CELLS LEUKOREDUCED | AP   | =W81253010609100 | =<E0685V00 |
