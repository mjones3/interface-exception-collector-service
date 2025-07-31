# Feature Unit Number reference: W777725005xxx

@api @hzd9 @613 @AOA-61
Feature: Out of Storage Time Quarantine

    Rule: As a distribution specialist, I want the system to trigger a Quarantine if the product exceeds the out of storage time so that products are reviewed prior to being placed into available inventory.

    Scenario Outline: Product quarantined when out of storage time is exceeded
        Given I have an irradiation batch with the following products:
            | Unit Number  | Product Code  | Status    | Location  | Product Family  | Device         | Lot Number |
            | <unitNumber> | <productCode> | AVAILABLE | 123456789 | <productFamily> | AUTO-DEVICE500 | LOT001     |
        And the irradiation batch for unit "<unitNumber>" was started "<batchStartMinutes>" minutes ago
        When I receive a product stored event for unit "<unitNumber>" with product "<productCode>" stored "<storageMinutes>" minutes ago
        Then the product "<unitNumber>" should be quarantined with reason "OUT_OF_STORAGE_TIME_EXCEEDED"

        Examples:
            | unitNumber    | productCode | productFamily                    | batchStartMinutes | storageMinutes |
            | W777725005001 | E003300     | RED_BLOOD_CELLS                  | 60                | 29             |
            | W777725005002 | E1624V00    | APHERESIS_PLATELETS_LEUKOREDUCED | 1500              | 59             |
            | W777725005004 | E003300     | RED_BLOOD_CELLS_LEUKOREDUCED     | 60                | 29             |

    Scenario: Product is not quarantined when within out of storage time limit
        Given I have an irradiation batch with the following products:
            | Unit Number   | Product Code | Status    | Location  | Product Family  | Device         | Lot Number |
            | W777725005003 | E003300      | AVAILABLE | 123456789 | RED_BLOOD_CELLS | AUTO-DEVICE500 | LOT001     |
        And the irradiation batch for unit "W777725005003" was started "60" minutes ago
        When I receive a product stored event for unit "W777725005003" with product "E003300" stored "40" minutes ago
        Then the product "W777725005003" should not be quarantined

    Scenario: Multiple products in same unit - only exceeded product is quarantined
        Given I have an irradiation batch with the following products:
            | Unit Number   | Product Code | Status    | Location  | Product Family                   | Device         | Lot Number |
            | W777725005006 | E003300      | AVAILABLE | 123456789 | RED_BLOOD_CELLS                  | AUTO-DEVICE500 | LOT001     |
            | W777725005006 | E1624V00     | AVAILABLE | 123456789 | APHERESIS_PLATELETS_LEUKOREDUCED | AUTO-DEVICE500 | LOT001     |
        And the irradiation batch for unit "W777725005006" was started "60" minutes ago
        When I receive a product stored event for unit "W777725005006" with product "E003300" stored "29" minutes ago
        Then the product "W777725005006" should be quarantined with reason "OUT_OF_STORAGE_TIME_EXCEEDED"
        When I receive a product stored event for unit "W777725005006" with product "E1624V00" stored "20" minutes ago
        Then the product "W777725005006" should not be quarantined for product "E1624V00"

    Scenario: Product stored event ignored when batch is open
        Given I have an open irradiation batch with the following products:
            | Unit Number   | Product Code | Status    | Location  | Product Family  | Device         | Lot Number |
            | W777725005007 | E003300      | AVAILABLE | 123456789 | RED_BLOOD_CELLS | AUTO-DEVICE500 | LOT001     |
        When I receive a product stored event for unit "W777725005007" with product "E003300" stored "29" minutes ago
        Then the product "W777725005007" should not be quarantined for product "E003300"
        And the timing rule validated flag should be "false" for unit "W777725005007" and product "E003300"

    Scenario: Product stored event ignored when already processed - within limit first
        Given I have an irradiation batch with the following products:
            | Unit Number   | Product Code | Status    | Location  | Product Family  | Device         | Lot Number |
            | W777725005008 | E003300      | AVAILABLE | 123456789 | RED_BLOOD_CELLS | AUTO-DEVICE500 | LOT001     |
        And the irradiation batch for unit "W777725005008" was started "60" minutes ago
        When I receive a product stored event for unit "W777725005008" with product "E003300" stored "35" minutes ago
        Then the product "W777725005008" should not be quarantined for product "E003300"
        And the timing rule validated flag should be "true" for unit "W777725005008" and product "E003300"
        When I receive a product stored event for unit "W777725005008" with product "E003300" stored "15" minutes ago
        Then the product "W777725005008" should not be quarantined for product "E003300"
        And the system should log that the product stored event was already processed for unit "W777725005008" and product "E003300"

    Scenario: Product stored event ignored when already processed - quarantine first
        Given I have an irradiation batch with the following products:
            | Unit Number   | Product Code | Status    | Location  | Product Family  | Device         | Lot Number |
            | W777725005009 | E003300      | AVAILABLE | 123456789 | RED_BLOOD_CELLS | AUTO-DEVICE500 | LOT001     |
        And the irradiation batch for unit "W777725005009" was started "60" minutes ago
        When I receive a product stored event for unit "W777725005009" with product "E003300" stored "20" minutes ago
        Then the product "W777725005009" should be quarantined with reason "OUT_OF_STORAGE_TIME_EXCEEDED"
        And the timing rule validated flag should be "true" for unit "W777725005009" and product "E003300"
        When I receive a product stored event for unit "W777725005009" with product "E003300" stored "29" minutes ago
        Then the product "W777725005009" should not be quarantined for product "E003300"
        And the system should log that the product stored event was already processed for unit "W777725005009" and product "E003300"

