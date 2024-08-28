Feature: View order details

    As a Distribution Technician,
    I want to be able to view the details of an order,
    so that I know the customer requirements for filling the products in an order.

    Background:
        Given I cleaned up from the database the orders with external ID starting with "ORDER".

    Rule: I should be able to see the available inventory for each line item.
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
                | External ID | LocationCode | Priority | Status | ProductFamily                            | BloodType | Quantity | Shipment Type | Shipping Method | Product Category | Desired Date | Shipping Customer Code | Shipping Customer Name     | Billing Customer Code | Billing Customer Name      | Order Comments     | Item Comments                |
                | ORDER001    | MDL_HUB_1    | STAT     | OPEN   | PLASMA_TRANSFUSABLE, PLASMA_TRANSFUSABLE | AB, O     | 3, 2     | CUSTOMER      | FEDEX           | FROZEN           | 2024-08-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Confirm when ready | Needed asap, Another comment |


    Rule: I should be able to create the order fulfillment request when the pick list is generated.Rule: The BioPro order status must be updated to InProgress when an order is being fulfilled.
        Rule: I should not be able to generate multiple pick lists for the same order.
    Rule: I should be able to view or reprint the pick list that was previously generated.
        Rule: I should be able to see the short-dated products if applicable.
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
                | External ID | LocationCode | Priority | Status | ProductFamily       | BloodType | Quantity | Shipment Type | Shipping Method | Product Category | Desired Date | Shipping Customer Code | Shipping Customer Name     | Billing Customer Code | Billing Customer Name      | Order Comments     | Item Comments | Short Date |
                | ORDER002    | MDL_HUB_1    | STAT     | OPEN   | PLASMA_TRANSFUSABLE | AB        | 3        | CUSTOMER      | FEDEX           | FROZEN           | 2024-08-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Confirm when ready | Needed asap   | CAN        |
                | ORDER003    | MDL_HUB_1    | STAT     | OPEN   | PLASMA_TRANSFUSABLE | B         | 8        | CUSTOMER      | FEDEX           | FROZEN           | 2024-09-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Send asap          | Needed asap   | CANNOT     |



