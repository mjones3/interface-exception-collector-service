@AOA-39 @AOA-19
Feature: Search Orders

    Background:
        Given I cleaned up from the database the orders with external ID starting with "EXTSEARCH1".
        And I cleaned up from the database the orders with external ID starting with "29402".
        And I have removed from the database all the configurations for the location "123456789_DIS481".


        Rule: I should be able to filter the order lists by specific criteria.
        Rule: I should be able to apply filter criteria.
        Rule: I should be able to search the order by BioPro order number or External Order ID.
        Rule: I should be prevented from selecting other filters when BioPro Order number or External ID is selected.
        Rule: I should be able to see the other filter options disabled when filtering by either the BioPro Order number or External Order ID.
        Rule: I should be able to see the Shipment Type and Ship to Location as filter options.
        @ui @R20-227 @R20-228 @DIS-481
        Scenario Outline: Search orders by Order Number
            Given I have a Biopro Order with externalId "<External ID>", Location Code "<Order LocationCode>", Priority "<Priority>" and Status "<Status>".
            And I have another Biopro Order with the externalId equals to order number of the previous order.
            And I am logged in the location "<User LocationCode>".
            And I choose search orders.
            And I open the search orders filter panel.
            And "order number, create date from, create date to, desired shipment date from, desired shipment date to, order status, priority, shipment type" fields are "enabled".
            And "ship to customer, ship to location" fields are "disabled".
            And I search the order by "<Search Key>".
            And "create date from, create date to, desired shipment date from, desired shipment date to, order status, priority, ship to customer, ship to location, shipment type" fields are "disabled".
            When I choose "apply" option.
            Then I should see 2 orders in the search results.

            Examples:
                | External ID   | Order LocationCode | User LocationCode | Priority | Status    | Search Key |
                | EXTSEARCH1979 | 123456789          | 123456789         | STAT     | OPEN      | orderId    |
                | EXTSEARCH1984 | 123456789          | 123456789         | STAT     | OPEN      | orderId    |
                | EXTSEARCH1985 | 123456789          | 123456789         | STAT     | CANCELLED | orderId    |


    Rule: I should not be able to use a greater initial date when compared to final date field
        Rule: I should be able to filter the results for date fields from 2 years back.
    Rule: I should not be able to search more than 2 years range.
        @api @R20-228
        Scenario Outline: Search for orders with wrong data range
            Given I have an order with external ID "EXTSEARCH1R20228001" and status "OPEN".
            When I search for orders by "createDate" from "<Create Date From>" to "<Create Date To>".
            Then I should receive a "BAD_REQUEST" error message response "<Error Message>".
            Examples:
                | Create Date From | Create Date To | Error Message                                      |
                | 2025-01-02       | 2025-01-01     | Initial date should not be greater than final date |
                | 2021-05-05       | 2024-05-01     | Date range exceeds two years                       |
                | 2021-05-05       | 2099-05-01     | Final date should not be greater than today        |


    Rule: I should be able to search completed orders by order number.
        @api @DIS-294 @bug
        Scenario Outline: Search completed order and the associated backorder
            Given I have an order with external ID "<External Id>" partially fulfilled with a shipment "<Shipment Status>".
            And I have Shipped "<Shipped Quantity>" products of each item line.
            And I have the back order configuration set to "true".
            And I request to complete the order.
            When I search for orders by "<Search Key>".
            Then I should receive the search results containing "<Expected Quantity>" orders.

            Examples:
                | Shipment Status | Search Key | Shipped Quantity | Expected Quantity | External Id        |
                | COMPLETED       | externalId | 2                | 2                 | EXTSEARCH1DIS29402 |
                | COMPLETED       | externalId | 2                | 2                 | 29402              |
                | COMPLETED       | orderId    | 3                | 1                 | EXTSEARCH1DIS29402 |
                | COMPLETED       | orderId    | 3                | 1                 | 29402              |



        Rule: I should be able to search the order by  “Ship to Location” when the Shipment Type is Internal Transfer.
        Rule: I should be able to search the order by “Ship to Customer” when the Shipment Type is Customer.
        Rule: I should be able to see the Location name in the “Ship To” column when the shipment type is Internal Transfer.
        @api @DIS-481
        Scenario Outline: Search orders by Ship to Customer/Location based on the shipment type
            Given The location "123456789_DIS481" is configured.
            And I have a Biopro Order with externalId "<External ID>", Location Code "<LocationCode>", Priority "<Priority>", Status "<Status>", shipment type "<Shipment Type>", delivery type "<Delivery Type>", shipping method "<Shipping Method>", product category "<Product Category>", desired ship date "<Desired Date>", shipping customer code and name as "<Shipping Customer Code>" and "<Shipping Customer Name>", billing customer code and name as "<Billing Customer Code>" and "<Billing Customer Name>", and comments "<Order Comments>", and Quarantined Products as "<Quarantined Products>", and Label Status as "<Label Status>".
            And I have 2 order items with product families "<ProductFamily>", blood types "<BloodType>", quantities "<Quantity>", and order item comments "<Item Comments>".
            When I search for orders by "<Search Keys>" and "<Search Values>".
            Then The order with external ID "<External ID>" is present.
            And The result list includes only the shipment type "<Shipment Type>".
            And The result list includes only the customer "<Shipping Customer Name>".
            Examples:
                | External ID    | LocationCode     | Priority | Status | ProductFamily                                                     | BloodType | Quantity | Shipment Type     | Shipping Method | Product Category | Desired Date | Shipping Customer Code | Shipping Customer Name     | Billing Customer Code | Billing Customer Name      | Order Comments     | Item Comments                | Quarantined Products | Label Status | Search Keys                           | Search Values                            |
                | ORDERDIS481001 | 123456789_DIS481 | STAT     | OPEN   | APHERESIS_PLATELETS_LEUKOREDUCED,APHERESIS_PLATELETS_LEUKOREDUCED | AB,A      | 2,50     | CUSTOMER          | FEDEX           | ROOM_TEMPERATURE | 2024-08-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Confirm when ready | Needed asap, Another comment | <null>               | <null>       | locationCode, shipmentType, customers | 123456789_DIS481, CUSTOMER, A1235        |
                | ORDERDIS481002 | 123456789_DIS481 | STAT     | OPEN   | PRT_APHERESIS_PLATELETS,PRT_APHERESIS_PLATELETS                   | AB,A      | 10,5     | CUSTOMER          | FEDEX           | ROOM_TEMPERATURE | 2024-08-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Confirm when ready | Needed asap, Another comment | <null>               | <null>       | locationCode, shipmentType, customers | 123456789_DIS481, CUSTOMER, A1235        |
                | ORDERDIS481003 | 123456789_DIS481 | STAT     | OPEN   | PRT_APHERESIS_PLATELETS,PRT_APHERESIS_PLATELETS                   | AB,O      | 10,7     | CUSTOMER          | FEDEX           | REFRIGERATED     | 2024-08-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Confirm when ready | Needed asap, Another comment | <null>               | <null>       | locationCode, shipmentType, customers | 123456789_DIS481, CUSTOMER, A1235        |
                | ORDERDIS481004 | 123456789_DIS481 | STAT     | OPEN   | RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED         | AP,OP     | 10,7     | CUSTOMER          | FEDEX           | FROZEN           | 2024-08-20   | A1235                  | Creative Testing Solutions | A1235                 | Creative Testing Solutions | Confirm when ready | Needed asap, Another comment | <null>               | <null>       | locationCode, shipmentType, customers | 123456789_DIS481, CUSTOMER, A1235        |
                | ORDERDIS481005 | 123456789_DIS481 | STAT     | OPEN   | RED_BLOOD_CELLS_LEUKOREDUCED,RED_BLOOD_CELLS_LEUKOREDUCED         | AP,OP     | 10,7     | INTERNAL_TRANSFER | FEDEX           | FROZEN           | 2024-08-20   | DO1                    | Distribution Only          | <null>                | <null>                     | Confirm when ready | Needed asap, Another comment | false                | LABELED      | locationCode, shipmentType, customers | 123456789_DIS481, INTERNAL_TRANSFER, D01 |
                | ORDERDIS481006 | 123456789_DIS481 | STAT     | OPEN   | WHOLE_BLOOD,WHOLE_BLOOD                                           | ANY,ANY   | 10,7     | INTERNAL_TRANSFER | FEDEX           | REFRIGERATED     | 2024-08-20   | DO1                    | Distribution Only          | <null>                | <null>                     | Confirm when ready | Needed asap, Another comment | true                 | UNLABELED    | locationCode, shipmentType, customers | 123456789_DIS481, INTERNAL_TRANSFER, D01 |



        Scenario: Cleanup database
            Given I cleaned up from the database the orders with external ID starting with "EXTSEARCH1".
            And I cleaned up from the database the orders with external ID starting with "29402".
            And I have removed from the database all the configurations for the location "123456789_DIS481".


                                ############################################################
                                ##################   Disabled scenarios  ##################
                                ###########################################################

        # AC covered by UI unit test
    Rule: I should be redirected to the Order Details page if there is only one order in the system that matches the search criteria.
        @disabled @ui @R20-227
        Scenario Outline: Search for an order and view the details
            Given I have a Biopro Order with externalId "<External ID>", Location Code "<Order LocationCode>", Priority "<Priority>" and Status "<Status>".
            And I have an order item with product family "<ProductFamily>", blood type "<BloodType>", quantity <Quantity>, and order item comments "<Item Comments>".
            And I am logged in the location "<User LocationCode>".
            And I choose search orders.
            And I open the search orders filter panel.
            And I search the order by "externalId".
            When I choose "apply" option.
            Then I should be redirected to the order details page.

            Examples:
                | External ID          | Order LocationCode | User LocationCode | Priority | Status | ProductFamily       | BloodType | Quantity | Item Comments |
                | EXTSEARCH1DIS1141179 | 123456789          | 123456789         | STAT     | OPEN   | PLASMA_TRANSFUSABLE | AB        | 3        | Needed asap   |

    # AC covered by UI unit test
    Rule: I should be able to reset the applied filter criteria.
        Rule: The system should not enable the Apply and Reset options until at least one filter criteria is chosen.
    Rule: I should be able to see the following filter options
        @disabled @ui @R20-228
        Scenario: The reset option clears the specified filter criteria
            Given I am logged in the location "123456789".
            And I choose search orders.
            And I open the search orders filter panel.
            And I should see "order number, create date from, create date to, desired shipment date from, desired shipment date to, order status, priority, ship to customer" fields.
            And "reset" option is "disabled".
            And "apply" option is "disabled".
            And I search the order by "00000".
            And "apply" option is "enabled".
            And "reset" option is "enabled".
            When I choose "reset" option.
            Then The filter information should be empty.

    # AC covered by UI unit test
    Rule: I should not be able to select create date parameters values greater than current date
        @disabled @ui @R20-228
        Scenario: Ensure that the selected dates for create date aren't greater than current date
            Given I am logged in the location "123456789".
            And I choose search orders.
            And I open the search orders filter panel.
            When I enter a future date for the field "create date to".
            Then I should see a validation message: "Final date should not be greater than today".
            And "apply" option is "disabled".


        # AC covered by UI unit test
    Rule: I should be able to multi-select options for Priority, Status, and Ship to Customer fields.
        Rule: I should be able to see order number disabled when filtering by remaining filter fields.
    Rule: I should see the number of fields used to select the filter criteria.
        @disabled @ui @R20-228
        Scenario Outline: Check if multiple select inputs are keeping the multiple selection after the user selects the second item

            Given I have a Biopro Order with externalId "EXTSEARCH1979", Location Code "123456789", Priority "STAT" and Status "OPEN".
            And I have a Biopro Order with externalId "EXTSEARCH1984", Location Code "123456789", Priority "ASAP" and Status "IN_PROGRESS".
            And I have a Biopro Order with externalId "EXTSEARCH12018", Location Code "123456789", Priority "ROUTINE" and Status "OPEN".
            And I am logged in the location "123456789".
            And I choose search orders.
            And I open the search orders filter panel.
            When I select "<Selected Priorities>" for the "priority".
            And I select "<Selected Statuses>" for the "order status".
            And I select "<Selected Customers>" for the "ship to customer".
            And Items "<Selected Priorities>" should be selected for "priority".
            And Items "<Selected Statuses>" should be selected for "order status".
            And Items "<Selected Customers>" should be selected for "ship to customer".
            And I select the current date as the "create date" range
            And I select the "12/25/2026" as the "desired shipping date" range
            And "order number" field is "disabled".
            Then I choose "apply" option.
            And I should see "<Expected External Ids>" orders in the search results.
            And I should see "<Expected Number of Filters>" as the number of used filters for the search.
            Examples:
                | Selected Priorities | Selected Statuses | Selected Customers         | Expected External Ids                      | Expected Number of Filters |
                | STAT,ASAP           | OPEN,IN PROGRESS  |                            | EXTSEARCH1979,EXTSEARCH1984                | 4                          |
                | STAT,ROUTINE        |                   |                            | EXTSEARCH1979,EXTSEARCH12018               | 3                          |
                | ASAP                | IN PROGRESS       | Creative Testing Solutions | EXTSEARCH1984                              | 5                          |
                |                     |                   |                            | EXTSEARCH1979,EXTSEARCH1984,EXTSEARCH12018 | 2                          |


    Rule: I should not be able to use a greater initial date when compared to final date field
        Rule: I should be able to see the required filter options
        @disabled @ui @R20-228
        Scenario Outline: Ensure that the date range validation checks for greater initial dates when compared to final dates for range fields
            Given I am logged in the location "123456789".
            And I choose search orders.
            And I open the search orders filter panel.
            When I enter the date: "12/31/2023" for the field "<Initial Date Field>" and the date: "12/30/2023"  for the field "<Final Date Field>".
            Then I should see a validation message: "Initial date should not be greater than final date".
            And  I should see "create date" fields as required.

            Examples:
                | Initial Date Field         | Final Date Field         |
                | create date from           | create date to           |
                | desired shipping date from | desired shipping date to |

    Rule: I should not be able to see the orders from a different location.
        Rule: I should be able to view an error message when I search for a non-existent order number.
        @disabled @ui @R20-227
        Scenario Outline: Search for an order number from a different location
            Given I have a Biopro Order with externalId "<External ID>", Location Code "<Order LocationCode>", Priority "<Priority>" and Status "<Status>".
            And I am logged in the location "<User LocationCode>".
            And I choose search orders.
            And I open the search orders filter panel.
            And I search the order by "<Search Key>".
            When I choose "apply" option.
            Then I should see a "Caution" message: "No Results Found".

            Examples:
                | External ID              | Order LocationCode | User LocationCode | Priority | Status | Search Key |
                | EXTSEARCH114117922233510 | 123456789          | 234567891         | STAT     | OPEN   | externalId |
                | EXTSEARCH114117922233510 | 123456789          | 123456788         | STAT     | OPEN   | 000111     |

    Rule: I should be able to filter the results for date fields from 2 years back.
        Rule: I should be able to enter the create date manually or select from the integrated component.
    Rule: I should not be able to search more than 2 years range.
        Rule: I should not be able to apply filters if any field validations fail.
    Rule: I should be able to implement the field-level validation and display an error message if the validations fail.
        @disabled @ui @R20-228
        Scenario: Check if the values informed for create date range don't exceed 2 years in the past
            Given I am logged in the location "123456789".
            And I choose search orders.
            And I open the search orders filter panel.
            When I enter a past date: "11/31/2018" for the field "create date from".
            Then I should see a validation message: "Date range exceeds two years".
            And "reset" option is "enabled".
            And "apply" option is "disabled".
