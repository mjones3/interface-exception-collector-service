@api @LAB-140
Feature: Product Recovered event
    Rule: As a inventory service I want to listen to product recovered event so that I can remove the discard status from the inventory and return product to the previous status.
        Scenario Outline: Revert the inventory to the previous status after receiving a Product Recovered event.
            Given I have a Discarded Product in Inventory with previous status "<Previous Status>"
            When I received a Product Recovered event
            Then The inventory status is "<Previous Status>"

            Examples:
                | Previous Status |
                | AVAILABLE       |
