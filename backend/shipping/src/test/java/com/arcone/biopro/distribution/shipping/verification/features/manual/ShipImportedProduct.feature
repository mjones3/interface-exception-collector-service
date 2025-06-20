#Manual Test of Shipping of Imported Products
#Date of testing: 06/17/2025
#Manually tested and documented by: Ruby Dizon, German Berros
#Result: PASSED – Working as expected
#Reviewed by: Archana Nallapeddi
#Review Date: 06/18/2025

@manual @disabled
Feature: Ship Imported Product

Rule: I should be prevented to ship an unacceptable imported product.
  @DIS-415
  Scenario Outline: Prevent shipping of unacceptable imported products.
    Given there is an imported product in inventory with unit number "<UNIT_NUMBER>" that has been "<STATUS>"
    When I attempt to ship the product during "<STEP_OF_PROCESS>"
    Then the system should prevent the shipment
    And the system should display a message stating the specific problem
    And the unacceptable imported product should not be added to the shipment

  Examples:
    | UNIT_NUMBER    | STATUS      | STEP_OF_PROCESS  |
    |                | DISCARDED   | Manage Products  |
    |                | QUARANTINED | Manage Products  |
    |                | QUARANTINED | Verify Products  |
    |                | DISCARDED   | Verify Products  |
    | W085112340001  | SHIPPED     | Manage Products  |
    | W085112340011  | SHIPPED     | Verify Products  |


Rule: I should be able to ship an imported product.
  @DIS-415
  Scenario: Successfully shipping of an acceptable imported product
    Given there is an imported product in inventory that is acceptable to be shipped
    When I attempt to add that imported product to the shipment
    Then the system should accept it and allow the shipment
    And mark the product as SHIPPED