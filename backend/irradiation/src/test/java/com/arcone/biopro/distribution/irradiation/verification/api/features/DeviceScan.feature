Feature: Scan Irradiation Device
    As a distribution specialist
    I want to scan the irradiator device and validate the irradiator is not already in use
    So that I do not initiate a batch in an irradiator that is in use

    Scenario: Successfully scan and validate available irradiation device
        Given I have a device "DEVICE001" at location "123456789" with status "ACTIVE"
        When I scan the device "DEVICE001" at location "123456789"
        Then the device validation should be successful
