# Feature Unit Number reference: W777725020000
@api @cleanUpAll
Feature: Quarantine update it to product ProductQuarantine Event - API
    As a lab specialist I want the system is able to add a quarantine for a product for any possible reason.

    Scenario Outline: Update Quarantine to product with different reasons.
        Given I have a unit number "<Unit Number>" with product "E1624V00" that is "AVAILABLE"
        When I received an Apply Quarantine event for unit "<Unit Number>" and product "E1624V00" with reason "Under investigation", id "<Id 1>" and stopsManufacturing mark as "<Stop manufacturing true>"
        Then The inventory status has quarantine

        When I received an Apply Quarantine event for unit "<Unit Number>" and product "E1624V00" with reason "BCA Unit Needed", id "<Id 2>" and stopsManufacturing mark as "<Stop manufacturing false>"
        Then The inventory status has quarantine

        When I received a Update Quarantine event for unit "<Unit Number>" and product "E1624V00" with reason "BCA Unit Needed", id "<Id 1>" and stopsManufacturing "<Stop manufacturing false>"
        Then Result of flagged as Stop manufacturing in inventory is "<Result stop manufacturing>"

        Examples:
            | Id 1 | Id 2 | Unit Number   | Stop manufacturing true | Stop manufacturing false | Result stop manufacturing |
            | 1    | 2    | W777725011006 | true                    | false                    | false                     |
            | 1    | 2    | W777725011007 | false                   | true                     | true                      |
