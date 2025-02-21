@AOA-22
Feature: External Transfers


    Background:
        Given I cleaned up from the database the product locations history that used the unit number "W036810946300,W036810946301,W036810946302".
        And I cleaned up from the database the external transfer information that used the customer code "A1235".

    Rule: I should be able to record the transfer customer, transfer date, and hospital order reference ID.
        @api @DIS-302
        Scenario Outline: Enter External Transfer Information
            Given I have shipped the following products.
                | Unit Number   | Product Code | Customer Code | Customer Name              | Shipped Date        |
                | W036810946300 | E0869V00     | A1235         | Creative Testing Solutions | 2025-02-01 00:00:00 |
                | W036810946301 | E0169V00     | A1235         | Creative Testing Solutions | 2025-02-01 00:00:00 |
                | W036810946302 | E0269V00     | A1235         | Creative Testing Solutions | 2025-02-01 00:00:00 |

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
                | Unit Number   | Product Code | Customer Code | Customer Name              | Shipped Date        |
                | W036810946300 | E0869V00     | A1235         | Creative Testing Solutions | 2025-02-01 00:00:00 |
                | W036810946301 | E0169V00     | A1235         | Creative Testing Solutions | 2025-02-01 00:00:00 |
                | W036810946302 | E0269V00     | A1235         | Creative Testing Solutions | 2025-02-01 00:00:00 |
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
                | Unit Number   | Product Code | Customer Code | Customer Name              | Shipped Date        |
                | W036810946300 | E0869V00     | A1235         | Creative Testing Solutions | 2025-02-01 00:00:00 |
                | W036810946301 | E0169V00     | A1235         | Creative Testing Solutions | 2025-02-01 00:00:00 |
                | W036810946302 | E0269V00     | A1235         | Creative Testing Solutions | 2025-02-01 00:00:00 |

            And I navigate to the external transfer page.
            When I choose customer name "<Customer Name>".
            And  I fill hospital transfer Id "<Hospital Order Reference ID>" and select the current date as transfer Date.
            Then I "should" be able to add products to the external transfer request.
            Examples:
                | Customer Name              | Hospital Order Reference ID |
                | Creative Testing Solutions | XYZ123                      |
                | Pioneer Health Services    | NULL_VALUE                  |



