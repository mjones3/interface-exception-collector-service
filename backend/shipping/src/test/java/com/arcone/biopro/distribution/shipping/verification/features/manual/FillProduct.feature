@disabled @manual @ui @AOA-152
Feature: Manual Test of Fill Products.

#Manual Test of Fill Products - Fill Products - Product Code field enabled
#Date of testing:01/08/2025
#Manually tested and documented by: German Berros, Ruby Dizon
#Supported by:Ram Bishunkhe, Marcos Delgado
#Result:PASSED – Working as expected


    @DIS-231
        Scenario: Verify Unit Number field is auto-enabled and the Product Code and Visual Inspection fields disabled after successful product submission.
            Given I am filling a product and the visual inspection is configured,
            When I scan a valid unit number and a valid product code,
            And I choose Unsatisfactory for the visual inspection,
            And I choose the discard reason and submit,
            Then I see an acknowledgement message,
            When I confirm the acknowledgement message,
            Then I see the Unit Number field gets enabled,
            And I see the Product Code field remains disabled,
            And I see the Visual Inspection toggle buttons remain disabled.

    @DIS-232
        Scenario: Verify Product Code field disabled after successful product submission.
            Given I am filling a product, and the visual inspection is not configured,
            When I scan a valid unit number and a valid product code,
            Then I see the product added to the list,
            And I see the Unit Number field gets enabled,
            And I see the Product Code field remains disabled.

    @DIS-283
        Scenario: Verify that no Error messages displayed every time a valid product is scanned.
            Given I am filling a product, and the visual inspection is not configured,
            When I scan a valid unit number and a valid product code,
            Then I see the product added to the list,
            And No error messages are displayed.
