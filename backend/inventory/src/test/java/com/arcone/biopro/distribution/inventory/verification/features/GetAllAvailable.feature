@api
Feature: Get all available inventories

    Scenario Outline: Get all available inventories
        Given I have "2" of the "PLASMA_TRANSFUSABLE" of the blood type "OP" in the "LOCATION_1" will expire in "2" days
        And I have "3" of the "PLASMA_TRANSFUSABLE" of the blood type "OP" in the "LOCATION_2" will expire in "2" days
        And I have "5" of the "RED_BLOOD_CELLS" of the blood type "OP" in the "LOCATION_1" will expire in "2" days
        And I have "8" of the "PLASMA_TRANSFUSABLE" of the blood type "ON" in the "LOCATION_1" will expire in "2" days
        And I have "13" of the "PLASMA_TRANSFUSABLE" of the blood type "ABN" in the "LOCATION_1" will expire in "2" days
        And I have "21" of the "PLASMA_TRANSFUSABLE" of the blood type "ABP" in the "LOCATION_1" will expire in "31" days
        And I have "44" of the "PLASMA_TRANSFUSABLE" of the blood type "OP" in the "LOCATION_1" will expire in "-1" days
        When I request "<Product Family>" of the blood type "<Abo Rh Type>" in the "<Location>"

        Then I receive "<Quantity>" of total products and "<Short Date Quantity>" of short date

        Examples:
            | Quantity | Product Family      | Abo Rh Type | Location   | Short Date Quantity |
            | 2        | PLASMA_TRANSFUSABLE | OP          | LOCATION_1 | 2                   |
            | 10       | PLASMA_TRANSFUSABLE | O           | LOCATION_1 | 10                  |
            | 44       | PLASMA_TRANSFUSABLE | ANY         | LOCATION_1 | 23                  |
            | 3        | PLASMA_TRANSFUSABLE | ANY         | LOCATION_2 | 3                   |
            | 5        | RED_BLOOD_CELLS     | ANY         | LOCATION_1 | 5                   |
            | 13       | PLASMA_TRANSFUSABLE | ABN         | LOCATION_1 | 13                  |
            | 21       | PLASMA_TRANSFUSABLE | ABP         | LOCATION_1 | 0                   |
            | 0        | PLASMA_TRANSFUSABLE | ANY         | LOCATION_7 | 0                   |

    Scenario: Get all available inventories grouped
        Given I have "2" of the "PLASMA_TRANSFUSABLE" of the blood type "OP" in the "LOCATION_1" will expire in "2" days
        And I have "3" of the "PLASMA_TRANSFUSABLE" of the blood type "OP" in the "LOCATION_2" will expire in "2" days
        And I have "5" of the "RED_BLOOD_CELLS" of the blood type "OP" in the "LOCATION_1" will expire in "2" days
        And I have "8" of the "PLASMA_TRANSFUSABLE" of the blood type "ON" in the "LOCATION_1" will expire in "2" days
        And I have "13" of the "PLASMA_TRANSFUSABLE" of the blood type "ABN" in the "LOCATION_1" will expire in "2" days
        And I have "21" of the "PLASMA_TRANSFUSABLE" of the blood type "ABP" in the "LOCATION_1" will expire in "31" days

        When I select "PLASMA_TRANSFUSABLE" of the blood type "O"
        And I select "PLASMA_TRANSFUSABLE" of the blood type "AB"
        And I select "RED_BLOOD_CELLS" of the blood type "ANY"
        And I request to location "LOCATION_1"

        Then I receive "3" groups
        And I receive a group of product family "PLASMA_TRANSFUSABLE" and abo rh criteria "O" with "10" inventories and "10" product short date listed
        And I receive a group of product family "PLASMA_TRANSFUSABLE" and abo rh criteria "AB" with "34" inventories and "13" product short date listed
        And I receive a group of product family "RED_BLOOD_CELLS" and abo rh criteria "ANY" with "5" inventories and "5" product short date listed

