@ui
Feature: View order details

    As a Distribution Technician,
    I want to be able to view the details of an order,
    so that I know the customer requirements for filling the products in an order.

    Background:
        Given I cleaned up from the database the orders with external ID starting with "ORDER".

        Rule: I should be able to see the available inventory for each line item.
        @DIS155 @DIS-121 @DIS-100 @DIS-97
        Scenario Outline: View order details
            Given I have a Biopro Order with externalId "<External ID>", Location Code "<LocationCode>", Priority "<Priority>", Status "<Status>", shipment type "<Shipment Type>", delivery type "<Delivery Type>", shipping method "<Shipping Method>", product category "<Product Category>", desired ship date "<Desired Date>", shipping customer code and name as "<Shipping Customer Code>" and "<Shipping Customer Name>", billing customer code and name as "<Billing Customer Code>" and "<Billing Customer Name>", and comments "<Order Comments>".
            And I have 2 order items with product families "<ProductFamily>", blood types "<BloodType>", quantities "<Quantity>", and order item comments "<Item Comments>".
            And I am logged in the location "<LocationCode>".
            When I navigate to the order details page.
            Then I can see the order details card filled with the order details.
            And I can see the shipping information card filled with the shipping information.
            And I can see the billing information card filled with the billing information.
            And I can see the Product Details section filled with all the product details.
            And I can see the number of Available Inventories for each line item.


            Examples:
                | External ID | LocationCode | Priority | Status | ProductFamily                                              | BloodType | Quantity | Shipment Type | Shipping Method | Product Category | Desired Date | Shipping Customer Code | Shipping Customer Name     | Billing Customer Code | Billing Customer Name      | Order Comments     | Item Comments                |
                | ORDER001    | 123456789    | STAT     | OPEN   | PLASMA_TRANSFUSABLE, PLASMA_TRANSFUSABLE                   | AB, O     | 3, 2     | CUSTOMER      | FEDEX           | FROZEN           | 2024-08-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Confirm when ready | Needed asap, Another comment |
                | ORDER006    | 123456789    | STAT     | OPEN   | RED_BLOOD_CELLS_LEUKOREDUCED, RED_BLOOD_CELLS_LEUKOREDUCED | AP, ON    | 5, 5     | CUSTOMER      | FEDEX           | REFRIGERATED     | 2024-08-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Confirm when ready | Needed asap, Another comment |


            Rule: I should be able to create the order fulfillment request when the pick list is generated.Rule: The BioPro order status must be updated to InProgress when an order is being fulfilled.
            Rule: I should not be able to generate multiple pick lists for the same order.
            Rule: I should be able to view or reprint the pick list that was previously generated.
            Rule: I should be able to see the short-dated products if applicable.
            @DIS-121 @DIS-100
            Scenario Outline: Generate pick list no short date products
            Given I have a Biopro Order with externalId "<External ID>", Location Code "<LocationCode>", Priority "<Priority>", Status "<Status>", shipment type "<Shipment Type>", delivery type "<Delivery Type>", shipping method "<Shipping Method>", product category "<Product Category>", desired ship date "<Desired Date>", shipping customer code and name as "<Shipping Customer Code>" and "<Shipping Customer Name>", billing customer code and name as "<Billing Customer Code>" and "<Billing Customer Name>", and comments "<Order Comments>".
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
                | External ID | LocationCode | Priority | Status | ProductFamily                | BloodType | Quantity | Shipment Type | Shipping Method | Product Category | Desired Date | Shipping Customer Code | Shipping Customer Name     | Billing Customer Code | Billing Customer Name      | Order Comments     | Item Comments | Short Date |
                | ORDER002    | 123456789    | STAT     | OPEN   | PLASMA_TRANSFUSABLE          | AB        | 3        | CUSTOMER      | FEDEX           | FROZEN           | 2024-08-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Confirm when ready | Needed asap   | CAN        |
                | ORDER003    | 123456789    | STAT     | OPEN   | PLASMA_TRANSFUSABLE          | B         | 8        | CUSTOMER      | FEDEX           | FROZEN           | 2024-09-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Send asap          | Needed asap   | CANNOT     |
                | ORDER007    | 123456789    | STAT     | OPEN   | RED_BLOOD_CELLS_LEUKOREDUCED | ABP       | 3        | CUSTOMER      | FEDEX           | REFRIGERATED     | 2024-08-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Confirm when ready | Needed asap   | CAN        |



        Rule: I should be able to see the number of products that have been shipped for each order.
        Rule: I should be able to see the number of products that have been shipped for each line item in an order.
        Rule: The shipment status must be updated to Completed when the shipment completed event is generated on order details page.
        Rule: The order status must remain In Progress status if the order is partially fulfilled.
        @DIS-141 @DIS-99 @DIS-98 @DIS-201
            Scenario Outline: Progress of the Order Fulfillment
            Given I have a Biopro Order with externalId "<External ID>", Location Code "<LocationCode>", Priority "<Priority>", Status "<Status>", shipment type "<Shipment Type>", delivery type "<Delivery Type>", shipping method "<Shipping Method>", product category "<Product Category>", desired ship date "<Desired Date>", shipping customer code and name as "<Shipping Customer Code>" and "<Shipping Customer Name>", billing customer code and name as "<Billing Customer Code>" and "<Billing Customer Name>", and comments "<Order Comments>".
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
                | ORDER004    | 123456789    | STAT     | IN_PROGRESS | 2              | PLASMA_TRANSFUSABLE, PLASMA_TRANSFUSABLE | AB, O     | 3, 2     | 1, 0            | 1                 | 5              | CUSTOMER      | FEDEX           | FROZEN           | 2024-08-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Confirm when ready | Needed asap, Another comment |



        Rule: The order status must be updated to Completed status if all the products requested in an order are fulfilled.
        Rule: The progress status bar should not be shown when the order is completed.
            @DIS-99
            Scenario Outline: Order Completed
            Given I have a Biopro Order with externalId "<External ID>", Location Code "<LocationCode>", Priority "<Priority>", Status "<Status>", shipment type "<Shipment Type>", delivery type "<Delivery Type>", shipping method "<Shipping Method>", product category "<Product Category>", desired ship date "<Desired Date>", shipping customer code and name as "<Shipping Customer Code>" and "<Shipping Customer Name>", billing customer code and name as "<Billing Customer Code>" and "<Billing Customer Name>", and comments "<Order Comments>".
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
