@ui @AOA-40 @AOA-6
Feature: Prevent filling a shipment with unsuitable products
    As a distribution technician, I want to prevent filling a shipment with unsuitable products, so that I can avoid shipping the wrong products to the customer.

    Background:
        Given I cleaned up from the database, all shipments with order number "999771,999778,999764,999779,999765,999766,999767,999768,999769,999770".

    @DIS-125 @DIS-78 @DIS-56 @DIS-194 @DIS-162
    Scenario Outline: Entering an unsuitable product
        Given The shipment details are order Number "<orderNumber>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>".
        And I have received a shipment fulfillment request with above details.
        When I am on the Shipment Fulfillment Details page.
        And I choose to fill product of family "<Family Description>" and blood type "<BloodType>".
        And I add the unit "<UN>" with product code "<Code>".
        When I define visual inspection as "<Inspection>", if needed.
        Then I should see a "<Message Type>" message: "<Message>".
        And I should not see the unit "<UN>" with product code "<Code>" added to the filled products table.

        Examples:
            | UN               | Code       | Inspection   | Message                                                                       | Message Type           | orderNumber | Customer ID | Customer Name | Quantity | BloodType | ProductFamily                                                                          | Family Description           |
            | =W03689878675600 | =<E0701V00 | Satisfactory | This product is expired and has been discarded. Place in biohazard container. | Acknowledgment Message | 999771      | 999991      | Tampa         | 10,5,23  | AP,AN,OP  | PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE                            | PLASMA TRANSFUSABLE          |
            | =W03689878675800 | =<E0703V00 | Satisfactory | This product is quarantined and cannot be shipped                             | Acknowledgment Message | 999778      | 999998      | Tampa         | 10,5,23  | AP,AN,OP  | RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED | RED BLOOD CELLS LEUKOREDUCED |
            | =W03689878676300 | =<E0703V00 | Satisfactory | This product is not in the inventory and cannot be shipped                    | Warning                | 999779      | 999998      | Tampa         | 10,5,23  | AP,AN,OP  | PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE                            | PLASMA TRANSFUSABLE          |


    @DIS-78 @DIS-56
    Scenario Outline: Filling a product which is already filled in another shipment
        Given The shipment details are order Number "<orderNumber>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>".
        And I have received a shipment fulfillment request with above details.
        And I have filled the shipment with the unit number "<UN>" and product code "<Code>" for order "<orderNumber>".
        And I am on the Shipment Fulfillment Details page.
        And I choose to fill product of family "<Family Description>" and blood type "<BloodType>".
        When I add the unit "<UN>" with product code "<Code>".
        And I define visual inspection as "<Inspection>", if needed.
        Then I should see a "Warning" message: "<Message>".
        Examples:
            | UN               | Code       | Inspection   | Message              | orderNumber | Customer ID | Customer Name | Quantity | BloodType | ProductFamily                                                                          | Family Description           |
            | =W03689878681000 | =<E4697V00 | Satisfactory | Product Already used | 999764      | 999991      | Tampa         | 10,5,23  | AP,AN,OP  | PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE                            | PLASMA TRANSFUSABLE          |
            | =W81253010608800 | =<E0685V00 | Satisfactory | Product Already used | 999765      | 999991      | Tampa         | 10,5,23  | AP,AN,OP  | RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED | RED BLOOD CELLS LEUKOREDUCED |


    Rule: I should not be able fill orders with ineligible Products.
        @api @DIS-254
        Scenario Outline: Fill shipments with ineligible Products.
            Given The shipment details are order Number "<Order Number>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>".
            And The visual inspection configuration is "enabled".
            And I have received a shipment fulfillment request with above details.
            When I fill an unsuitable product with the unit number "<UN>", product code "<Code>", and visual Inspection "<Inspection>".
            Then I should receive a "<Message Type>" message "<Message>".
            And The product unit number "<UN>" and product code "<Code>" should not be packed in the shipment.
            Examples:
                | Order Number | Customer ID | Customer Name    | Quantity | BloodType | ProductFamily                | UN            | Code     | Inspection     | Message                                                                                                   | Message Type |
                | 999766       | 1           | Testing Customer | 10       | ANY       | PLASMA_TRANSFUSABLE          | W036898786756 | E0701V00 | SATISFACTORY   | This product is expired and has been discarded. Place in biohazard container.                             | INFO         |
                | 999767       | 1           | Testing Customer | 5        | ANY       | RED_BLOOD_CELLS_LEUKOREDUCED | W036898786758 | E0703V00 | SATISFACTORY   | This product is quarantined and cannot be shipped                                                         | INFO         |
                | 999768       | 1           | Testing Customer | 5        | ABP       | WHOLE_BLOOD_LEUKOREDUCED     | W812530106097 | E0023V00 | SATISFACTORY   | Product Family does not match                                                                             | WARN         |
                | 999769       | 1           | Testing Customer | 5        | BP        | WHOLE_BLOOD                  | W812530106097 | E0023V00 | SATISFACTORY   | Blood type does not match                                                                                 | WARN         |
                | 999770       | 1           | Testing Customer | 5        | ON        | RED_BLOOD_CELLS              | W812530106098 | E0167V00 | UNSATISFACTORY | This product has been discarded for failed visual inspection in the system. Place in biohazard container. | WARN         |
