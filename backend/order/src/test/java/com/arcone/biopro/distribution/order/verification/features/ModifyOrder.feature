@api @AOA-152
Feature: Modify Order

    Background:
        Given I cleaned up from the database the orders with external ID starting with "EXTDIS316".

    Rule: The modified order request must be rejected if the internal BioPro order is not in the 'Open' status.
        Rule: The modified order request must be rejected if the order doesn't exist.
    Rule: The modified order request must be rejected if the content of the request doesn't match the acceptable values configured in the system.
        Rule: The modified order request must be rejected for a backorder.
    Rule: The modified order date and time and the employee who modified the order must be displayed on the BioPro application.
        Rule: The modified order request details must be available in the BioPro application.
        @DIS-316
        Scenario: Modify a Biopro order from a Modify Order request event.
            Given I have orders with the following details.
                | External ID   | Status      | Location Code | Delivery Type | Shipping Method | Product Category | Product Family                                             | Blood Type | Quantity | Back Order |
                | EXTDIS3160001 | OPEN        | 123456789     | SCHEDULED     | FEDEX           | REFRIGERATED     | RED_BLOOD_CELLS_LEUKOREDUCED, RED_BLOOD_CELLS_LEUKOREDUCED | ABP,AP     | 5,20     | FALSE      |
                | EXTDIS3160002 | OPEN        | 123456789     | SCHEDULED     | FEDEX           | FROZEN           | PLASMA_TRANSFUSABLE, PLASMA_TRANSFUSABLE                   | B,ANY      | 5,10     | FALSE      |
                | EXTDIS3160003 | OPEN        | 123456789     | SCHEDULED     | FEDEX           | REFRIGERATED     | RED_BLOOD_CELLS, RED_BLOOD_CELLS                           | BP,OP      | 2,15     | FALSE      |
                | EXTDIS3160004 | IN_PROGRESS | 123456789     | SCHEDULED     | FEDEX           | FROZEN           | PLASMA_TRANSFUSABLE, PLASMA_TRANSFUSABLE                   | B,O        | 2,15     | FALSE      |
                | EXTDIS3160005 | OPEN        | 123456789     | SCHEDULED     | FEDEX           | REFRIGERATED     | RED_BLOOD_CELLS, RED_BLOOD_CELLS                           | BP,AP      | 5,20     | TRUE       |
                | EXTDIS3160006 | OPEN        | 123456789     | SCHEDULED     | FEDEX           | REFRIGERATED     | RED_BLOOD_CELLS, RED_BLOOD_CELLS                           | BP,OP      | 2,15     | FALSE      |
                | EXTDIS3160007 | OPEN        | 123456789     | SCHEDULED     | FEDEX           | REFRIGERATED     | RED_BLOOD_CELLS_LEUKOREDUCED, RED_BLOOD_CELLS_LEUKOREDUCED | ABP,AP     | 5,20     | FALSE      |
#
            And I have received modify order requests with the following details externalId.
                | Modify External ID | Modify Date         | Location Code | Delivery Type | Shipping Method | Product Category | Product Family                                    | Blood Type | Quantity | Modify Reason | Modify Employee Code                 |
                | EXTDIS3160001      | 2025-01-01 11:09:55 | DO1           | STAT          | FEDEX           | FROZEN           | PLASMA_TRANSFUSABLE                               | ANY        | 10       | Reason  1     | ee1bf88e-2137-4a17-835a-d43e7b738374 |
                | EXTDIS3160002      | 2025-01-01 11:09:55 | DO1           | STAT          | FEDEX           | REFRIGERATED     | WHOLE_BLOOD,WHOLE_BLOOD                           | AP,BP      | 10,15    | Reason  2     | ee1bf88e-2137-4a17-835a-d43e7b738374 |
                | EXTDIS3160003      | 2025-01-01 11:09:55 | DO1           | STAT          | FEDEX           | FROZEN           | RED_BLOOD_CELLS_LEUKOREDUCED                      | AP         | 10       | Reason  3     | ee1bf88e-2137-4a17-835a-d43e7b738374 |
                | EXTDIS3160004      | 2025-01-01 11:09:55 | DO1           | STAT          | FEDEX           | REFRIGERATED     | WHOLE_BLOOD,WHOLE_BLOOD                           | AP,ANY     | 10,15    | Reason  2     | ee1bf88e-2137-4a17-835a-d43e7b738374 |
                | EXTDIS3160005      | 2025-01-01 11:09:55 | DO1           | STAT          | FEDEX           | REFRIGERATED     | WHOLE_BLOOD_LEUKOREDUCED,WHOLE_BLOOD_LEUKOREDUCED | AP,ANY     | 10,15    | Reason  2     | ee1bf88e-2137-4a17-835a-d43e7b738374 |
                | EXTDIS3160006      | 2025-01-01 11:09:55 | DO1           | STAT          | FEDEX           | REFRIGERATED     | PLASMA_TRANSFUSABLE,PLASMA_TRANSFUSABLE           | A,ANY      | 10,15    | Reason  2     | ee1bf88e-2137-4a17-835a-d43e7b738374 |
                | EXTDIS3160007      | 2025-31-77 11:09:55 | DO1           | STAT          | FEDEX           | FROZEN           | PLASMA_TRANSFUSABLE                               | ANY        | 10       | Reason  1     | ee1bf88e-2137-4a17-835a-d43e7b738374 |
            When The system processes the modify order requests.
            Then The Modify order request should be processed as.
                | Modify External ID | Location Code | Should be Found? | Should be Updated? |
                | EXTDIS3160001      | DO1           | YES              | YES                |
                | EXTDIS3160002      | DO1           | YES              | YES                |
                | EXTDIS3160003      | 123456789     | YES              | NO                 |
                | EXTDIS3160004      | 123456789     | YES              | NO                 |
                | EXTDIS3160005      | 123456789     | YES              | NO                 |
                | EXTDIS3160000      | 123456789     | NO               | NO                 |
                | EXTDIS3160007      | 123456789     | YES              | NO                 |
