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
        Then the device validation should fail with error "Device not in current location"

    Scenario: Device already in use
        Given I have an open batch for device "AUTO-DEVICE007"
        When I scan the device "AUTO-DEVICE007"
        Then the device validation should fail with error "Device already in use"
