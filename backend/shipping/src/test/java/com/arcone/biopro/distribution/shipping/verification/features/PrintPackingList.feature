@ui @AOA-19
Feature: Print Packing List
    As a DT, I want to be able to print the packing list, so that I can know the products that are placed in the box.

    Background:
        Given I cleaned up from the database the packed item that used the unit number "W036898786811,W036810946400,W812530106085".
        And I cleaned up from the database, all shipments with order number "499,421,500,4410001,4410002".

    Rule: I should be able to print the packing slip in the PDF format for the shipment when it is completed.
    Rule: I should be able to see the product details that are placed in the box when the shipment is completed.
    Rule: I should be able to reprint the packing slip, if needed.
        @DIS-157 @DIS-50 @DIS-41 @DIS-27 @DIS-19
        Scenario Outline: Print the Packing List
            Given The shipment details are Order Number "<Order Number>", Location Code "<Location Code>", Customer ID "<Customer ID>", Customer Name "<Customer Name>", Department "<Department>", Address Line 1 "<Address Line 1>", Address Line 2 "<Address Line 2>", Unit Number "<Unit Number>", Product Code "<Product Code>", Product Family "<Product Family>", Blood Type "<Blood Type>", Expiration "<Expiration>", Quantity <Quantity>.
            And The second verification configuration is "disabled".
            And I received a shipment fulfillment request with above details.
            And I have filled the shipment with the unit number "<Unit Number>" and product code "<Product Code>".
            And I have completed a shipment with above details.
            And I am on the Shipment Fulfillment Details page for order <Order Number>.
            When I choose to print the Packing Slip.
            Then I am able to see the Packing Slip content.

            Examples:
                | Order Number | Location Code | Customer ID | Customer Name        | Department            | Address Line 1 | Address Line 2 | Unit Number   | Product Code | Product Family               | Blood Type | Expiration | Quantity |
                | 499          | 123456789     | 1           | Random Hospital Inc. | Testing Blood Banking | Street N1      | Suite N2       | W036898786811 | E4701V00     | PLASMA_TRANSFUSABLE          | AP         | 04-09-2025 | 1        |
                | 500          | 123456789     | 1           | Random Hospital Inc. | Testing Blood Banking | Street N1      | Suite N2       | W812530106085 | E0685V00     | RED_BLOOD_CELLS_LEUKOREDUCED | ABP        | 04-09-2025 | 1        |

    Rule: I should not be able to print the packing slip in the PDF format for the shipment when it is open.
        @DIS-50 @DIS-41 @DIS-27
        Scenario Outline: Print the Packing List with an open Shipment
            Given The shipment details are Order Number "<Order Number>", Location Code "<Location Code>", Customer ID "<Customer ID>", Customer Name "<Customer Name>", Department "<Department>", Address Line 1 "<Address Line 1>", Address Line 2 "<Address Line 2>", Unit Number "<Unit Number>", Product Code "<Product Code>", Product Family "<Product Family>", Blood Type "<Blood Type>", Expiration "<Expiration>", Quantity <Quantity>.
            And I have an open shipment with above details.
            When I enter the Shipment Fulfillment Details page for order <Order Number>.
            Then I should not be able to print the Packing List.

            Examples:
                | Order Number | Location Code | Customer ID | Customer Name        | Department    | Address Line 1 | Address Line 2 | Unit Number   | Product Code | Product Family      | Blood Type | Expiration | Quantity |
                | 421          | 123456789     | 1           | Random Hospital Inc. | Blood Banking | Street 1       | Suite 2        | W036810946400 | E246300      | PLASMA_TRANSFUSABLE | AP         | 04-09-2025 | 1        |


        Rule: I should be able to view and print the packing slip for the Internal Transfer order.
        @DIS-441
        Scenario Outline: Print the Packing List Internal Transfer
            Given The Internal Transfer shipment details are:
                | Order_Number   | Customer_ID   | Customer_Name   | Quantity   | Blood_Type  | Product_Family  | Unit_Numbers  | Product_Codes  | Temp_Category | Shipment_Type   | Label_Status   | Quarantined_Products   | Product_Status |
                | <Order Number> | <Customer ID> | <Customer Name> | <Quantity> | <BloodType> | <ProductFamily> | <UNs>         | <ProductCodes> | <Category>    | <Shipment Type> | <Label Status> | <Quarantined Products> | PACKED         |
            And The second verification configuration is "disabled".
            And I have completed a shipment with above details.
            And I am on the Shipment Fulfillment Details page for order <Order Number>.
            When I choose to print the Packing Slip.
            Then I am able to see the Packing Slip content.
            And I "should not" see the Billed to information.

            Examples:
                | Order Number | Customer ID | Customer Name             | UNs           | ProductCodes | ProductFamily                | BloodType | Quantity | Category | Shipment Type     | Label Status | Quarantined Products |
                | 4410001      | DL1         | Distribution and Labeling | W036898786811 | E4701V00     | PLASMA_TRANSFUSABLE          | AP        | 1        | FROZEN   | INTERNAL_TRANSFER | LABELED      | true                 |
                | 4410002      | DO1         | Distribution Only         | W812530106085 | E0685V00     | RED_BLOOD_CELLS_LEUKOREDUCED | ABP       | 1        | FROZEN   | INTERNAL_TRANSFER | LABELED      | false                |
