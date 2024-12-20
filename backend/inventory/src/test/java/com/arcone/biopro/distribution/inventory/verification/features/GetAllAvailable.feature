@api
Feature: Get all available inventories

    Scenario Outline: Get all available inventories
        Given I have "2" products of family "PLASMA_TRANSFUSABLE" with ABORh "OP" in location "LOCATION_1" and that will expire in "2" days
        And I have "3" products of family "PLASMA_TRANSFUSABLE" with ABORh "OP" in location "LOCATION_2" and that will expire in "2" days
        And I have "5" products of family "RED_BLOOD_CELLS_LEUKOREDUCED" with ABORh "OP" in location "LOCATION_1" and that will expire in "2" days
        And I have "8" products of family "PLASMA_TRANSFUSABLE" with ABORh "ON" in location "LOCATION_1" and that will expire in "2" days
        And I have "13" products of family "PLASMA_TRANSFUSABLE" with ABORh "ABN" in location "LOCATION_1" and that will expire in "2" days
        And I have "21" products of family "PLASMA_TRANSFUSABLE" with ABORh "ABP" in location "LOCATION_1" and that will expire in "31" days
        And I have "44" products of family "PLASMA_TRANSFUSABLE" with ABORh "OP" in location "LOCATION_1" and that will expire in "-1" days
        And I have "4" products of family "WHOLE_BLOOD" with ABORh "OP" in location "LOCATION_1" and that will expire in "5" days
        And I have "5" products of family "WHOLE_BLOOD_LEUKOREDUCED" with ABORh "OP" in location "LOCATION_2" and that will expire in "5" days
        And I have "10" products of family "WHOLE_BLOOD" with ABORh "ABN" in location "LOCATION_2" and that will expire in "2" days
        And I have "12" products of family "WHOLE_BLOOD_LEUKOREDUCED" with ABORh "ABN" in location "LOCATION_1" and that will expire in "2" days
        And I have "18" products of family "WHOLE_BLOOD" with ABORh "ABP" in location "LOCATION_1" and that will expire in "30" days
        And I have "7" products of family "WHOLE_BLOOD_LEUKOREDUCED" with ABORh "ABP" in location "LOCATION_1" and that will expire in "30" days
        And I have "3" products of family "WHOLE_BLOOD" with ABORh "ABN" in location "LOCATION_2" and that will expire in "-1" days
        When I request available inventories for family "<Product Family>" and ABORh "<Abo Rh Type>" in location "<Location>"
        Then I receive "<Quantity>" of total products and "<Short Date Quantity>" of short date

        @LAB-81 @AOA-75
        Examples:
            | Quantity | Product Family               | Abo Rh Type | Location   | Short Date Quantity |
            | 2        | PLASMA_TRANSFUSABLE          | OP          | LOCATION_1 | 2                   |
            | 10       | PLASMA_TRANSFUSABLE          | O           | LOCATION_1 | 10                  |
            | 44       | PLASMA_TRANSFUSABLE          | ANY         | LOCATION_1 | 23                  |
            | 3        | PLASMA_TRANSFUSABLE          | ANY         | LOCATION_2 | 3                   |
            | 5        | RED_BLOOD_CELLS_LEUKOREDUCED | ANY         | LOCATION_1 | 5                   |
            | 13       | PLASMA_TRANSFUSABLE          | ABN         | LOCATION_1 | 13                  |
            | 21       | PLASMA_TRANSFUSABLE          | ABP         | LOCATION_1 | 0                   |
            | 0        | PLASMA_TRANSFUSABLE          | ANY         | LOCATION_7 | 0                   |

        @LAB-257 @AOA-152
        Examples:
            | Quantity | Product Family           | Abo Rh Type | Location   | Short Date Quantity |
            | 4        | WHOLE_BLOOD              | OP          | LOCATION_1 | 4                   |
            | 5        | WHOLE_BLOOD_LEUKOREDUCED | OP          | LOCATION_2 | 5                   |
            | 22       | WHOLE_BLOOD              | ANY         | LOCATION_1 | 4                   |
            | 19       | WHOLE_BLOOD_LEUKOREDUCED | ANY         | LOCATION_1 | 12                  |
            | 10       | WHOLE_BLOOD              | ABN         | LOCATION_2 | 10                  |

    @LAB-81 @AOA-75 @LAB-257 @AOA-152
    Scenario: Get all available inventories grouped
        Given I have "2" products of family "PLASMA_TRANSFUSABLE" with ABORh "OP" in location "LOCATION_1" and that will expire in "2" days
        And I have "3" products of family "PLASMA_TRANSFUSABLE" with ABORh "OP" in location "LOCATION_2" and that will expire in "2" days
        And I have "5" products of family "RED_BLOOD_CELLS_LEUKOREDUCED" with ABORh "OP" in location "LOCATION_1" and that will expire in "2" days
        And I have "8" products of family "PLASMA_TRANSFUSABLE" with ABORh "ON" in location "LOCATION_1" and that will expire in "2" days
        And I have "13" products of family "PLASMA_TRANSFUSABLE" with ABORh "ABN" in location "LOCATION_1" and that will expire in "2" days
        And I have "21" products of family "PLASMA_TRANSFUSABLE" with ABORh "ABP" in location "LOCATION_1" and that will expire in "31" days
        And I have "4" products of family "WHOLE_BLOOD" with ABORh "OP" in location "LOCATION_1" and that will expire in "3" days
        And I have "5" products of family "WHOLE_BLOOD_LEUKOREDUCED" with ABORh "OP" in location "LOCATION_1" and that will expire in "3" days
        And I have "10" products of family "WHOLE_BLOOD" with ABORh "ABN" in location "LOCATION_1" and that will expire in "6" days
        And I have "12" products of family "WHOLE_BLOOD_LEUKOREDUCED" with ABORh "ABN" in location "LOCATION_1" and that will expire in "6" days
        And I have "3" products of family "WHOLE_BLOOD" with ABORh "ABN" in location "LOCATION_1" and that will expire in "-1" days

        When I select "PLASMA_TRANSFUSABLE" of the blood type "O"
        And I select "PLASMA_TRANSFUSABLE" of the blood type "AB"
        And I select "RED_BLOOD_CELLS_LEUKOREDUCED" of the blood type "ANY"
        And I select "WHOLE_BLOOD_LEUKOREDUCED" of the blood type "AB"
        And I select "WHOLE_BLOOD" of the blood type "ANY"
        And I request available inventories in location "LOCATION_1"

        Then I receive "5" groups
        And I receive a group of product family "PLASMA_TRANSFUSABLE" and abo rh criteria "O" with "10" inventories and "10" product short date listed
        And I receive a group of product family "PLASMA_TRANSFUSABLE" and abo rh criteria "AB" with "34" inventories and "13" product short date listed
        And I receive a group of product family "RED_BLOOD_CELLS_LEUKOREDUCED" and abo rh criteria "ANY" with "5" inventories and "5" product short date listed
        And I receive a group of product family "WHOLE_BLOOD_LEUKOREDUCED" and abo rh criteria "AB" with "12" inventories and "0" product short date listed
        And I receive a group of product family "WHOLE_BLOOD" and abo rh criteria "ANY" with "14" inventories and "4" product short date listed

