#Manual Test of Product Details columns order
#Date of testing: 07/25/2025
#Manually tested and documented by:  German Berros, Ruby Dizon
#Result: PASSED – Working as expected
#Reviewed by: Archana Nallapeddi
#Review Date: 07/25/2025

@manual @disabled @AOA-36
Feature: Automated Orders - Apheresis Plasma

    Rule: I should be able to view the product details columns in the following order: Product Family, Blood Type, Quantity, Available Inventory and Filled Products.
    @DIS-97 @DIS-512
    Scenario: Product Details columns are displayed in the correct order on the Order Details page.
    Given I am on the Order Details page for a valid order
    When I view the Product Details section
    Then I should see the columns in the following order: Product Family, Blood Type, Quantity, Available Inventory, Filled Products.


    Rule: I should be able to view the Order Comments in the pick list.
    @DIS-121 @DIS-512
    Scenario: Display order comments in the pick list
    Given I am on the Order Details page of an order
    And the order includes a comment
    When I select to view the pick list
    Then the pick list should open
    And the Order Comments section should be visible
    And it should display the same comment shown on the Order Details page.


    Rule: I should not be able to view the View Pick List on the shipping details page.
    @DIS-121 @DIS-512
    Scenario: Pick list not accessible from Shipment Details page
     Given I am on the Order Details page of an order
     When I select to go to the Shipment Details page
     Then I should be redirected to the Shipment Details page
     And I should not see an option to view the pick list.


    Rule: I should not see a clock icon in the Filled section on both Order Details and Shipment Details pages.
    @DIS-148 @DIS-512
    Scenario: Clock icon is not displayed on Order Details and in Shipment Details pages
    Given I am on the Order Details page for a valid order
    When I check the number of products filled
    Then I should not see a clock icon
    When I navigate to the Shipment Details page for the same order
    And I check the number of products filled
    Then I should not see a clock icon.

