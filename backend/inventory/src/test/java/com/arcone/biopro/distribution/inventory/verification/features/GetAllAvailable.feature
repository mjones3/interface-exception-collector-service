# Feature Unit Number reference: W036825003000
@api
Feature: Get all available inventories

    Scenario Outline: Get all available inventories
        Given I have the following units of products in inventory
            | Unit Number   | Units | Family                           | ABORh | Location   | Expires In Days | Temperature Category |
            | W036825003001 | 2     | PLASMA_TRANSFUSABLE              | OP    | LOCATION_1 | 2               | FROZEN               |
            | W036825003002 | 3     | PLASMA_TRANSFUSABLE              | OP    | LOCATION_2 | 2               | FROZEN               |
            | W036825003003 | 5     | RED_BLOOD_CELLS                  | OP    | LOCATION_1 | 2               | FROZEN               |
            | W036825003004 | 5     | RED_BLOOD_CELLS_LEUKOREDUCED     | OP    | LOCATION_1 | 2               | FROZEN               |
            | W036825003005 | 8     | PLASMA_TRANSFUSABLE              | ON    | LOCATION_1 | 2               | FROZEN               |
            | W036825003006 | 13    | PLASMA_TRANSFUSABLE              | ABN   | LOCATION_1 | 2               | FROZEN               |
            | W036825003007 | 21    | PLASMA_TRANSFUSABLE              | ABP   | LOCATION_1 | 31              | FROZEN               |
            | W036825003008 | 44    | PLASMA_TRANSFUSABLE              | OP    | LOCATION_1 | -1              | FROZEN               |
            | W036825003009 | 4     | WHOLE_BLOOD                      | OP    | LOCATION_1 | 5               | FROZEN               |
            | W036825003010 | 5     | WHOLE_BLOOD_LEUKOREDUCED         | OP    | LOCATION_2 | 5               | FROZEN               |
            | W036825003011 | 10    | WHOLE_BLOOD                      | ABN   | LOCATION_2 | 2               | FROZEN               |
            | W036825003012 | 12    | WHOLE_BLOOD_LEUKOREDUCED         | ABN   | LOCATION_1 | 2               | FROZEN               |
            | W036825003013 | 18    | WHOLE_BLOOD                      | ABP   | LOCATION_1 | 30              | FROZEN               |
            | W036825003014 | 7     | WHOLE_BLOOD_LEUKOREDUCED         | ABP   | LOCATION_1 | 30              | FROZEN               |
            | W036825003015 | 3     | WHOLE_BLOOD                      | ABN   | LOCATION_2 | -1              | FROZEN               |
            | W036825003016 | 1     | PLASMA_TRANSFUSABLE              | OP    | LOCATION_3 | 2               | REFRIGERATED         |
            | W036825003017 | 1     | PLASMA_TRANSFUSABLE              | OP    | LOCATION_3 | 31              | REFRIGERATED         |
            | W036825003018 | 1     | PLASMA_TRANSFUSABLE              | OP    | LOCATION_3 | 2               | FROZEN               |
            | W036825003019 | 1     | PLASMA_TRANSFUSABLE              | OP    | LOCATION_3 | 31              | FROZEN               |
            | W036825003020 | 1     | PLASMA_MFG_NONINJECTABLE         | OP    | LOCATION_4 | 1               | FROZEN               |
            | W036825003021 | 1     | PLASMA_MFG_INJECTABLE            | OP    | LOCATION_4 | 1               | FROZEN               |
            | W036825003022 | 1     | PRT_APHERESIS_PLATELETS          | OP    | LOCATION_5 | 2               | ROOM_TEMPERATURE     |
            | W036825003023 | 1     | APHERESIS_PLATELETS_LEUKOREDUCED | OP    | LOCATION_5 | 2               | ROOM_TEMPERATURE     |

        When I request available inventories for family with the following parameters:
            | Product Family   | Abo Rh Type   | Location   | Temperature Category   |
            | <Product Family> | <Abo Rh Type> | <Location> | <Temperature Category> |

        Then I receive "<Quantity>" of total products and "<Short Date Quantity>" of short date

        @LAB-81 @AOA-75
        Examples:
            | Quantity | Product Family               | Abo Rh Type | Location   | Short Date Quantity | Temperature Category |
            | 2        | PLASMA_TRANSFUSABLE          | OP          | LOCATION_1 | 2                   |                      |
            | 10       | PLASMA_TRANSFUSABLE          | O           | LOCATION_1 | 10                  |                      |
            | 44       | PLASMA_TRANSFUSABLE          | ANY         | LOCATION_1 | 23                  |                      |
            | 3        | PLASMA_TRANSFUSABLE          | ANY         | LOCATION_2 | 3                   |                      |
            | 5        | RED_BLOOD_CELLS              | ANY         | LOCATION_1 | 5                   |                      |
            | 5        | RED_BLOOD_CELLS_LEUKOREDUCED | ANY         | LOCATION_1 | 5                   |                      |
            | 13       | PLASMA_TRANSFUSABLE          | ABN         | LOCATION_1 | 13                  |                      |
            | 21       | PLASMA_TRANSFUSABLE          | ABP         | LOCATION_1 | 0                   |                      |
            | 0        | PLASMA_TRANSFUSABLE          | ANY         | LOCATION_7 | 0                   |                      |

        @LAB-257 @AOA-152
        Examples:
            | Quantity | Product Family           | Abo Rh Type | Location   | Short Date Quantity | Temperature Category |
            | 4        | WHOLE_BLOOD              | OP          | LOCATION_1 | 4                   |                      |
            | 5        | WHOLE_BLOOD_LEUKOREDUCED | OP          | LOCATION_2 | 5                   |                      |
            | 22       | WHOLE_BLOOD              | ANY         | LOCATION_1 | 4                   |                      |
            | 19       | WHOLE_BLOOD_LEUKOREDUCED | ANY         | LOCATION_1 | 12                  |                      |
            | 10       | WHOLE_BLOOD              | ABN         | LOCATION_2 | 10                  |                      |

        @LAB-379
        Examples:
            | Quantity | Product Family      | Abo Rh Type | Location   | Short Date Quantity | Temperature Category |
            | 4        | PLASMA_TRANSFUSABLE | ANY         | LOCATION_3 | 2                   |                      |
            | 2        | PLASMA_TRANSFUSABLE | ANY         | LOCATION_3 | 1                   | REFRIGERATED         |
            | 2        | PLASMA_TRANSFUSABLE | ANY         | LOCATION_3 | 1                   | FROZEN               |

        @LAB-415
        Examples:
            | Quantity | Product Family           | Abo Rh Type | Location   | Short Date Quantity | Temperature Category |
            | 1        | PLASMA_MFG_NONINJECTABLE | ANY         | LOCATION_4 | 0                   |                      |
            | 1        | PLASMA_MFG_INJECTABLE    | ANY         | LOCATION_4 | 0                   | FROZEN               |

        @LAB-412ó
        Examples:
            | Quantity | Product Family                   | Abo Rh Type | Location   | Short Date Quantity | Temperature Category |
            | 1        | PRT_APHERESIS_PLATELETS          | ANY         | LOCATION_5 | 1                   | ROOM_TEMPERATURE     |
            | 1        | APHERESIS_PLATELETS_LEUKOREDUCED | ANY         | LOCATION_5 | 1                   | ROOM_TEMPERATURE     |

    @LAB-81 @AOA-75 @LAB-257 @AOA-152
    Scenario: Get all available inventories grouped
        Given I have the following units of products in inventory
            | Unit Number   | Units | Family                       | ABORh | Location   | Expires In Days |
            | W036825003016 | 2     | PLASMA_TRANSFUSABLE          | OP    | LOCATION_1 | 2               |
            | W036825003017 | 3     | PLASMA_TRANSFUSABLE          | OP    | LOCATION_2 | 2               |
            | W036825003018 | 5     | RED_BLOOD_CELLS              | OP    | LOCATION_1 | 2               |
            | W036825003019 | 5     | RED_BLOOD_CELLS_LEUKOREDUCED | OP    | LOCATION_1 | 2               |
            | W036825003020 | 8     | PLASMA_TRANSFUSABLE          | ON    | LOCATION_1 | 2               |
            | W036825003021 | 13    | PLASMA_TRANSFUSABLE          | ABN   | LOCATION_1 | 2               |
            | W036825003022 | 21    | PLASMA_TRANSFUSABLE          | ABP   | LOCATION_1 | 31              |
            | W036825003023 | 4     | WHOLE_BLOOD                  | OP    | LOCATION_1 | 3               |
            | W036825003024 | 5     | WHOLE_BLOOD_LEUKOREDUCED     | OP    | LOCATION_1 | 3               |
            | W036825003025 | 10    | WHOLE_BLOOD                  | ABN   | LOCATION_1 | 6               |
            | W036825003026 | 12    | WHOLE_BLOOD_LEUKOREDUCED     | ABN   | LOCATION_1 | 6               |
            | W036825003027 | 3     | WHOLE_BLOOD                  | ABN   | LOCATION_1 | -1              |

        When I select "PLASMA_TRANSFUSABLE" of the blood type "O"
        And I select "PLASMA_TRANSFUSABLE" of the blood type "AB"
        And I select "RED_BLOOD_CELLS" of the blood type "ANY"
        And I select "RED_BLOOD_CELLS_LEUKOREDUCED" of the blood type "ANY"
        And I select "WHOLE_BLOOD_LEUKOREDUCED" of the blood type "AB"
        And I select "WHOLE_BLOOD" of the blood type "ANY"
        And I request available inventories in location "LOCATION_1"

        Then I receive "6" groups
        And I receive a group of product family "PLASMA_TRANSFUSABLE" and abo rh criteria "O" with "10" inventories and "10" product short date listed
        And I receive a group of product family "PLASMA_TRANSFUSABLE" and abo rh criteria "AB" with "34" inventories and "13" product short date listed
        And I receive a group of product family "RED_BLOOD_CELLS" and abo rh criteria "ANY" with "5" inventories and "5" product short date listed
        And I receive a group of product family "RED_BLOOD_CELLS_LEUKOREDUCED" and abo rh criteria "ANY" with "5" inventories and "5" product short date listed
        And I receive a group of product family "WHOLE_BLOOD_LEUKOREDUCED" and abo rh criteria "AB" with "12" inventories and "0" product short date listed
        And I receive a group of product family "WHOLE_BLOOD" and abo rh criteria "ANY" with "14" inventories and "4" product short date listed

    @LAB-259
    Scenario: Get all available inventories not considering the other statuses
        Given I have the following inventories:
            | Unit Number   | Product Code | Status     |
            | W036825003001 | E1624V00     | AVAILABLE  |
            | W036825003002 | E1624V00     | AVAILABLE  |
            | W036825003003 | E1624V00     | CONVERTED  |
            | W036825003004 | E1624V00     | DISCARDED  |
            | W036825003005 | E1624V00     | SHIPPED    |
            | W036825003006 | E1624V00     | IN_TRANSIT |
        When I select "PLASMA_TRANSFUSABLE" of the blood type "OP"
        And I request available inventories in location "123456789"
        Then I receive "1" groups
        And I receive a group of product family "PLASMA_TRANSFUSABLE" and abo rh criteria "OP" with "2" inventories and "2" product short date listed
