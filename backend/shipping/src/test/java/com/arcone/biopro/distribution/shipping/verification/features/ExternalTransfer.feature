@AOA-22
Feature: External Transfers

    Rule: I should be able to record the transfer customer, transfer date, and hospital order reference ID.
       @api @DIS-302
        Scenario Outline: Enter External Transfer Information
        Given I have shipped the following products to customer "<Customer Code From>".
            | Unit Number   | Product Code |
            | W036810946300 | E0869V00     |
            | W036810946301 | E0169V00     |
            | W036810946302 | E0269V00     |

           When I create an external transfer request to the customer "<Customer Code To>", hospital transfer id "<Transfer ID>" and transfer date "<Transfer Date>".
           Then I should have an external transfer request created.
            Examples:
                | Transfer Date | Customer Code To     | Customer Code From | Transfer ID |
                | 01-03-2025    | Random Hospital Inc. | A1235              | 123         |
                | 01-03-2025    | Random Hospital Inc. | A1236              | NULL_VALUE  |


    Rule: I should be alerted and not proceed ahead if the transfer date entered is in the future.
        Rule: I should not be able to proceed if transfer customer and transfer date are not valid.
        @api @DIS-302
        Scenario Outline: Enter Invalid Transfer Information
            Given I have shipped the following products to customer "<Customer Code From>".
                | Unit Number   | Product Code |
                | W036810946300 | E0869V00     |
                | W036810946301 | E0169V00     |
                | W036810946302 | E0290V00     |

            When I create an external transfer request to the customer "<Customer Code To>", hospital transfer id "<Transfer ID>" and transfer date "<Transfer Date>".
            Then I should receive a "Warning" message response "<Message>".
            Examples:
                | Transfer Date | Customer Code To | Customer Code From | Transfer ID | Message                  |
                | 01-03-2025    | ABC              | A1235              | 123         | Customer Name is Invalid |
                | 01-03-2025    | A1235            | A1236              | NULL_VALUE  | Transfer Date is Invalid |


    Rule: I should be able to record the transfer customer, transfer date, and hospital order reference ID.
        @ui @DIS-302
        Scenario Outline: Enter External Transfer Information
            Given I have shipped the following products to customer "<Customer Code From>".
                | Unit Number   | Product Code |
                | W036810946300 | E0869V00     |
                | W036810946301 | E0169V00     |
                | W036810946302 | E0290V00     |

            And I navigate to the external transfer page.
            When I choose customer name "<Customer Name>",
            When I fill hospital transfer Id "<Hospital Order Reference ID>" and transfer Date "<Transfer Date>".
            Then I "should" be able to add products to the external transfer request.
            Examples:
                | Transfer Date | Customer Code From | Customer Name        | Hospital Order Reference ID |
                | 01/03/2024    | A1235              | Random Hospital Inc. | XYZ123                      |
                | 01/03/2024    | A1236              | Hospital Inc.        | NULL_VALUE                  |



