Feature: Scan Irradiation Device
    As a distribution specialist
    I want to scan the irradiator device and validate the irradiator is not already in use
    So that I do not initiate a batch in an irradiator that is in use

    Scenario: Successfully scan and validate available irradiation device
        Given I have a device "AUTO-DEVICE004" at location "123456789" with status "ACTIVE"
        When I scan the device "AUTO-DEVICE004" at location "123456789"
        Then the device validation should be successful

    Scenario: Device location mismatch
        Given I have a device "AUTO-DEVICE005" at location "987654321" with status "ACTIVE"
        When I scan the device "AUTO-DEVICE005" at location "123456789"
        Then I should see a notification "Device is not in your location"

    Scenario: Check if batch is completed
        Given I have a batch "BATCH001" for device "AUTO-DEVICE007" with end time null
        When I query the batch "BATCH001" status
        Then the batch should be active
