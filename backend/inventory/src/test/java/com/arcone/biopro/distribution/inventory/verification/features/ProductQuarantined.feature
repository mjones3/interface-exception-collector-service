# Feature Unit Number reference: W777725020000
@api @cleanUpAll
Feature: Quarantine add it to product ProductQuarantine Event - API
    As a lab specialist I want the system is able to add a quarantine for a product for any possible reason.

    Scenario Outline: Add Quarantine to product with different reasons and stopManofactoring True .
        Given I have a unit number "<Unit Number>" with product "E1624V00" that is "AVAILABLE"
        When I received an Apply Quarantine event for unit "<Unit Number>" and product "E1624V00" with reason "Under investigation", id "<Id>" and stopManufacturing mark as "<Stop manufacturing>"
        Then The inventory status has quarantine
        And Result of flagged as Stop manufacturing in inventory is "<Result stop manufacturing>"

        Examples:
            | Id | Unit Number   | Stop manufacturing | Result stop manufacturing |
            | 1  | W777725011003 | true               | true                      |

    Scenario Outline: Add Quarantine to product with different reasons with stopManofacturing false.
        Given I have a unit number "<Unit Number>" with product "E1624V00" that is "AVAILABLE"
        When I received an Apply Quarantine event for unit "<Unit Number>" and product "E1624V00" with reason "ABS Positive", id "<Id>" and stopManufacturing mark as "<Stop manufacturing>"
        Then The inventory status has quarantine
        And Result of flagged as Stop manufacturing in inventory is "<Result stop manufacturing>"

        Examples:
            | Id | Unit Number   | Stop manufacturing | Result stop manufacturing |
            | 1  | W777725011004 | false              | false                     |

    Scenario Outline: Add Quarantine to product with different reasons with stopManofacturing false.
        Given I have a unit number "<Unit Number>" with product "E1624V00" that is "AVAILABLE"
        When I received an Apply Quarantine event for unit "<Unit Number>" and product "E1624V00" with reason "ABS Positive", id "<Quarantine 1 Id>" and stopManufacturing mark as "<Stop manufacturing Quarantine 1>"
        Then The inventory status has quarantine
        And Result of flagged as Stop manufacturing in inventory is "<Result stop manufacturing 1>"

        When I received an Apply Quarantine event for unit "<Unit Number>" and product "E1624V00" with reason "Under investigation", id "<Quarantine 2 Id>" and stopManufacturing mark as "<Stop manufacturing Quarantine 2>"
        Then The inventory status has quarantine
        And Result of flagged as Stop manufacturing in inventory is "<Result stop manufacturing 2>"

        Examples:
            | Quarantine 1 Id | Unit Number   | Stop manufacturing Quarantine 1 | Result stop manufacturing 1 | Quarantine 2 Id | Stop manufacturing Quarantine 2 | Result stop manufacturing 2 |
            | 1               | W777725011005 | false                           | false                       | 2               | true                            | true                        |


