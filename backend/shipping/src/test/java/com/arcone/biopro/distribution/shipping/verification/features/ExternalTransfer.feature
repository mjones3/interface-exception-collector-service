@AOA-22
Feature: External Transfers


    Background:
        Given I cleaned up from the database the product locations history that used the unit number "W036898786807,W812530106085,W812530106095".
        And I cleaned up from the database the external transfer information that used the customer code "A1235,B2346,C3457".

    Rule: I should be able to record the transfer customer, transfer date, and hospital order reference ID.
        @api @DIS-302
        Scenario Outline: Enter External Transfer Information
            Given I have shipped the following products.
                | Unit Number   | Product Code | Product Family               | Customer Code | Customer Name              | Shipped Date        |
                | W036898786807 | E0869V00     | PLASMA_TRANSFUSABLE          | A1235         | Creative Testing Solutions | 2025-02-01 00:00:00 |
                | W812530106085 | E0685V00     | RED_BLOOD_CELLS_LEUKOREDUCED | A1235         | Creative Testing Solutions | 2025-02-01 00:00:00 |
                | W812530106095 | E0033V00     | WHOLE_BLOOD_LEUKOREDUCED     | A1235         | Creative Testing Solutions | 2025-02-01 00:00:00 |

            When I create an external transfer request to the customer "<Customer Code To>", hospital transfer id "<Transfer ID>" and transfer date "<Transfer Date>".
            Then I should have an external transfer request created.
            Examples:
                | Transfer Date | Customer Code To | Transfer ID |
                | 2025-02-01    | A1235            | 123         |
                | 2025-01-01    | A1235            | NULL_VALUE  |


    Rule: I should be alerted and not proceed ahead if the transfer date entered is in the future.
        Rule: I should not be able to proceed if transfer customer and transfer date are not valid.
        @api @DIS-302
        Scenario Outline: Enter Invalid Transfer Information
            Given I have shipped the following products.
                | Unit Number   | Product Code | Product Family               | Customer Code | Customer Name              | Shipped Date        |
                | W036898786807 | E0869V00     | PLASMA_TRANSFUSABLE          | A1235         | Creative Testing Solutions | 2025-02-01 00:00:00 |
                | W812530106085 | E0685V00     | RED_BLOOD_CELLS_LEUKOREDUCED | A1235         | Creative Testing Solutions | 2025-02-01 00:00:00 |
                | W812530106095 | E0033V00     | WHOLE_BLOOD_LEUKOREDUCED     | A1235         | Creative Testing Solutions | 2025-02-01 00:00:00 |
            When I create an external transfer request to the customer "<Customer Code To>", hospital transfer id "<Transfer ID>" and transfer date "<Transfer Date>".
            Then I should receive a "WARN" message response "<Message>".
            Examples:
                | Transfer Date | Customer Code To | Transfer ID | Message                               |
                | 2025-02-01    | ABC              | 123         | Customer not found ABC                |
                | 2055-03-01    | A1235            | NULL_VALUE  | Transfer Date cannot be in the future |


    Rule: I should be able to record the transfer customer, transfer date, and hospital order reference ID.
        @ui @DIS-302
        Scenario Outline: Enter External Transfer Information
            Given I have shipped the following products.
                | Unit Number   | Product Code | Product Family               | Customer Code | Customer Name              | Shipped Date        |
                | W036898786807 | E0869V00     | PLASMA_TRANSFUSABLE          | A1235         | Creative Testing Solutions | 2025-02-01 00:00:00 |
                | W812530106085 | E0685V00     | RED_BLOOD_CELLS_LEUKOREDUCED | A1235         | Creative Testing Solutions | 2025-02-01 00:00:00 |
                | W812530106095 | E0033V00     | WHOLE_BLOOD_LEUKOREDUCED     | A1235         | Creative Testing Solutions | 2025-02-01 00:00:00 |

            And I navigate to the external transfer page.
            When I choose customer name "<Customer Name>".
            And  I fill hospital transfer Id "<Hospital Order Reference ID>" and "<Transfer Date>" as transfer Date.
            Then I "should" be able to add products to the external transfer request.
            Examples:
                | Transfer Date | Customer Name           | Hospital Order Reference ID |
                | 01/25/2024    | Pioneer Health Services | NULL_VALUE                  |
                | 01/02/2024    | Advanced Medical Center | XYZ123                      |


    Rule: I should be able to manually enter the unit number and product code without the check digit.
        Rule: I should be able to complete the process if at least one valid product is entered.
    Rule: I should receive a success message after the external transfer process has been completed.
        @api @DIS-303
        Scenario Outline: Enter product information.
            Given I have shipped the following products.
                | Unit Number   | Product Code | Product Family               | Customer Code | Customer Name              | Shipped Date        |
                | W036898786807 | E0869V00     | PLASMA_TRANSFUSABLE          | A1235         | Creative Testing Solutions | 2025-02-01 00:00:00 |
                | W812530106085 | E0685V00     | RED_BLOOD_CELLS_LEUKOREDUCED | A1235         | Creative Testing Solutions | 2025-02-01 00:00:00 |
                | W812530106095 | E0033V00     | WHOLE_BLOOD_LEUKOREDUCED     | A1235         | Creative Testing Solutions | 2025-02-01 00:00:00 |
            And I have entered the external transfer information for the customer "<Customer Code>" , transfer date "<Transfer Date>" successfully.
            When I add the product with "<Unit Number>" and "<Product Code>".
            Then The product "should" be added in the list of products to be transferred.
            When I submit the external transfer process.
            Then I should receive a "SUCCESS" message response "<Success Message>".
            Examples:
                | Customer Code | Transfer Date | Unit Number   | Product Code | Success Message                          |
                | B2346         | 2025-02-03    | W036898786807 | E0869V00     | External transfer completed successfully |

    Rule: I should be alerted if the current external transfer date is before the last shipped date.
        Rule: I should be alerted if the product entered is not shipped.
    Rule: I should be alerted if the last shipped location is the same as transfer to location.
        @api @DIS-303
        Scenario Outline: Enter the unacceptable product information.
            Given I have shipped the following products.
                | Unit Number   | Product Code | Product Family               | Customer Code | Customer Name              | Shipped Date        |
                | W036898786807 | E0869V00     | PLASMA_TRANSFUSABLE          | A1235         | Creative Testing Solutions | 2025-02-01 00:00:00 |
                | W812530106085 | E0685V00     | RED_BLOOD_CELLS_LEUKOREDUCED | A1235         | Creative Testing Solutions | 2025-02-01 00:00:00 |
                | W812530106095 | E0033V00     | WHOLE_BLOOD_LEUKOREDUCED     | B2346         | Creative Testing Solutions | 2025-02-01 00:00:00 |
            And I have entered the external transfer information for the customer "<Customer Code>" , transfer date "<Transfer Date>" successfully.
            When I add the product with "<Unit Number>" and "<Product Code>".
            Then The product "should not" be added in the list of products to be transferred.
            And I should receive a "WARN" message response "<Message>".
            Examples:
                | Customer Code | Unit Number   | Product Code | Transfer Date | Message                                                                   |
                | B2346         | W036810946300 | E0869V00     | 2025-02-14    | This product has not been shipped                                         |
                | B2346         | W812530106085 | E0685V00     | 2025-01-20    | The transfer date is before the last shipped date                         |
                | B2346         | W812530106095 | E0033V00     | 2025-02-20    | Last Shipped Location cannot be same as the Transfer to Customer location |

    Rule: I should be alerted if the last shipped location for the products in the batch is not the same.
        @api @DIS-303
        Scenario Outline: Validate Last Shipped Location for Products in Batch.
            Given I have shipped the following products.
                | Unit Number   | Product Code | Product Family               | Customer Code | Customer Name              | Shipped Date        |
                | W036898786807 | E0869V00     | PLASMA_TRANSFUSABLE          | A1235         | Creative Testing Solutions | 2025-02-01 00:00:00 |
                | W812530106085 | E0685V00     | RED_BLOOD_CELLS_LEUKOREDUCED | B2346         | Advanced Medical Center    | 2025-02-01 00:00:00 |
                | W812530106095 | E0033V00     | WHOLE_BLOOD_LEUKOREDUCED     | B2346         | Advanced Medical Center    | 2025-02-01 00:00:00 |
            And I have entered the external transfer information for the customer "<Customer Code>" , transfer date "<Transfer Date>" successfully.
            When I add the product with "<First Unit Number>" and "<First Product Code>".
            Then The product "should" be added in the list of products to be transferred.
            When I add the product with "<Second Unit Number>" and "<Second Product Code>".
            Then The product "should not" be added in the list of products to be transferred.
            And I should receive a "WARN" message response "<Message>".
            Examples:
                | Customer Code | Transfer Date | First Unit Number | First Product Code | Second Unit Number | Second Product Code | Message                                                      |
                | C3457         | 2025-02-01    | W036898786807     | E0869V00           | W812530106085      | E0685V00            | The product location doesn't match the last shipped location |

    Rule: External Transfer Process Should be Completed Successfully.
        Rule: I should be able to transfer different product families in the batch.
        @ui @DIS-303
        Scenario Outline: Successfully Enter External Transfer Information and Complete Transfer
            Given I have shipped the following products.
                | Unit Number   | Product Code | Product Family               | Customer Code | Customer Name              | Shipped Date        |
                | W036898786807 | E0869V00     | PLASMA_TRANSFUSABLE          | B2346         | Creative Testing Solutions | 2025-02-01 00:00:00 |
                | W812530106085 | E0685V00     | RED_BLOOD_CELLS_LEUKOREDUCED | B2346         | Creative Testing Solutions | 2025-02-01 00:00:00 |
                | W812530106095 | E0033V00     | WHOLE_BLOOD_LEUKOREDUCED     | B2346         | Creative Testing Solutions | 2025-02-01 00:00:00 |
            And I navigate to the external transfer page.
            When I choose customer name "<Customer Name>".
            And  I fill hospital transfer Id "<Hospital Order Reference ID>" and "<Transfer Date>" as transfer Date.
            Then The submit external transfer option should be "disabled".
            When I add the following products to the external transfer request.
                | Unit Number   | Product Code |
                | W812530106085 | E0685V00     |
                | W036898786807 | E0869V00     |
            Then The submit external transfer option should be "enabled".
            And The product should be added to the list of products to be transferred.
            When I choose to submit the external transfer.
            Then I should see a "success" message: "<Success Message>".
            And The External transfer process should be restarted.
            And The submit external transfer option should be "disabled".
            Examples:
                | Customer Name              | Hospital Order Reference ID | Transfer Date | Success Message                          |
                | Creative Testing Solutions | XYZ123                      | 02/20/2025    | External transfer completed successfully |


    Rule: I should be able to cancel the process at any point after entering the external transfer information.
    Rule: I should receive a confirmation message stating that all transfer information will be removed.
    Rule: The data entered must not be saved after canceling the process.
        @api @DIS-306
        Scenario Outline: Cancel external transfer process.
            Given I have shipped the following products.
                | Unit Number   | Product Code | Product Family               | Customer Code | Customer Name              | Shipped Date        |
                | W036898786807 | E0869V00     | PLASMA_TRANSFUSABLE          | A1235         | Creative Testing Solutions | 2025-02-01 00:00:00 |
                | W812530106085 | E0685V00     | RED_BLOOD_CELLS_LEUKOREDUCED | B2346         | Advanced Medical Center    | 2025-02-01 00:00:00 |
                | W812530106095 | E0033V00     | WHOLE_BLOOD_LEUKOREDUCED     | B2346         | Advanced Medical Center    | 2025-02-01 00:00:00 |
            And I have entered the external transfer information for the customer "<Customer Code>" , transfer date "<Transfer Date>" successfully.
            When I request to cancel the external transfer process.
            Then I should receive status "200 OK" with type "CONFIRMATION" and message "When cancelling, all external transfer information will be removed. Are you sure you want to cancel?".
            When I confirm the cancelation of external transfer.
            Then I should receive a "Success" message: "External transfer cancelation completed".
            And The data entered must not be saved after canceling the process.
            Examples:
                | Customer Code | Transfer Date |
                | B2346         | 2025-02-03    |


    Rule: I should be able to cancel the process at any point after entering the external transfer information.
    Rule: I should receive a confirmation message stating that all transfer information will be removed.
    Rule: The data entered must not be saved after canceling the process.
        @api @DIS-306
        Scenario Outline: Cancel external transfer process with products in the batch.
            Given I have shipped the following products.
                | Unit Number   | Product Code | Product Family               | Customer Code | Customer Name              | Shipped Date        |
                | W036898786807 | E0869V00     | PLASMA_TRANSFUSABLE          | A1235         | Creative Testing Solutions | 2025-02-01 00:00:00 |
                | W812530106085 | E0685V00     | RED_BLOOD_CELLS_LEUKOREDUCED | B2346         | Advanced Medical Center    | 2025-02-01 00:00:00 |
                | W812530106095 | E0033V00     | WHOLE_BLOOD_LEUKOREDUCED     | B2346         | Advanced Medical Center    | 2025-02-01 00:00:00 |
            And I have entered the external transfer information for the customer "<Customer Code>" , transfer date "<Transfer Date>" successfully.
            And I add the product with "<Unit Number>" and "<Product Code>".
            Then The product "should" be added in the list of products to be transferred.
            When I request to cancel the external transfer process.
            Then I should receive status "200 OK" with type "CONFIRMATION" and message "When cancelling, all external transfer information will be removed. Are you sure you want to cancel?".
            When I confirm the cancellation of external transfer.
            Then I should receive a "Success" message: "External transfer cancellation completed".
            And The data entered must not be saved after canceling the process.
            Examples:
                | Customer Code | Transfer Date | Unit Number   | Product Code |
                | B2346         | 2025-02-03    | W036898786807 | E0869V00     |


    Rule: I should be able to cancel the process at any point after entering the external transfer information.
    Rule: I should be able to abort the cancellation process and resume the external transfer process.
    Rule: I should be able to initiate a new external transfer process after cancelation is completed.
    Rule: I should receive a confirmation message stating that all transfer information will be removed.
    Rule: The data entered must not be saved after canceling the process.
        @ui @DIS-306
        Scenario Outline: Cancel External Transfer Process
            Given I have shipped the following products.
                | Unit Number   | Product Code | Product Family               | Customer Code | Customer Name              | Shipped Date        |
                | W036898786807 | E0869V00     | PLASMA_TRANSFUSABLE          | A1235         | Creative Testing Solutions | 2025-02-01 00:00:00 |
                | W812530106085 | E0685V00     | RED_BLOOD_CELLS_LEUKOREDUCED | A1235         | Creative Testing Solutions | 2025-02-01 00:00:00 |
                | W812530106095 | E0033V00     | WHOLE_BLOOD_LEUKOREDUCED     | A1235         | Creative Testing Solutions | 2025-02-01 00:00:00 |
            And I navigate to the external transfer page.
            Then The cancel external transfer option should be "disabled".
            When I choose customer name "<Customer Name>".
            And  I fill hospital transfer Id "<Hospital Order Reference ID>" and "<Transfer Date>" as transfer Date.
            Then The cancel external transfer option should be "enabled".
            When I choose to cancel the external transfers process.
            Then I should see a "Cancel Confirmation" message: "When cancelling, all external transfer information will be removed. Are you sure you want to cancel?".
            When I choose to confirm the cancelation of external transfers process.
            And I should see a "Success" message: "External transfer cancellation completed".
            And The External transfer process should be restarted.
            And The submit external transfer option should be "disabled".
            And The cancel external transfer option should be "disabled"
            Examples:
                | Transfer Date | Customer Name           | Hospital Order Reference ID |
                | 01/25/2024    | Pioneer Health Services | NULL_VALUE                  |
                | 01/02/2024    | Advanced Medical Center | XYZ123                      |

