@AOA-40 @AOA-6 @AOA-152 @AOA-128 @AOA-105 @AOA-240 @AOA-19 @AOA-197
Feature: Prevent filling a shipment with unsuitable products
    As a distribution technician, I want to prevent filling a shipment with unsuitable products, so that I can avoid shipping the wrong products to the customer.

    Background:
        Given I cleaned up from the database, all shipments with order number "999771,999778,999764,999779,999765,999766,999767,999768,999769,999770,999771,999772,999773,999774,999775,999776,4440009,45200007,45200008,45200009,45200010,45200011,45200012,45200013,45200014,45200015,44600011,44600012,44600013,44600014".

    @ui @DIS-125 @DIS-78 @DIS-56 @DIS-194 @DIS-162
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
            | UN               | Code           | Inspection   | Message                                                                       | Message Type           | orderNumber | Customer ID | Customer Name | Quantity | BloodType | ProductFamily                                                                          | Family Description           |
            | =W03689878675600 | =<E0707V00     | Satisfactory | This product is expired and has been discarded. Place in biohazard container. | Acknowledgment Message | 999771      | 999991      | Tampa         | 10,5,23  | AP,AN,OP  | PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE                            | PLASMA TRANSFUSABLE          |
            | =W03689878675800 | =<GENERIC_CODE | Satisfactory | This product is quarantined and cannot be shipped                             | Acknowledgment Message | 999778      | 999998      | Tampa         | 10,5,23  | AP,AN,OP  | RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED | RED BLOOD CELLS LEUKOREDUCED |
            | =W03689878676300 | =<E0703V00     | Satisfactory | This product is not in the inventory and cannot be shipped                    | Warning                | 999779      | 999998      | Tampa         | 10,5,23  | AP,AN,OP  | PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE                            | PLASMA TRANSFUSABLE          |


    @ui @DIS-78 @DIS-56
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
        Rule: I should not be able to fill the same internal order with both quarantined and not quarantined products.
        Rule: I should notified when I select and expired product.
        Rule: I should not be able to enter a unit number that doesn’t exist in the system.
        Rule: I should not be able to enter a unit number if it is part of another order or shipment.
        Rule: I should not be able to enter a product that has already shipped.
        Rule: I should not be able to enter a product that is not in the same product family as the internal transfer order.
        Rule: I should not be able to enter a product that does not have a blood type that matches the internal transfer order.
        Rule: I should not be able to enter a product if the temperature category does not match.
        Rule: I should be able to fill an internal transfer order with quarantined products as requested.
        Rule: I should not be able to fill the same internal order with both quarantined and not quarantined products.
        Rule: I should not be able to select an unlabeled product when the the internal transfer is for labeled products.
        Rule: I should not be able to fill orders with unacceptable cryo and cryo-reduced plasma Products.
        @api @DIS-254 @bug @DIS-321 @DIS-336 @DIS-337 @DIS-444 @DIS-452 @DIS-446
        Scenario Outline: Fill shipments with ineligible Products.
            Given The shipment details are order Number "<Order Number>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>", Temperature Category as "<Temperature Category>", Shipment Type defined as "<Shipment Type>", Label Status as "<Label Status>" and Quarantined Products as "<Quarantined Products>".
            And The visual inspection configuration is "enabled".
            And I have received a shipment fulfillment request with above details.
            When I fill an unsuitable product with the unit number "<UN>", product code "<Code>", and visual Inspection "<Inspection>".
            Then I should receive a "<Message Type>" message "<Message>".
            And The product unit number "<UN>" and product code "<Code>" should not be packed in the shipment.
            Examples:
                | Order Number | Customer ID | Customer Name     | Quantity | BloodType | ProductFamily                    | Temperature Category | Shipment Type     | Label Status | Quarantined Products | UN            | Code       | Inspection     | Message                                                                                                   | Message Type |
                | 999766       | 1           | Testing Customer  | 10       | ANY       | PLASMA_TRANSFUSABLE              | FROZEN               | CUSTOMER          | LABELED      | false                | W036898786756 | E0707V00   | SATISFACTORY   | This product is expired and has been discarded. Place in biohazard container.                             | INFO         |
                | 999767       | 1           | Testing Customer  | 5        | ANY       | RED_BLOOD_CELLS_LEUKOREDUCED     | FROZEN               | CUSTOMER          | LABELED      | false                | W036898786758 | E0707V00   | SATISFACTORY   | This product is quarantined and cannot be shipped                                                         | INFO         |
                | 999768       | 1           | Testing Customer  | 5        | ABP       | WHOLE_BLOOD_LEUKOREDUCED         | FROZEN               | CUSTOMER          | LABELED      | false                | W812530107002 | E0023V00   | SATISFACTORY   | Product Family does not match                                                                             | WARN         |
                | 999769       | 1           | Testing Customer  | 5        | BP        | WHOLE_BLOOD                      | FROZEN               | CUSTOMER          | LABELED      | false                | W812530107002 | E0023V00   | SATISFACTORY   | Blood type does not match                                                                                 | WARN         |
                | 999770       | 1           | Testing Customer  | 5        | ON        | RED_BLOOD_CELLS                  | FROZEN               | CUSTOMER          | LABELED      | false                | W812530107003 | E0167V00   | UNSATISFACTORY | This product has been discarded for failed visual inspection in the system. Place in biohazard container. | WARN         |
                | 999771       | 1           | Testing Customer  | 5        | ON        | PLASMA_TRANSFUSABLE              | FROZEN               | CUSTOMER          | LABELED      | false                | W812530107004 | E2457V00   | SATISFACTORY   | Temperature Category does not match                                                                       | WARN         |
                | 999772       | 1           | Testing Customer  | 5        | ABP       | PLASMA_TRANSFUSABLE              | FROZEN               | CUSTOMER          | LABELED      | false                | W812530107005 | E2469V00   | SATISFACTORY   | Temperature Category does not match                                                                       | WARN         |
                | 999773       | 1           | Testing Customer  | 5        | AP        | APHERESIS_PLATELETS_LEUKOREDUCED | FROZEN               | CUSTOMER          | LABELED      | false                | W812530107008 | EA141V00   | SATISFACTORY   | Temperature Category does not match                                                                       | WARN         |
                | 999774       | 1           | Testing Customer  | 5        | AP        | PRT_APHERESIS_PLATELETS          | FROZEN               | CUSTOMER          | LABELED      | false                | W812530107009 | E8340V00   | SATISFACTORY   | Temperature Category does not match                                                                       | WARN         |
                | 999775       | 1           | Testing Customer  | 5        | BP        | PRT_APHERESIS_PLATELETS          | FROZEN               | CUSTOMER          | LABELED      | false                | W812530107010 | EB317V00   | SATISFACTORY   | Temperature Category does not match                                                                       | WARN         |
                | 999776       | 1           | Testing Customer  | 5        | AP        | RED_BLOOD_CELLS_LEUKOREDUCED     | REFRIGERATED         | CUSTOMER          | LABELED      | false                | W812530107012 | E5107V00   | SATISFACTORY   | Temperature Category does not match                                                                       | WARN         |
                | 4440009      | DO1         | Distribution Only | 5        | A         | APHERESIS_PLATELETS_LEUKOREDUCED | ROOM_TEMPERATURE     | INTERNAL_TRANSFER | LABELED      | true                 | W812530444002 | EA007V00   | SATISFACTORY   | Shipment can only contain quarantined products                                                            | WARN         |
                | 45200007     | DO1         | Distribution Only | 10       | ANY       | PLASMA_TRANSFUSABLE              | FROZEN               | INTERNAL_TRANSFER | UNLABELED    | false                | W036825158911 | BAG-A      | SATISFACTORY   | This product is expired and has been discarded. Place in biohazard container.                             | INFO         |
                | 45200008     | DO1         | Distribution Only | 5        | ANY       | PLASMA_TRANSFUSABLE              | FROZEN               | INTERNAL_TRANSFER | UNLABELED    | false                | W036825158907 | BAG-B      | SATISFACTORY   | This product is quarantined and cannot be shipped                                                         | INFO         |
                | 45200009     | DO1         | Distribution Only | 5        | ON        | WHOLE_BLOOD_LEUKOREDUCED         | FROZEN               | INTERNAL_TRANSFER | UNLABELED    | false                | W036825158906 | E4532V00   | SATISFACTORY   | Product Family does not match                                                                             | WARN         |
                | 45200010     | DO1         | Distribution Only | 5        | BP        | RED_BLOOD_CELLS_LEUKOREDUCED     | FROZEN               | INTERNAL_TRANSFER | UNLABELED    | false                | W036825158906 | E4532V00   | SATISFACTORY   | Blood type does not match                                                                                 | WARN         |
                | 45200011     | DO1         | Distribution Only | 5        | ON        | RED_BLOOD_CELLS_LEUKOREDUCED     | FROZEN               | INTERNAL_TRANSFER | UNLABELED    | false                | W036825158912 | E0707V00   | UNSATISFACTORY | This product has been discarded for failed visual inspection in the system. Place in biohazard container. | WARN         |
                | 45200012     | DO1         | Distribution Only | 5        | AP        | PLASMA_TRANSFUSABLE              | REFRIGERATED         | INTERNAL_TRANSFER | UNLABELED    | false                | W036825861249 | PLASAPHP_D | SATISFACTORY   | Temperature Category does not match                                                                       | WARN         |
                | 45200014     | DO1         | Distribution Only | 5        | A         | APHERESIS_PLATELETS_LEUKOREDUCED | ROOM_TEMPERATURE     | INTERNAL_TRANSFER | LABELED      | false                | W036825158909 | EA007V00   | SATISFACTORY   | This product is not labeled and cannot be shipped                                                         | WARN         |
                | 45200013     | DO1         | Distribution Only | 5        | A         | APHERESIS_PLATELETS_LEUKOREDUCED | ROOM_TEMPERATURE     | INTERNAL_TRANSFER | LABELED      | true                 | W036825158910 | EA007V00   | SATISFACTORY   | Shipment can only contain quarantined products                                                            | WARN         |
                | 45200015     | DO1         | Distribution Only | 5        | A         | APHERESIS_PLATELETS_LEUKOREDUCED | ROOM_TEMPERATURE     | INTERNAL_TRANSFER | UNLABELED    | false                | W036825158910 | EA007V00   | SATISFACTORY   | Shipment can only contain unlabeled products                                                              | WARN         |
                | 44600011     | DO1         | Distribution Only | 5        | B         | PLASMA_MFG_NONINJECTABLE         | REFRIGERATED         | CUSTOMER          | LABELED      | false                | W013682515113 | E0701V00   | SATISFACTORY   | Product Family does not match                                                                             | WARN         |
                | 44600012     | DO1         | Distribution Only | 5        | B         | PLASMA_MFG_INJECTABLE            | FROZEN               | CUSTOMER          | LABELED      | false                | W036825151112 | E5879V00   | SATISFACTORY   | Product Family does not match                                                                             | WARN         |
                | 44600013     | DO1         | Distribution Only | 5        | A         | CRYOPRECIPITATE                  | FROZEN               | CUSTOMER          | LABELED      | false                | W036825151111 | E5165V00   | SATISFACTORY   | Blood type does not match                                                                                 | WARN         |
                | 44600014     | DO1         | Distribution Only | 5        | AP        | PLASMA_TRANSFUSABLE              | FROZEN               | CUSTOMER          | LABELED      | false                | W036825151114 | E2617V00   | SATISFACTORY   | This product is expired and has been discarded. Place in biohazard container.                             | INFO         |

