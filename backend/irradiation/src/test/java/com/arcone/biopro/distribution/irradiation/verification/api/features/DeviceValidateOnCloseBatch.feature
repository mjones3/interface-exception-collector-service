@api @LAB-617 @AOA-61 @hzd9
Feature: Validate device and close batch

    As a distribution user
    I want to scan an irradiation device and close the batch
    So that I can complete the irradiation process for all products in the batch


    Scenario: Successfully scan device and view batch products for closing
        Given I have a device "AUTO-DEVICE0010" at location "123456789" with status "ACTIVE"
        And the device has an active batch with products:
            | unitNumber    | productCode |
            | W777725003001 | E0869V00    |
            | W777725003002 | E0868V00    |
            | W777725003003 | E0867V00    |
        When I scan the device "AUTO-DEVICE0010" at location "123456789" for batch closing
        Then I should see all products in the batch

    Scenario: Device not in my location
        Given I have a device "AUTO-DEVICE0011" at location "987654321" with status "ACTIVE"
        When I scan the device "AUTO-DEVICE0011" at location "123456789" for batch closing
        Then I should see notification "Device not in current location"


    Scenario: batch is closed or not associated with given device
        Given I have a device "AUTO-DEVICE0012" at location "123456789" with status "ACTIVE"
        And the device is not associated with any open batch
        When I scan the device "AUTO-DEVICE0012" at location "123456789" for batch closing
        Then I should see notification "Device is not listed in any open batch"
