Feature: Prevent filling a shipment with unsuitable products
    As a distribution technician, I want to prevent filling a shipment with unsuitable products, so that I can avoid shipping the wrong products to the customer.

    Background:
        Given I cleaned up from the database, all shipments with order number "999771,999778,999764,999779,999765".
        And The check digit configuration is "disabled".

    @ui
    Scenario Outline: Entering an unsuitable product
        Given The shipment details are order Number "<orderNumber>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>".
        And I have received a shipment fulfillment request with above details.
        When I am on the Shipment Fulfillment Details page.
        And I choose to fill product of family "<Family Description>" and blood type "<BloodType>".
        And I add the unit "<UN>" with product code "<Code>".
        When I define visual inspection as "<Inspection>".
        Then I should see a "<Message Type>" message: "<Message>".
        And I should not see the unit "<UN>" with product code "<Code>" added to the filled products table.

        Examples:
            | UN               | Code       | Inspection   | Message                                                    | Message Type           | orderNumber | Customer ID | Customer Name | Quantity | BloodType | ProductFamily                                                                          | Family Description           |
            | W036898786756    | E0701V00   | Satisfactory | This product is expired and cannot be shipped              | Acknowledgment Message | 999771      | 999991      | Tampa         | 10,5,23  | AP,AN,OP  | PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE                            | PLASMA TRANSFUSABLE          |
            | =W03689878675800 | =<E0703V00 | Satisfactory | This product is quarantined and cannot be shipped          | Acknowledgment Message | 999778      | 999998      | Tampa         | 10,5,23  | AP,AN,OP  | RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED | RED BLOOD CELLS LEUKOREDUCED |
            | =W03689878676300 | =<E0703V00 | Satisfactory | This product is not in the inventory and cannot be shipped | Warning                | 999779      | 999998      | Tampa         | 10,5,23  | AP,AN,OP  | PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE                            | PLASMA TRANSFUSABLE          |


    @ui
    Scenario Outline: Filling a product which is already filled in another shipment
        Given The shipment details are order Number "<orderNumber>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>".
        And I have received a shipment fulfillment request with above details.
        And I have filled the shipment with the unit number "<UN>" and product code "<Code>" for order "<orderNumber>".
        And I am on the Shipment Fulfillment Details page.
        And I choose to fill product of family "<Family Description>" and blood type "<BloodType>".
        When I add the unit "<UN>" with product code "<Code>".
        And I define visual inspection as "<Inspection>".
        Then I should see a "Warning" message: "<Message>".
        Examples:
            | UN            | Code     | Inspection   | Message              | orderNumber | Customer ID | Customer Name | Quantity | BloodType | ProductFamily                                                                          | Family Description           |
            | W036898786810 | E4697V00 | Satisfactory | Product Already used | 999764      | 999991      | Tampa         | 10,5,23  | AP,AN,OP  | PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE                            | PLASMA TRANSFUSABLE          |
            | W812530106085 | E0685V00 | Satisfactory | Product Already used | 999765      | 999991      | Tampa         | 10,5,23  | ABP,AN,OP | RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED | RED BLOOD CELLS LEUKOREDUCED |
