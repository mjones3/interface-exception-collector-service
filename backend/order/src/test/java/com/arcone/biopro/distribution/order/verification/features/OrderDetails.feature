@ui @AOA-152 @AOA-128 @AOA-105 @AOA-240 @AOA-19 @AOA-197
Feature: View order details

    Background:
        Given I cleaned up from the database the orders with external ID starting with "ORDER".

        Rule: I should be able to see the available inventory for each line item.
        Rule: I should be able to see the internal transfer order details.
        Rule: The system should create a BioPro internal transfer order when an internal transfer order is received through the third-party application.
        Rule: I should be able to see the internal transfer order details.
        Rule: I should be able to see the quarantine status of the internal transfer order.
        Rule: I should be able to see the Label status of the internal transfer order.
        Rule: I should be able to see the shipment type information for the orders.
        Rule: The available inventory information should not be available for internal transfer orders.
        @DIS155 @DIS-121 @DIS-100 @DIS-97 @DIS-161 @DIS-253 @bug @DIS-295 @bug @DIS-321 @DIS-336 @DIS-337 @DIS-400 @DIS-440 @DIS-446
        Scenario Outline: View order details
            Given I have a Biopro Order with externalId "<External ID>", Location Code "<LocationCode>", Priority "<Priority>", Status "<Status>", shipment type "<Shipment Type>", delivery type "<Delivery Type>", shipping method "<Shipping Method>", product category "<Product Category>", desired ship date "<Desired Date>", shipping customer code and name as "<Shipping Customer Code>" and "<Shipping Customer Name>", billing customer code and name as "<Billing Customer Code>" and "<Billing Customer Name>", and comments "<Order Comments>", and Quarantined Products as "<Quarantined Products>", and Label Status as "<Label Status>".
            And I have 2 order items with product families "<ProductFamily>", blood types "<BloodType>", quantities "<Quantity>", and order item comments "<Item Comments>".
            And I am logged in the location "<LocationCode>".
            When I navigate to the order details page.
            Then I can see the order details card filled with the order details.
            And I can see the shipping information card filled with the shipping information.
            And I "<Can/Cannot>" see the billing information card filled with the billing information.
            And I can see the Product Details section filled with all the product details.
            And I "<Can/Cannot>" see the number of Available Inventories for each line item.
            And I can see the Temperature Category as "<Product Category>".
            And I can see the Shipment Type as "<Shipment Type>".
            And I "<Can/Cannot2>" see the Label Status as "<Label Status>".
            And I "<Can/Cannot2>" see the Quarantined Products as "<Quarantined Products>".

            Examples:
                | External ID    | LocationCode | Priority | Status | ProductFamily                                                     | BloodType | Quantity | Shipment Type     | Shipping Method | Product Category | Desired Date | Shipping Customer Code | Shipping Customer Name     | Billing Customer Code | Billing Customer Name      | Order Comments     | Item Comments                | Quarantined Products | Label Status | Can/Cannot | Can/Cannot2 |
                | ORDER001       | 123456789    | STAT     | OPEN   | PLASMA_TRANSFUSABLE, PLASMA_TRANSFUSABLE                          | AB, O     | 3, 2     | CUSTOMER          | FEDEX           | FROZEN           | 2024-08-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Confirm when ready | Needed asap, Another comment | <null>               | <null>       | can        | cannot      |
                | ORDER006       | 123456789    | STAT     | OPEN   | RED_BLOOD_CELLS_LEUKOREDUCED, RED_BLOOD_CELLS_LEUKOREDUCED        | AP, ON    | 5, 5     | CUSTOMER          | FEDEX           | REFRIGERATED     | 2024-08-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Confirm when ready | Needed asap, Another comment | <null>               | <null>       | can        | cannot      |
                | ORDER008       | 123456789    | STAT     | OPEN   | WHOLE_BLOOD,WHOLE_BLOOD                                           | AP, AN    | 5, 5     | CUSTOMER          | FEDEX           | REFRIGERATED     | 2024-08-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Confirm when ready | Needed asap, Another comment | <null>               | <null>       | can        | cannot      |
                | ORDER009       | 123456789    | STAT     | OPEN   | WHOLE_BLOOD_LEUKOREDUCED, WHOLE_BLOOD_LEUKOREDUCED                | ABN, ABP  | 5, 3     | CUSTOMER          | FEDEX           | REFRIGERATED     | 2024-08-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Confirm when ready | Needed asap, Another comment | <null>               | <null>       | can        | cannot      |
                | ORDER010       | 123456789    | STAT     | OPEN   | RED_BLOOD_CELLS, RED_BLOOD_CELLS                                  | ON, OP    | 10, 5    | CUSTOMER          | FEDEX           | REFRIGERATED     | 2024-08-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Confirm when ready | Needed asap, Another comment | <null>               | <null>       | can        | cannot      |
                | ORDERDIS336001 | 123456789    | STAT     | OPEN   | APHERESIS_PLATELETS_LEUKOREDUCED,APHERESIS_PLATELETS_LEUKOREDUCED | AB,A      | 2,50     | CUSTOMER          | FEDEX           | ROOM_TEMPERATURE | 2024-08-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Confirm when ready | Needed asap, Another comment | <null>               | <null>       | can        | cannot      |
                | ORDERDIS336002 | 123456789    | STAT     | OPEN   | PRT_APHERESIS_PLATELETS,PRT_APHERESIS_PLATELETS                   | AB,A      | 10,5     | CUSTOMER          | FEDEX           | ROOM_TEMPERATURE | 2024-08-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Confirm when ready | Needed asap, Another comment | <null>               | <null>       | can        | cannot      |
                | ORDERDIS336003 | 123456789    | STAT     | OPEN   | PRT_APHERESIS_PLATELETS,PRT_APHERESIS_PLATELETS                   | AB,O      | 10,7     | CUSTOMER          | FEDEX           | REFRIGERATED     | 2024-08-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Confirm when ready | Needed asap, Another comment | <null>               | <null>       | can        | cannot      |
                | ORDERDIS337001 | 123456789    | STAT     | OPEN   | RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED         | AP,OP     | 10,7     | CUSTOMER          | FEDEX           | FROZEN           | 2024-08-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Confirm when ready | Needed asap, Another comment | <null>               | <null>       | can        | cannot      |
                | ORDERDIS400001 | 123456789    | STAT     | OPEN   | RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED         | AP,OP     | 10,7     | INTERNAL_TRANSFER | FEDEX           | FROZEN           | 2024-08-20   | DO1                    | Distribution Only          | <null>                | <null>                     | Confirm when ready | Needed asap, Another comment | false                | LABELED      | cannot     | can         |
                | ORDERDIS400002 | 123456789    | STAT     | OPEN   | WHOLE_BLOOD,WHOLE_BLOOD                                           | ANY,ANY   | 10,7     | INTERNAL_TRANSFER | FEDEX           | REFRIGERATED     | 2024-08-20   | DO1                    | Distribution Only          | <null>                | <null>                     | Confirm when ready | Needed asap, Another comment | true                 | UNLABELED    | cannot     | can         |
                | ORDERDIS446001 | 123456789    | STAT     | OPEN   | PLASMA_MFG_NONINJECTABLE,PLASMA_MFG_NONINJECTABLE                 | A,O       | 10,7     | CUSTOMER          | FEDEX           | REFRIGERATED     | 2025-08-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Confirm when ready | Needed asap, Another comment | <null>               | <null>       | can        | cannot      |
                | ORDERDIS446002 | 123456789    | STAT     | OPEN   | PLASMA_MFG_INJECTABLE, PLASMA_MFG_INJECTABLE                      | A,O       | 10,7     | CUSTOMER          | FEDEX           | FROZEN           | 2025-08-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Confirm when ready | Needed asap, Another comment | <null>               | <null>       | can        | cannot      |
                | ORDERDIS446003 | 123456789    | STAT     | OPEN   | CRYOPRECIPITATE, CRYOPRECIPITATE                                  | ANY,ANY   | 10,7     | CUSTOMER          | FEDEX           | FROZEN           | 2025-08-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Confirm when ready | Needed asap, Another comment | <null>               | <null>       | can        | cannot      |


    Rule: I should be able to create the order fulfillment request when the pick list is generated.Rule: The BioPro order status must be updated to InProgress when an order is being fulfilled.
            Rule: I should not be able to generate multiple pick lists for the same order.
            Rule: I should be able to view or reprint the pick list that was previously generated.
            Rule: I should be able to see the short-dated products if applicable.
            Rule: I should be able to generate pick list for internal transfer order.
            Rule: The shipment details for cryo and cryo-reduced plasma should be generated.
            @DIS-121 @DIS-100 @DIS-253 @bug @DIS-321 @DIS-442 @DIS-446
            Scenario Outline: Generate pick list no short date products
            Given I have a Biopro Order with externalId "<External ID>", Location Code "<LocationCode>", Priority "<Priority>", Status "<Status>", shipment type "<Shipment Type>", delivery type "<Delivery Type>", shipping method "<Shipping Method>", product category "<Product Category>", desired ship date "<Desired Date>", shipping customer code and name as "<Shipping Customer Code>" and "<Shipping Customer Name>", billing customer code and name as "<Billing Customer Code>" and "<Billing Customer Name>", and comments "<Order Comments>", and Quarantined Products as "<Quarantined Products>", and Label Status as "<Label Status>".
            And I have an order item with product family "<ProductFamily>", blood type "<BloodType>", quantity <Quantity>, and order item comments "<Item Comments>".
            And I am logged in the location "<LocationCode>".
            And I navigate to the order details page.
            When I choose to generate the Pick List.
            Then I can see the pick list details.
            And I have received a shipment created event.
            And I "<Short Date>" see the short date product details.
            When I close the pick list.
            And I should see the shipment details.
            And I should see an option to navigate to the shipment details page.
            And The order status is "IN PROGRESS".
            And I choose to generate the Pick List.
            Then I can see the pick list details.
            When I close the pick list.
            And I should not see multiple shipments generated.

            Examples:
                | External ID    | LocationCode | Priority | Status | ProductFamily                | BloodType | Quantity | Shipment Type     | Shipping Method | Product Category | Desired Date | Shipping Customer Code | Shipping Customer Name     | Billing Customer Code | Billing Customer Name      | Order Comments     | Item Comments | Short Date | Label Status | Quarantined Products |
                | ORDER002       | 123456789    | STAT     | OPEN   | PLASMA_TRANSFUSABLE          | AB        | 3        | CUSTOMER          | FEDEX           | FROZEN           | 2024-08-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Confirm when ready | Needed asap   | CAN        | LABELED      | false                |
                | ORDER003       | 123456789    | STAT     | OPEN   | PLASMA_TRANSFUSABLE          | B         | 8        | CUSTOMER          | FEDEX           | FROZEN           | 2024-09-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Send asap          | Needed asap   | CANNOT     | LABELED      | false                |
                | ORDER007       | 123456789    | STAT     | OPEN   | RED_BLOOD_CELLS_LEUKOREDUCED | ABP       | 3        | CUSTOMER          | FEDEX           | REFRIGERATED     | 2024-08-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Confirm when ready | Needed asap   | CAN        | LABELED      | false                |
                | ORDER011       | 123456789    | STAT     | OPEN   | WHOLE_BLOOD                  | AN        | 8        | CUSTOMER          | FEDEX           | REFRIGERATED     | 2024-09-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Send asap          | Needed asap   | CANNOT     | LABELED      | false                |
                | ORDER012       | 123456789    | STAT     | OPEN   | WHOLE_BLOOD_LEUKOREDUCED     | BP        | 8        | CUSTOMER          | FEDEX           | REFRIGERATED     | 2024-09-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Send asap          | Needed asap   | CANNOT     | LABELED      | false                |
                | ORDER013       | 123456789    | STAT     | OPEN   | RED_BLOOD_CELLS              | BN        | 8        | CUSTOMER          | FEDEX           | REFRIGERATED     | 2024-09-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Send asap          | Needed asap   | CANNOT     | LABELED      | false                |
                | ORDERDIS442001 | 123456789    | STAT     | OPEN   | RED_BLOOD_CELLS_LEUKOREDUCED | AP        | 10       | INTERNAL_TRANSFER | FEDEX           | FROZEN           | 2024-08-20   | DO1                    | Distribution Only          | <null>                | <null>                     | Confirm when ready | Needed asap   | CANNOT     | LABELED      | false                |
                | ORDERDIS442002 | 123456789    | STAT     | OPEN   | WHOLE_BLOOD                  | ANY       | 10       | INTERNAL_TRANSFER | FEDEX           | REFRIGERATED     | 2024-08-20   | DO1                    | Distribution Only          | <null>                | <null>                     | Confirm when ready | Needed asap   | CANNOT     | UNLABELED    | true                 |
                | ORDERDIS446001 | 123456789    | STAT     | OPEN   | CRYOPRECIPITATE              | A         | 8        | CUSTOMER          | FEDEX           | FROZEN           | 2025-09-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Send asap          | Needed asap   | CANNOT     | LABELED      | false                |
                | ORDERDIS447002 | 123456789    | STAT     | OPEN   | PLASMA_MFG_NONINJECTABLE     | A         | 8        | CUSTOMER          | FEDEX           | REFRIGERATED     | 2025-09-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Send asap          | Needed asap   | CANNOT     | LABELED      | false                |
                | ORDERDIS447003 | 123456789    | STAT     | OPEN   | PLASMA_MFG_INJECTABLE        | A         | 8        | CUSTOMER          | FEDEX           | FROZEN           | 2025-09-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Send asap          | Needed asap   | CANNOT     | LABELED      | false                |



    Rule: I should be able to see the number of products that have been shipped for each order.
        Rule: I should be able to see the number of products that have been shipped for each line item in an order.
        Rule: The shipment status must be updated to Completed when the shipment completed event is generated on order details page.
        Rule: The order status must remain In Progress status if the order is partially fulfilled.
        @DIS-141 @DIS-99 @DIS-98 @DIS-201
            Scenario Outline: Progress of the Order Fulfillment
            Given I have a Biopro Order with externalId "<External ID>", Location Code "<LocationCode>", Priority "<Priority>", Status "<Status>", shipment type "<Shipment Type>", delivery type "<Delivery Type>", shipping method "<Shipping Method>", product category "<Product Category>", desired ship date "<Desired Date>", shipping customer code and name as "<Shipping Customer Code>" and "<Shipping Customer Name>", billing customer code and name as "<Billing Customer Code>" and "<Billing Customer Name>", and comments "<Order Comments>", and Quarantined Products as "<null>", and Label Status as "<null>".
            And I have <Items Quantity> order items with product families "<ProductFamily>", blood types "<BloodType>", quantities "<Quantity>", and order item comments "<Item Comments>".
            And I have received a shipment completed event.
            And I am logged in the location "<LocationCode>".
            When I navigate to the order details page.
            Then I can see the pending log of products is updated with <Expected Quantity> product(s) out of <Total Quantity>.
            And I can see the Filled Products section filled with "<Filled Quantity>" shipped products.
            And I can see the shipment status as "COMPLETED".
            And The order status is "IN PROGRESS".

            Examples:
                | External ID | LocationCode | Priority | Status      | Items Quantity | ProductFamily                            | BloodType | Quantity | Filled Quantity | Expected Quantity | Total Quantity | Shipment Type | Shipping Method | Product Category | Desired Date | Shipping Customer Code | Shipping Customer Name     | Billing Customer Code | Billing Customer Name      | Order Comments     | Item Comments                |
                | ORDER004    | 123456789    | STAT     | IN_PROGRESS | 2              | PLASMA_TRANSFUSABLE, PLASMA_TRANSFUSABLE | AB, O     | 3, 2     | 1, 0            | 1                 | 5              | CUSTOMER      | FEDEX           | FROZEN           | 2030-12-31   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Confirm when ready | Needed asap, Another comment |



        Rule: The order status must be updated to Completed status if all the products requested in an order are fulfilled.
        Rule: The progress status bar should not be shown when the order is completed.
            @DIS-99
            Scenario Outline: Order Completed
            Given I have a Biopro Order with externalId "<External ID>", Location Code "<LocationCode>", Priority "<Priority>", Status "<Status>", shipment type "<Shipment Type>", delivery type "<Delivery Type>", shipping method "<Shipping Method>", product category "<Product Category>", desired ship date "<Desired Date>", shipping customer code and name as "<Shipping Customer Code>" and "<Shipping Customer Name>", billing customer code and name as "<Billing Customer Code>" and "<Billing Customer Name>", and comments "<Order Comments>", and Quarantined Products as "<null>", and Label Status as "<null>".
            And I have <Items Quantity> order items with product families "<ProductFamily>", blood types "<BloodType>", quantities "<Quantity>", and order item comments "<Item Comments>".
            And I have received a shipment completed event.
            And I am logged in the location "<LocationCode>".
            When I navigate to the order details page.
            Then I cannot see the progress status bar.
            And I can see the shipment status as "COMPLETED".
            And The order status is "COMPLETED".

            Examples:
                | External ID | LocationCode | Priority | Status      | Items Quantity | ProductFamily       | BloodType | Quantity | Shipment Type | Shipping Method | Product Category | Desired Date | Shipping Customer Code | Shipping Customer Name     | Billing Customer Code | Billing Customer Name      | Order Comments     | Item Comments |
                | ORDER005    | 123456789    | STAT     | IN_PROGRESS | 1              | PLASMA_TRANSFUSABLE | AB        | 1        | CUSTOMER      | FEDEX           | FROZEN           | 2024-08-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Confirm when ready | Needed asap   |

            Scenario: Cleanup database
                Given I cleaned up from the database the orders with external ID starting with "ORDER".
