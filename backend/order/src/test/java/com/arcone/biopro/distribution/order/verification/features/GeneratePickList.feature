@AOA-19
Feature: Generate Picklist

    Background:
        Given I cleaned up from the database the orders with external ID starting with "DIS442".

    Rule: I should be able to generate pick list for internal transfer order.
    @api @DIS-442
    Scenario Outline: Generate pick list no short date products
    Given I have a Biopro Order with externalId "<External ID>", Location Code "<LocationCode>", Priority "<Priority>", Status "<Status>", shipment type "<Shipment Type>", delivery type "<Delivery Type>", shipping method "<Shipping Method>", product category "<Product Category>", desired ship date "<Desired Date>", shipping customer code and name as "<Shipping Customer Code>" and "<Shipping Customer Name>", billing customer code and name as "<Billing Customer Code>" and "<Billing Customer Name>", and comments "<Order Comments>", and Quarantined Products as "<Quarantined Products>", and Label Status as "<Label Status>".
    And I have an order item with product family "<ProductFamily>", blood type "<BloodType>", quantity <Quantity>, and order item comments "<Item Comments>".
    When I request to generate the Pick List.
    Then I have received the pick list details.
    And I have received a shipment created event.
    And I "<ShouldShouldNot>" received the short date product details.
    And The order status in the API response is "IN_PROGRESS".
    And I should not have multiple shipments generated.
    And I cleaned up from the database the orders with external ID starting with "DIS442".

    Examples:
        | External ID | LocationCode | Priority | Status | ProductFamily                | BloodType | Quantity | Shipment Type     | Shipping Method | Product Category | Desired Date | Shipping Customer Code | Shipping Customer Name     | Billing Customer Code | Billing Customer Name      | Order Comments     | Item Comments | ShouldShouldNot | Label Status | Quarantined Products |
        | DIS442001   | 123456789    | STAT     | OPEN   | PLASMA_TRANSFUSABLE          | AB        | 3        | CUSTOMER          | FEDEX           | FROZEN           | 2024-08-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Confirm when ready | Needed asap   | SHOULD          | LABELED      | <null>               |
        | DIS442002   | 123456789    | STAT     | OPEN   | PLASMA_TRANSFUSABLE          | B         | 8        | CUSTOMER          | FEDEX           | FROZEN           | 2024-09-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Send asap          | Needed asap   | SHOULD NOT      | LABELED      | <null>               |
        | DIS442003   | 123456789    | STAT     | OPEN   | RED_BLOOD_CELLS_LEUKOREDUCED | ABP       | 3        | CUSTOMER          | FEDEX           | REFRIGERATED     | 2024-08-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Confirm when ready | Needed asap   | SHOULD          | LABELED      | <null>               |
        | DIS442004   | 123456789    | STAT     | OPEN   | WHOLE_BLOOD                  | AN        | 8        | CUSTOMER          | FEDEX           | REFRIGERATED     | 2024-09-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Send asap          | Needed asap   | SHOULD NOT      | LABELED      | <null>               |
        | DIS442005   | 123456789    | STAT     | OPEN   | WHOLE_BLOOD_LEUKOREDUCED     | BP        | 8        | CUSTOMER          | FEDEX           | REFRIGERATED     | 2024-09-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Send asap          | Needed asap   | SHOULD NOT      | LABELED      | <null>               |
        | DIS442006   | 123456789    | STAT     | OPEN   | RED_BLOOD_CELLS              | BN        | 8        | CUSTOMER          | FEDEX           | REFRIGERATED     | 2024-09-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Send asap          | Needed asap   | SHOULD NOT      | LABELED      | <null>               |
        | DIS442007   | 123456789    | STAT     | OPEN   | RED_BLOOD_CELLS_LEUKOREDUCED | AP        | 10       | INTERNAL_TRANSFER | FEDEX           | FROZEN           | 2024-08-20   | DO1                    | Distribution Only          | <null>                | <null>                     | Confirm when ready | Needed asap   | SHOULD NOT      | LABELED      | false                |
        | DIS442008   | 123456789    | STAT     | OPEN   | WHOLE_BLOOD                  | ANY       | 10       | INTERNAL_TRANSFER | FEDEX           | REFRIGERATED     | 2024-08-20   | DO1                    | Distribution Only          | <null>                | <null>                     | Confirm when ready | Needed asap   | SHOULD NOT      | UNLABELED    | true                 |
