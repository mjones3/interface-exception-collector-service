Feature: View order details

    As a Distribution Technician,
    I want to be able to view the details of an order,
    so that I know the customer requirements for filling the products in an order.

    Background:
        Given I cleaned up from the database the orders with external ID starting with "ORDER".

    Scenario Outline: View order details
        Given I have a Biopro Order with externalId "<External ID>", Location Code "<LocationCode>", Priority "<Priority>", Status "<Status>", shipment type "<Shipment Type>", delivery type "<Delivery Type>", shipping method "<Shipping Method>", product category "<Product Category>", desired ship date "<Desired Date>", shipping customer code and name as "<Shipping Customer Code>" and "<Shipping Customer Name>", billing customer code and name as "<Billing Customer Code>" and "<Billing Customer Name>", and comments "<Order Comments>".
        And I have an order item with product family "<ProductFamily>", blood type "<BloodType>", quantity <Quantity>, and order item comments "<Item Comments>".
        And I am logged in the location "<LocationCode>".
        When I navigate to the order details page.
        Then I can see the order details card filled with the order details.
        And I can see the shipping information card filled with the shipping information.
        And I can see the billing information card filled with the billing information.
        And I can see the Product Details section filled with the product details.

        Examples:
            | External ID | LocationCode              | Priority  | Status      | ProductFamily       | BloodType | Quantity | Shipment Type | Shipping Method | Product Category | Desired Date | Shipping Customer Code | Shipping Customer Name     | Billing Customer Code | Billing Customer Name      | Order Comments     | Item Comments |
            | ORDER001    | MDL_HUB_1                 | STAT      | OPEN        | PLASMA_TRANSFUSABLE | AB        | 3        | CUSTOMER      | FEDEX           | FROZEN           | 2024-08-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Confirm when ready | Needed asap   |


