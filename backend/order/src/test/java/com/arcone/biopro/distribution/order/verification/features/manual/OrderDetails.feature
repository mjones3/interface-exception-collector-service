@disabled @manual @ui
Feature: Order Details

#    Date of testing: 01/22/2025
#    Manually tested and documented by: Ruby Dizon, German Berros
#    Supported by: Michel Risucci, Benjamin Pinto
#    Result: PASSED – All working as expected.

#    Escaped defect found by RC while testing on RC-Demo environment. [DIS-295]

        Rule: I should see the storage location on the Pick List when a listed short-dated product has a storage location defined in the system.
        @DIS-295 @bug
        Scenario: Display of Short-Dated Products in Pick List with storage location.
            Given there are short-dated products in inventory with a storage location defined,
            And I am in the Order Details page,
            And I can see the number of products available in the Available Inventory column,
            When I select to view the Pick List,
            Then the Pick List page displays a list of short-dated products with their storage location.

        Rule: I should see the available inventory amount, even when there are products in the system without defined storage locations.
        Rule: I should see the message “No storage location available” on the Pick List when a listed short-dated product does not have a storage location defined in the system.
        @DIS-295 @bug
        Scenario: Display of Short-Dated Products in Pick List with no storage location info.
            Given there are short-dated products in inventory with no storage locations defined,
            And I am in the Order Details page,
            And I can see the number of products available in the Available Inventory column,
            When I select to view the Pick List,
            Then the Pick List page displays "no storage location available"
