# Feature Unit Number reference: W777725001000

@ui @disabled
#    This scenario was disabled because it's covered in all the other UI scenarios
Feature: Irradiation Menu

    Rule: I should be able to select an option to begin the Irradiation process.

        Scenario: Irradiation Menu
            Given I login to Manufacturing module
            And I select the location "MDL Hub 1"
            When I navigate to "Orders"
            Then I verify that I am taken to the page "Search Orders" in "Order"
