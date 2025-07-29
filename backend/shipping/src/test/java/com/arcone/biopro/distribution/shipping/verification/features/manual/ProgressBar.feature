@manual @disabled @ui @AOA-152
Feature: Manual Test of Progress Bar Enhancement.

#Manual Test of Progress Bar Enhancement
#Date of testing: 05/12/2025
#Manually tested and documented by: Ruby Dizon
#Supported by: Ram Bishunkhe
#Result: PASSED – Working as expected



    @DIS-201 @DIS-21
     Scenario: Verify Progress Bar Enhancement.
        Given I am filling an order,
        When I scan a valid unit number and a valid product code,
        And I choose Satisfactory for the visual inspection,
        Then I see the progress bar advance with the number of products filled
        And I see the percentage fulfilled increases
        And I see the progress button in yellow
        When I continue to add the products to fulfill the quantity requested
        Then I see the quantity matches the request
        And I see the Progress Bar turn green
        And I see the percentage of 100% fulfilled
