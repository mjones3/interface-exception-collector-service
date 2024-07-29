Feature: Health Check
    As an administrator
    I want to verify that the application is running
    So that I can ensure the service is available
    @api
    Scenario: Application is up and running
        Given the application is started
        When I check the health endpoint
        Then the response status should be 200
