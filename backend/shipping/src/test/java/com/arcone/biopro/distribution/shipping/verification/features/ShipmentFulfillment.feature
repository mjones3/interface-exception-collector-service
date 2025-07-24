@AOA-6 @AOA-152 @AOA-128 @AOA-105 @AOA-240 @AOA-19
Feature: Shipment fulfillment request

    Background:
        Given I cleaned up from the database the packed item that used the unit number "W822530106093,W822530106094,W812530106095,W812530106097,W812530106098,W812530106199,W812530107006,W812530107007,W036825158907,W036898786758,W812530107009,W036825185915,W812530107010,W812530444001,W812530444002,W036825158914,W036825158916,W036825158912,W036898786802,W036898445901,W036898445902".
        And I cleaned up from the database, all shipments with order number "1321,1331,1341,1351,1361,1371,1381,1391,1392,1393,1394,1395,2851,2852,261002,336001,336002,336003,336004,337001,650001,570001,4440001,4440002,4440006,4440007,4440008,44400010,44400011,44400012,44400013,44400014,44400015,45200001,45200002,45200003,45200004,45200004,45200005,45200006,45200007".

        Rule: I should be able to receive the shipment fulfillment request.
        Rule: I should be able to persist the shipment fulfilled request on the local store.
        Rule: I should be able to manage labeled products to fill a shipment.
        @DIS-65 @DIS-57 @DIS-444
        Scenario Outline: Receive shipment fulfillment request
            Given I have no shipment fulfillment requests.
            When I receive a shipment fulfillment request event with Order Number as "<Order Number>", Priority as "<Priority>", Shipping Date as "<Shipping Date>" , Shipment Type defined as "<Shipment Type>", Label Status as "<Label Status>" and Quarantined Products as "<Quarantined Products>".
            Then The shipment request will be available in the Distribution local data store and I can fill the shipment.
            Examples:
                | Order Number | Priority | Shipping Date | Shipment Type     | Label Status | Quarantined Products |
                | 650001       | ASAP     | <tomorrow>    | CUSTOMER          | LABELED      | false                |
                | 570001       | ASAP     | <tomorrow>    | CUSTOMER          | LABELED      | false                |
                | 4440001      | ASAP     | <tomorrow>    | INTERNAL_TRANSFER | LABELED      | false                |
                | 4440002      | ASAP     | <tomorrow>    | INTERNAL_TRANSFER | LABELED      | true                 |


       Rule: I should be able to list the pending shipment fulfillment request.
       Rule: I should be able to view the shipment fulfillment details.
       @DIS-65 @DIS-57
       Scenario Outline: View shipment's fulfillment details
           Given I have a shipment request persisted.
           When I retrieve the shipment list.
           Then I am able to see the requests.
           When I retrieve one shipment by shipment id.
           Then I am able to view the shipment fulfillment details.
           And The item attribute "Product Family" contains "PLASMA_TRANSFUSABLE".
           And The item attribute "Blood Type" contains "<Group Value>".
           And The item attribute "Product Quantity" contains "<Quantity>".
           And The item attribute "Shipment Id" is not empty.
           Examples:
               | Group Value | Quantity |
               | A           | 10       |
               | B           | 5        |
               | O           | 8        |
       Rule: I should be able to receive the shipment fulfillment request with short date product details.
       Rule: I should be able to persist with the short date shipment fulfilled request on the local store.
       @DIS-65
       Scenario: Receive shipment fulfillment request
           Given I have no shipment fulfillment requests.
           When I receive a shipment fulfillment request event.
           Then The shipment request will be available in the Distribution local data store and I can fill the shipment.


       Rule: I should be able to list the pending shipment fulfillment request.
       Rule: I should be able to view the shipment fulfillment details with short date products.
       @DIS-59
       Scenario Outline: View shipment's fulfillment details
           Given I have a shipment request persisted.
           When I retrieve the shipment list.
           Then I am able to see the requests.
           When I retrieve one shipment by shipment id.
           Then I am able to view the shipment fulfillment details.
           And The fulfillment request attribute "Order Number" is not empty.
           And The item attribute "Shipment Id" is not empty.
           And The item attribute "Product Family" contains "PLASMA_TRANSFUSABLE".
           And The item attribute "Blood Type" contains "<Group Value>".
           And The item attribute "Product Quantity" contains "<Quantity>".
           And The short date item attribute "Unit Number" contains "<Unit Number>".
           And The short date item attribute "Product Code" contains "<Product Code>".
           Examples:
               | Group Value | Quantity | Unit Number   | Product Code |
               | A           | 10       | W036810946300 | E086900      |
               | B           | 5        | W036810946301 | E070700      |


       Rule: I should be able to fill a product with any valid blood type when the line item product criteria has “ANY“ defined as blood type.
       @api @bug @DIS-273
       Scenario Outline: Fill Shipment with ANY as blood type.
           Given The shipment details are order Number "<Order Number>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>".
           And The visual inspection configuration is "enabled".
           And I have received a shipment fulfillment request with above details.
           When I fill a product with the unit number "<UN>", product code "<Code>".
           Then The product unit number "<UN>" and product code "<Code>" should be packed in the shipment.
           Examples:
               | Order Number | Customer ID | Customer Name    | Quantity | BloodType | ProductFamily                | UN            | Code     |
               | 1321         | 1           | Testing Customer | 10       | ANY       | PLASMA_TRANSFUSABLE          | W822530106093 | E7648V00 |
               | 1331         | 1           | Testing Customer | 5        | ANY       | RED_BLOOD_CELLS_LEUKOREDUCED | W822530106094 | E0685V00 |


       Rule: Distribution Technicians must be able to process and ship products for “DATE-TIME” delivery type orders.
       @api @bug @DIS-260 @DIS-285
       Scenario Outline: Receive a shipment fulfillment request based on priority
           Given I have no shipment fulfillment requests.
           When I receive a shipment fulfillment request event for the order number "<Order Number>" and priority "<Priority>" and shipping date "<ShippingDate>".
           Then The shipment request will be available in the Distribution local data store and I can fill the shipment.
           Examples:
               | Order Number | Priority  | ShippingDate |
               | 1341         | DATE_TIME | 2025-12-31   |
               | 1351         | ASAP      | 2025-12-31   |
               | 1361         | ROUTINE   | 2025-12-31   |
               | 1371         | STAT      | 2025-12-31   |
               | 1381         | SCHEDULED | 2025-12-31   |
               | 2852         | ROUTINE   | NULL_VALUE   |


       @api @DIS-377 @rc
       Scenario Outline: Receive a shipment fulfillment request using a custom shipping method configuration
           Given I have no shipment fulfillment requests.
           When I receive a shipment fulfillment request event for the order number "<Order Number>" and shipping method "<Shipping Method>".
           Then The shipment request will be available in the Distribution local data store and I can fill the shipment.
           Examples:
               | Order Number | Shipping Method |
               | 2853         | DEFAULT         |


       Rule: I should be able to fill orders with Whole Blood and Derived Products.
       Rule: I should be able to fill orders with Apheresis Platelets (PRT and BacT) Products.
       Rule: I should be able to fill orders with Frozen RBCs Products.
       @api @DIS-254 @DIS-336 @DIS-337
       Scenario Outline: Ship Whole Blood and Derived Products.
           Given The shipment details are order Number "<Order Number>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>" , Temperature Category "<Category>".
           And The visual inspection configuration is "enabled".
           And I have received a shipment fulfillment request with above details.
           When I fill a product with the unit number "<UN>", product code "<Code>".
           Then The product unit number "<UN>" and product code "<Code>" should be packed in the shipment.
           Examples:
               | Order Number   | Customer ID | Customer Name    | Quantity | BloodType | ProductFamily                    | UN            | Code     | Category         |
               | 1391           | 1           | Testing Customer | 10       | ANY       | PLASMA_TRANSFUSABLE              | W822530106093 | E7648V00 | FROZEN           |
               | 1392           | 1           | Testing Customer | 5        | ANY       | RED_BLOOD_CELLS_LEUKOREDUCED     | W822530106094 | E0685V00 | FROZEN           |
               | 1393           | 1           | Testing Customer | 5        | ABP       | WHOLE_BLOOD_LEUKOREDUCED         | W812530106095 | E0033V00 | FROZEN           |
               | 1394           | 1           | Testing Customer | 5        | AP        | WHOLE_BLOOD                      | W812530107002 | E0023V00 | FROZEN           |
               | 1395           | 1           | Testing Customer | 5        | ON        | RED_BLOOD_CELLS                  | W812530106098 | E0167V00 | FROZEN           |
               | 336001         | 1           | Testing Customer | 10       | A         | APHERESIS_PLATELETS_LEUKOREDUCED | W812530107006 | EA007V00 | ROOM_TEMPERATURE |
               | 336002         | 1           | Testing Customer | 5        | AB        | APHERESIS_PLATELETS_LEUKOREDUCED | W812530107007 | EA139V00 | ROOM_TEMPERATURE |
               | 336003         | 1           | Testing Customer | 5        | AP        | PRT_APHERESIS_PLATELETS          | W812530107009 | E8340V00 | ROOM_TEMPERATURE |
               | 336004         | 1           | Testing Customer | 5        | BP        | PRT_APHERESIS_PLATELETS          | W812530107010 | EB317V00 | REFRIGERATED     |
               | 337001         | 1           | Testing Customer | 5        | BP        | RED_BLOOD_CELLS_LEUKOREDUCED     | W812530107011 | E5085V00 | FROZEN           |


   @api @DIS-261
   Rule: The second verification process should be restarted when a product is added into the shipment.
   Scenario Outline: Restart verification status when a Product is added into a Shipment.
       Given I have a shipment for order "<Order Number>" with the units "<Units>" and product codes "<Product Codes>" of family "<Product Family>" and blood type "<Blood Type>" "verified", out of <Quantity Requested> requested.
       And The second verification configuration is "enabled".
       When I fill a product with the unit number "<Unit Filled>", product code "<Code Filled>".
       Then The product unit number "<Unit Filled>" and product code "<Code Filled>" should be packed in the shipment.
       And I should have 0 items "verified" in the shipment.
       Examples:
           | Order Number | Product Codes              | Units                                     | Product Family               | Blood Type | Unit Filled   | Code Filled  | Quantity Requested |
           | 261002       | E4701V00,E4701V00,E4701V00 | W822530103004,W822530103005,W822530103006 | PLASMA_TRANSFUSABLE          | AP         | W822530106093 | E7648V00     | 10                 |
           | 261002       | E0685V00,E0685V00,E0685V00 | W822530103004,W822530103005               | RED_BLOOD_CELLS_LEUKOREDUCED | OP         | W822530106094 | E0685V00     | 8                  |
           | 261002       | E0033V00,E0033V00,E0033V00 | W822530103004,W822530103005,W822530103006 | WHOLE_BLOOD_LEUKOREDUCED     | ABP        | W812530106095 | E0033V00     | 5                  |
           | 261002       | E0167V00,E0167V00,E0167V00 | W822530103004,W822530103005               | RED_BLOOD_CELLS              | ON         | W812530106098 | E0167V00     | 5                  |
           | 261002       | E0167V00,E0167V00,E0167V00 | W822530103004,W822530103005               | WHOLE_BLOOD                  | AP         | W812530107002 | E0023V00     | 9                  |


    Rule: I should be able to manage labeled products to fill a shipment.
    Rule: I should be able to fill the internal transfer order labeled products as requested.
    Rule: I should be able to fill an internal transfer order with quarantined products as requested.
    @api @DIS-444
    Scenario Outline: Fill Internal Transfer final labeled product.
        Given The shipment details are order Number "<Order Number>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>", Temperature Category as "<Category>", Shipment Type defined as "<Shipment Type>", Label Status as "<Label Status>" and Quarantined Products as "<Quarantined Products>".
        And The visual inspection configuration is "enabled".
        And I have received a shipment fulfillment request with above details.
        When I fill a product with the unit number "<UN>", product code "<Code>".
        Then The product unit number "<UN>" and product code "<Code>" should be packed in the shipment.
        Examples:
            | Order Number | Customer ID | Customer Name             | Quantity | BloodType | ProductFamily                    | UN            | Code     | Category         | Shipment Type     | Label Status | Quarantined Products |
            | 4440006      |DL1          | Distribution and Labeling | 10       | ANY       | RED_BLOOD_CELLS_LEUKOREDUCED     | W812530444001 | E5107V00 | FROZEN           | INTERNAL_TRANSFER | LABELED      | false                |
            | 4440007      |234567891    | MDL Hub 2                 | 10       | A         | APHERESIS_PLATELETS_LEUKOREDUCED | W812530444002 | EA007V00 | ROOM_TEMPERATURE | INTERNAL_TRANSFER | LABELED      | false                |
            | 4440008      |DO1          | Distribution Only         | 5        | AB        | PLASMA_TRANSFUSABLE              | W036898786758 | E0701V00 | FROZEN           | INTERNAL_TRANSFER | LABELED      | true                 |



    Rule: I should be able to manage labeled products to fill a shipment.
    Rule: I should be able to fill the internal transfer order labeled products as requested.
    Rule: I should be able to fill an internal transfer order with quarantined products as requested.
    @ui @DIS-444
    Scenario Outline: Fill Shipment with Labeled Products.
        Given The shipment details are order Number "<Order Number>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>", Temperature Category as "<Category>", Shipment Type defined as "<Shipment Type>", Label Status as "<Label Status>" and Quarantined Products as "<Quarantined Products>".
        And The check digit configuration is "disabled".
        And The visual inspection configuration is "<Inspection Config>".
        And The second verification configuration is "disabled".
        And I have received a shipment fulfillment request with above details.
        And I am on the Shipment Fulfillment Details page for order <Order Number>.
        And I choose to fill product of family "<Family>" and blood type "<Type>".
        When I add the unit "<UN>" with product code "<Code>".
        And I define visual inspection as "Satisfactory", if needed.
        Then I should see the list of packed products added including "<UN>" and "<Code>".
        And I should not see the product selection option with the products "<negative_product_list>".
        And I should see the inspection status as "Satisfactory", if applicable.
        And I "<ShouldShouldNot>" see the product status as "Quarantined".

        Examples:
            | Order Number | Customer ID | Customer Name     | Quantity | BloodType | ProductFamily                                               | Family                           | Type | UN               | Code       | Inspection Config | Category         | Shipment Type     | Label Status | Quarantined Products | ShouldShouldNot | negative_product_list       |
            | 44400010     | 1           | Testing Customer  | 10,5,8   | ANY,B,O   | PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE | PLASMA TRANSFUSABLE              | ANY  | =W03689878680200 | =<E7648V00 | enabled           | FROZEN           | CUSTOMER          | LABELED      | false                | should not      |                             |
            | 44400011     | 1           | Testing Customer  | 2        | ANY       | APHERESIS_PLATELETS_LEUKOREDUCED                            | APHERESIS PLATELETS LEUKOREDUCED | ANY  | =W81253010700800 | =<EA141V00 | enabled           | ROOM_TEMPERATURE | CUSTOMER          | LABELED      | false                | should not      |                             |
            | 44400012     | 1           | Testing Customer  | 2        | B         | PRT_APHERESIS_PLATELETS                                     | PRT APHERESIS PLATELETS          | B    | =W81253010701000 | =<EB317V00 | enabled           | REFRIGERATED     | CUSTOMER          | LABELED      | false                | should not      |                             |
            | 44400013     | 1           | Testing Customer  | 2        | AP        | RED_BLOOD_CELLS_LEUKOREDUCED                                | RED BLOOD CELLS LEUKOREDUCED     | AP   | =W81253010701200 | =<E5107V00 | enabled           | FROZEN           | CUSTOMER          | LABELED      | false                | should not      |                             |
            | 44400014     | DO1         | Distribution Only | 2        | ANY       | PLASMA_TRANSFUSABLE                                         | PLASMA TRANSFUSABLE              | ANY  | =W03689878675800 | =<E0701V00 | enabled           | FROZEN           | INTERNAL_TRANSFER | LABELED      | true                 | should          | LR_RBC,PRODUCT_DESCRIPTION1 |
            | 44400015     | DO1         | Distribution Only | 2        | ANY       | PLASMA_TRANSFUSABLE                                         | PLASMA TRANSFUSABLE              | ANY  | =W03689844590200 | =<E0701V00 | enabled           | FROZEN           | INTERNAL_TRANSFER | LABELED      | true                 | should          | GENERIC2                    |




        Rule: I should be able to see all eligible products for a given unit number.
        Rule: I should be able to select unlabeled products to fill an internal transfer order.
        Rule: I should not be able to select multiple products.
        @ui @DIS-452
        Scenario Outline: Fill Shipment with Unlabeled Unit with multiple products.
            Given The shipment details are order Number "<Order Number>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>", Temperature Category as "<Category>", Shipment Type defined as "<Shipment Type>", Label Status as "<Label Status>" and Quarantined Products as "<Quarantined Products>".
            And The check digit configuration is "disabled".
            And The visual inspection configuration is "<Inspection Config>".
            And The second verification configuration is "disabled".
            And I have received a shipment fulfillment request with above details.
            And I am on the Shipment Fulfillment Details page for order <Order Number>.
            And I choose to fill product of family "<Family>" and blood type "<Type>".
            When I add the unit "<UN>".
            And I define visual inspection as "Satisfactory", if needed.
            Then I should see the product selection option with the products "<product_list>".
            And I should not see the product selection option with the products "<negative_product_list>".
            When I select the product "<product_description>".
            Then I should see the list of packed products added including "<UN>" and "<product_description>".
            And I should see the inspection status as "Satisfactory", if applicable.
            And I "<ShouldShouldNot>" see the product status as "Quarantined".
            Examples:
                | Order Number | Customer ID | Customer Name     | Quantity | BloodType | ProductFamily                | Family                       | Type | UN               | product_description  | product_list                                              | negative_product_list | Inspection Config | Category | Shipment Type     | Label Status | Quarantined Products | ShouldShouldNot |
                | 45200001     | DO1         | Distribution Only | 2        | ANY       | PLASMA_TRANSFUSABLE          | PLASMA TRANSFUSABLE          | ANY  | =W03689878675800 | CPD PLS MI 48H       | LR_RBC,CPD PLS MI 24H,CPD PLS MI 48H,PRODUCT_DESCRIPTION1 | PRODUCT_DESCRIPTION2  | enabled           | FROZEN   | INTERNAL_TRANSFER | UNLABELED    | true                 | should not      |
                | 45200002     | DO1         | Distribution Only | 2        | ANY       | RED_BLOOD_CELLS_LEUKOREDUCED | RED BLOOD CELLS LEUKOREDUCED | ANY  | =W03682518591500 | CPD PLS MI 24H       | LR_RBC,CPD PLS MI 24H                                     |                       | enabled           | FROZEN   | INTERNAL_TRANSFER | UNLABELED    | false                | should not      |
                | 45200003     | DO1         | Distribution Only | 2        | ANY       | PLASMA_TRANSFUSABLE          | PLASMA TRANSFUSABLE          | ANY  | =W03682515890700 | CPD PLS MI 48H       | CPD PLS MI 24H,CPD PLS MI 48H                             |                       | disabled          | FROZEN   | INTERNAL_TRANSFER | UNLABELED    | true                 | should not      |
                | 45200007     | DO1         | Distribution Only | 2        | ANY       | PLASMA_TRANSFUSABLE          | PLASMA TRANSFUSABLE          | ANY  | =W03689844590100 | APH PLASMA 24H EXP 3 | APH PLASMA 24H EXP 2,APH PLASMA 24H EXP 3                 | APH PLASMA 24H EXP 1  | disabled          | FROZEN   | INTERNAL_TRANSFER | UNLABELED    | false                | should not      |


    Rule: I should be able to see all eligible products for a given unit number.
        Rule: I should be able to select unlabeled products to fill an internal transfer order.
        Rule: I should not be able to select multiple products.
        Rule: The system should automatically select when the unit has only one product eligible.
        Rule: I should be notified when all available products have been selected.
        @ui @DIS-452
        Scenario Outline: Fill Shipment with Unlabeled Unit with single product.
            Given The shipment details are order Number "<Order Number>", customer ID "<Customer ID>", Customer Name "<Customer Name>", Product Details: Quantities "<Quantity>", Blood Types: "<BloodType>", Product Families "<ProductFamily>", Temperature Category as "<Category>", Shipment Type defined as "<Shipment Type>", Label Status as "<Label Status>" and Quarantined Products as "<Quarantined Products>".
            And The check digit configuration is "disabled".
            And The visual inspection configuration is "<Inspection Config>".
            And The second verification configuration is "disabled".
            And I have received a shipment fulfillment request with above details.
            And I am on the Shipment Fulfillment Details page for order <Order Number>.
            And I choose to fill product of family "<Family>" and blood type "<Type>".
            When I add the unit "<UN>".
            And I define visual inspection as "Satisfactory", if needed.
            Then I should see the list of packed products added including "<UN>" and "<product_description>".
            And I should see the inspection status as "Satisfactory", if applicable.
            And I "<ShouldShouldNot>" see the product status as "Quarantined".
            When I add the unit "<UN>".
            And I define visual inspection as "Satisfactory", if needed.
#            Verify message type difference
#            Then I should see a "WARNING" message: "All products associated with this unit have already been selected".
            Then I should see a "CAUTION" message: "All products associated with this unit have already been selected".
            Examples:
                | Order Number | Customer ID | Customer Name     | Quantity | BloodType | ProductFamily                    | Family                           | Type | UN               | product_description | Inspection Config | Category         | Shipment Type     | Label Status | Quarantined Products | ShouldShouldNot |
                | 45200004     | DO1         | Distribution Only | 2        | ANY       | APHERESIS_PLATELETS_LEUKOREDUCED | APHERESIS PLATELETS LEUKOREDUCED | ANY  | =W03682515891400 | Apheresis PLATELETS | enabled           | ROOM_TEMPERATURE | INTERNAL_TRANSFER | UNLABELED    | true                 | should not      |
                | 45200005     | DO1         | Distribution Only | 2        | ANY       | RED_BLOOD_CELLS_LEUKOREDUCED     | RED BLOOD CELLS LEUKOREDUCED     | ANY  | =W03682515891200 | LR_RBC              | enabled           | FROZEN           | INTERNAL_TRANSFER | UNLABELED    | false                | should not      |
                | 45200006     | DO1         | Distribution Only | 2        | ANY       | PLASMA_TRANSFUSABLE              | PLASMA TRANSFUSABLE              | ANY  | =W03682515891600 | CPD PLS MI 48H      | disabled          | FROZEN           | INTERNAL_TRANSFER | UNLABELED    | true                 | should not      |


