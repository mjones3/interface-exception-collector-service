@AOA-6 @AOA-40 @AOA-152 @AOA-128 @AOA-105 @AOA-240 @AOA-19 @AOA-197 @AOA-294
Feature: Complete Shipment Feature
    As a distribution technician, I want to complete a shipment, so I can ship products to the customer.

    Background:
        Given I cleaned up from the database the packed item that used the unit number "W036898786802,W812530106086,W812530106089,W036824705327,W812530106090,W812530107002,W812530107001,W812530106099,W036898786759,W812530107008,W812530107009,W8125301070010,W812530107012,W812530107013,W036825158911,W036825158912,W036825158913,W036825151111,W036825151112,W013682515113,W036825151115,W036598786805,W036598786806,W03683047922100,W03683047922200,W03683047922300,W03683047922400".
        And I cleaned up from the database, all shipments with order number "108,109,110,111,112,113,114,115,116,117,254001,254002,254003,1125,336001,336002,336003,337001,4450001,4450002,4450003,4450004,446001,446002,446003,446004,479001, 479002, 479003,479004".

        Rule: I should be able to complete a shipment whenever at least one product is filled.
        Rule: I should be able to view the list of packed products added once it is filled on the Shipment Fulfillment Details page.
        Rule: I should see a success message when the shipment is completed.
        Rule: I should be able to view the shipping details of the products once it is shipped on the Shipment Fulfillment Details page.
        Rule: I should not be able to see the pending log once the order is completely filled, shipped, or closed. (This is going to be tested on Shipment Fulfillment Details page and Fill Shipment page)
        Rule: I should be able to view the pending log of products to be filled for each line item on the Shipment Fulfillment Details page.
        Rule: I should be able to complete the shipment process without second verification if configured by the blood center.
        Rule: I should be able to complete a shipment with cryo and cryo-reduced plasma Products.
        Rule: I should be able to complete a shipment with washed apheresis platelets products.
        Rule: I should be able to complete a shipment with for washed PRT apheresis platelets products.
        Rule: I should be able to complete a shipment with washed red blood cells products.

        @ui  @DIS-202 @DIS-162 @DIS-156 @DIS-56 @DIS-25 @DIS-21 @DIS-201 @bug @DIS-273 @DIS-254 @DIS-336 @DIS-337 @DIS-446 @DIS-479
        Scenario Outline: Complete Shipment with suitable products.
            Given The shipment details are order Number "<Order Number>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>" , Temperature Category "<Category>".
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
                | Order Number | Customer ID | Customer Name    | Quantity | BloodType | ProductFamily                                                                          | Family                           | Type | UN               | Code       | Quantity Shipped | Inspection Config | Category         |
                | 108          | 1           | Testing Customer | 10,5,8   | ANY,B,O   | PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE                            | PLASMA TRANSFUSABLE              | ANY  | =W03689878680200 | =<E7648V00 | 1                | enabled           | FROZEN           |
                | 109          | 1           | Testing Customer | 10,5,8   | ANY,BP,OP | RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED | RED BLOOD CELLS LEUKOREDUCED     | ANY  | W812530106086    | E0685V00   | 1                | enabled           | FROZEN           |
                | 116          | 1           | Testing Customer | 10,5,8   | ABP,BP,OP | RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED | RED BLOOD CELLS LEUKOREDUCED     | ABP  | W812530106086    | E0685V00   | 1                | disabled          | FROZEN           |
                | 254001       | 1           | Testing Customer | 5,8      | AP,BN     | WHOLE_BLOOD,WHOLE_BLOOD                                                                | WHOLE BLOOD                      | AP   | =W81253010700200 | =<E0023V00 | 1                | enabled           | FROZEN           |
                | 254002       | 1           | Testing Customer | 10,5     | ABP,AP    | WHOLE_BLOOD_LEUKOREDUCED,WHOLE_BLOOD_LEUKOREDUCED                                      | WHOLE BLOOD LEUKOREDUCED         | ABP  | =W81253010700100 | =<E0033V00 | 1                | enabled           | FROZEN           |
                | 254003       | 1           | Testing Customer | 2,2      | OP,ON     | RED_BLOOD_CELLS,RED_BLOOD_CELLS                                                        | RED BLOOD CELLS                  | ON   | =W81253010609900 | =<E0167V00 | 1                | enabled           | FROZEN           |
                | 336001       | 1           | Testing Customer | 2        | ANY       | APHERESIS_PLATELETS_LEUKOREDUCED                                                       | APHERESIS PLATELETS LEUKOREDUCED | ANY  | =W81253010700800 | =<EA141V00 | 1                | enabled           | ROOM_TEMPERATURE |
                | 336002       | 1           | Testing Customer | 2        | A         | PRT_APHERESIS_PLATELETS                                                                | PRT APHERESIS PLATELETS          | A    | =W81253010700900 | =<E8340V00 | 1                | enabled           | ROOM_TEMPERATURE |
                | 336003       | 1           | Testing Customer | 2        | B         | PRT_APHERESIS_PLATELETS                                                                | PRT APHERESIS PLATELETS          | B    | =W81253010701000 | =<EB317V00 | 1                | enabled           | REFRIGERATED     |
                | 337001       | 1           | Testing Customer | 2        | AP        | RED_BLOOD_CELLS_LEUKOREDUCED                                                           | RED BLOOD CELLS LEUKOREDUCED     | AP   | =W81253010701200 | =<E5107V00 | 1                | enabled           | FROZEN           |
                | 446001       | 1           | Testing Customer | 2        | B         | PLASMA_MFG_NONINJECTABLE                                                               | PLASMA MFG NONINJECTABLE         | B    | =W03682515111200 | =<E5879V00 | 1                | enabled           | REFRIGERATED     |
                | 446002       | 1           | Testing Customer | 2        | B         | PLASMA_MFG_INJECTABLE                                                                  | PLASMA MFG INJECTABLE            | B    | =W01368251511300 | =<E0701V00 | 1                | enabled           | FROZEN           |
                | 446003       | 1           | Testing Customer | 2        | B         | CRYOPRECIPITATE                                                                        | CRYOPRECIPITATE                  | B    | =W03682515111100 | =<E5165V00 | 1                | enabled           | FROZEN           |
                | 446004       | 1           | Testing Customer | 2        | ANY       | PLASMA_TRANSFUSABLE                                                                    | PLASMA TRANSFUSABLE              | ANY  | =W03682515111500 | =<E2617V00 | 1                | enabled           | FROZEN           |
                | 479001       | 1           | Testing Customer | 5,8,4    | O,AB,ANY  | WASHED_APHERESIS_PLATELETS,WASHED_APHERESIS_PLATELETS,WASHED_APHERESIS_PLATELETS       | WASHED APHERESIS PLATELETS       | ANY  | =W03683047922100 | =<E3541V00 | 1                | enabled           | ROOM_TEMPERATURE |
                | 479002       | 1           | Testing Customer | 5,5      | A,O       | WASHED_PRT_APHERESIS_PLATELETS,WASHED_PRT_APHERESIS_PLATELETS                          | WASHED PRT APHERESIS PLATELETS   | O    | =W03683047922200 | =<E8697V00 | 1                | enabled           | ROOM_TEMPERATURE |
                | 479003       | 1           | Testing Customer | 4,5,2    | ANY,ON,AP | WASHED_RED_BLOOD_CELLS,WASHED_RED_BLOOD_CELLS,WASHED_RED_BLOOD_CELLS                   | WASHED RED BLOOD CELLS           | ANY  | =W03683047922300 | =<E5107V00 | 1                | enabled           | REFRIGERATED     |
                | 479004       | 1           | Testing Customer | 4,5,2    | BN,ABP,OP | WASHED_RED_BLOOD_CELLS,WASHED_RED_BLOOD_CELLS,WASHED_RED_BLOOD_CELLS                   | WASHED RED BLOOD CELLS           | BN   | =W03683047922400 | =<E4562V00 | 1                | enabled           | REFRIGERATED     |
                | 479001       | 1           | Testing Customer | 5,8,4    | ANY,AB,O  | WASHED_APHERESIS_PLATELETS,WASHED_APHERESIS_PLATELETS,WASHED_APHERESIS_PLATELETS       | WASHED APHERESIS PLATELETS       | ANY  | =W03683047922100 | =<E3541V00 | 1                | enabled           | ROOM_TEMPERATURE |
                | 479002       | 1           | Testing Customer | 5,5      | A,O       | WASHED_PRT_APHERESIS_PLATELETS,WASHED_PRT_APHERESIS_PLATELETS                          | WASHED PRT APHERESIS PLATELETS   | O    | =W03683047922200 | =<E8697V00 | 1                | enabled           | ROOM_TEMPERATURE |
                | 479003       | 1           | Testing Customer | 4,5,2    | ANY,ON,AP | WASHED_RED_BLOOD_CELLS,WASHED_RED_BLOOD_CELLS,WASHED_RED_BLOOD_CELLS                   | WASHED RED BLOOD CELLS           | ANY  | =W03683047922300 | =<E5107V00 | 1                | enabled           | REFRIGERATED     |
                | 479004       | 1           | Testing Customer | 4,5,2    | BN,ABP,OP | WASHED_RED_BLOOD_CELLS,WASHED_RED_BLOOD_CELLS,WASHED_RED_BLOOD_CELLS                   | WASHED RED BLOOD CELLS           | BN   | =W03683047922400 | =<E4562V00 | 1                | enabled           | REFRIGERATED     |







            @ui  @DIS-69
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
            And If the check digit configuration is enabled, the check digit field should disappear if I clean the Unit Number field.

            Examples:
                | Order Number | Customer ID | Customer Name    | Quantity | BloodType | ProductFamily                                                                          | Message Content | Message Type      | Family                       | Type | Code       | UN               | Check Digit Config | Digit | Inspection Config |
                | 110          | 1           | Testing Customer | 10,5,8   | A,B,O     | PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE                            |                 | not see any error | PLASMA TRANSFUSABLE          | A    | E7648V00   | W036824705327    | enabled            | 2     | enabled           |
                | 113          | 1           | Testing Customer | 10,5,8   | AP,BP,OP  | RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED |                 | not see any error | RED BLOOD CELLS LEUKOREDUCED | AP   | =<E0685V00 | =W81253010608900 | enabled            |       | enabled           |
                | 114          | 1           | Testing Customer | 10,5,8   | AP,BP,OP  | RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED |                 | not see any error | RED BLOOD CELLS LEUKOREDUCED | AP   | E0685V00   | W812530106090    | disabled           |       | enabled           |
                | 115          | 1           | Testing Customer | 10,5,8   | AP,BP,OP  | RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED |                 | not see any error | RED BLOOD CELLS LEUKOREDUCED | AP   | E0685V00   | W812530106090    | disabled           |       | disabled          |

        @ui @DIS-69
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
                | 111          | 1           | Testing Customer | 10,5,8   | AP,BP,OP  | RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED | Check Digit is invalid  | see an error message | RED BLOOD CELLS LEUKOREDUCED | AP   | E0685V00 | W812530106087 | enabled            | F     | disabled          |
                | 112          | 1           | Testing Customer | 10,5,8   | AP,BP,OP  | RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED | Check Digit is required | see an error message | RED BLOOD CELLS LEUKOREDUCED | AP   | E0685V00 | W812530106088 | enabled            |       | disabled          |


        @ui @DIS-202
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

        Rule: I should be able to see the discard reason containing up to 250 characters.
        @api @bug @DIS-312
        Scenario Outline: Complete shipment with unsuitable units.
            Given I have a shipment for order "<Order Number>" with the units "<Suitable UN>,<Unsuitable UN>" and product codes "<Suitable Code>,<Unsuitable Code>" "verified".
            And The second verification configuration is "enabled".
            When I request to complete a shipment.
            Then I should receive a "<Message Type>" message response "<Message>".
            And I should receive the product ineligible type "<Ineligible Type>" with message "<Ineligible Message>"
            Examples:
                | Order Number | Suitable Code | Suitable UN   | Unsuitable Code | Unsuitable UN | Message Type | Message                                                                               | Ineligible Type        | Ineligible Message                                                                                                                                                                                                                                                                                                                                          |
                | 1125          | E0685V00     | W822530106094 | E0713V00        | W036898786759 | CONFIRMATION | One or more products have changed status. You must rescan the products to be removed. | INVENTORY_IS_DISCARDED | This product is discarded and cannot be shippedLorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec. |


       Rule: I should not be able to mix quarantined and not quarantined products in the internal transfer shipment.
       @api @DIS-445
       Scenario Outline: Cannot mix quarantined and unquarantined products in shipment
           Given The shipment details are order Number "<Order Number>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities <Quantity>, Blood Types: "<BloodType>", Product Families "<ProductFamily>", Temperature Category as "<Category>", Shipment Type defined as "<Shipment Type>", Label Status as "<Label Status>" and Quarantined Products as "<Quarantined Products>" with the units "<Quarantined UN>,<Unquarantined UN>" and product codes "<Quarantined Code>,<Unquarantined Code>" "verified"
           And The second verification configuration is "enabled".
           When I request to complete a shipment.
           Then I should receive a "<Message Type>" message response "<Message>".
           And I should receive the unit "<Unquarantined UN>", product code "<Unquarantined Code>" flagged as "INVENTORY_IS_NOT_QUARANTINED"
           Examples:
                | Order Number | Customer ID | Customer Name     | Quantity | BloodType | ProductFamily       | Category | Shipment Type     | Label Status | Quarantined Products | Quarantined UN | Quarantined Code | Unquarantined UN | Unquarantined Code | Message Type | Message                                                                               |
                | 4450001      | DO1         | Distribution Only |  2       | ANY       | PLASMA_TRANSFUSABLE | FROZEN   | INTERNAL_TRANSFER | LABELED      |    true              | W036898445758  | E0701V00         |  W036898445760   |  E0701V00          | CONFIRMATION | One or more products have changed status. You must rescan the products to be removed. |




        Rule: I should be able to complete a shipment with labeled quarantine products when requested.
        Rule: I should see a success message when the shipment is completed.
        Rule: I should be able to fill and ship an imported product as part of an internal transfer order.
        @api @DIS-445 @DIS-454
        Scenario Outline: Complete shipment with labeled quarantine products
            Given The shipment details are order Number "<Order Number>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities <Quantity>, Blood Types: "<BloodType>", Product Families "<ProductFamily>", Temperature Category as "<Category>", Shipment Type defined as "<Shipment Type>", Label Status as "<Label Status>" and Quarantined Products as "<Quarantined Products>" with the units "<Quarantined UN>" and product codes "<Quarantined Code>" "verified"
            And The second verification configuration is "enabled".
 #           When I request to complete a shipment.
 #           Then I should receive a "<Message Type>" message response "<Message>".
            Examples:
                | Order Number | Customer ID | Customer Name     | Quantity | BloodType | ProductFamily                    | Category         | Shipment Type     | Label Status | Quarantined Products | Quarantined UN              | Quarantined Code  | Message Type | Message            |
                | 4450002      | DO1         | Distribution Only | 2        | ANY       | PLASMA_TRANSFUSABLE              | FROZEN           | INTERNAL_TRANSFER | LABELED      | true                 | W036898445758,W036898445759 | E0701V00,E0701V00 | success      | Shipment completed |
                | 4450003      | DO1         | Distribution Only | 1        | ANY       | APHERESIS_PLATELETS_LEUKOREDUCED | ROOM_TEMPERATURE | INTERNAL_TRANSFER | LABELED      | true                 | W036598786805               | E4140V00          | success      | Shipment completed |
                | 4450004      | DO1         | Distribution Only | 1        | BP        | PRT_APHERESIS_PLATELETS          | REFRIGERATED     | INTERNAL_TRANSFER | LABELED      | true                 | W036598786806               | EB317V00          | success      | Shipment completed |












