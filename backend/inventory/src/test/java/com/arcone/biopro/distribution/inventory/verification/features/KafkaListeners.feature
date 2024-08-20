@api
Feature: Kafka listeners

    Scenario Outline: Application is listening and creating/updating inventory from kafka events
        Given I am listening the "<Event>" event
        When I receive an event "<Event>" event
        Then The inventory status is "<Status>"

        Examples:
            | Event              | Status      |
            | Label Applied      | AVAILABLE   |
