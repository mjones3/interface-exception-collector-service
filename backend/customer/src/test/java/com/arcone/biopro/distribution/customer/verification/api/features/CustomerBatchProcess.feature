@api @AOA-140 @cleanup
Feature: Customer Batch Information - API

    Background:
        Given I receive a batch update request "<payload>" json
            | payload            |
            | batch-request.json |
        And I have the following test customers existing in the system
            | External ID | Name     | Code | Department Code | Department Name | Phone Number | Foreign Flag | Customer Type | Status | Contact Name | Address Type | Address Line 1                                | Address Line 2 | City    | State    | Postal Code | Country | District |
            | TEST-001    | John Doe | 1901 | ship123         | Shipping        | 1234567890   | M            | 0             | Y      | Doe          | Office       | Jean Baptiste Point du Sable Lake Shore Drive |                | Chicago | Illinois | 60618       | USA     |          |

    @R20-932
    Scenario: Batch request processed with event publish for an existing customer
        Given The batch request has the customer with "<External ID>"
        When The request has a valid data to be updated
        Then The batch request should be processed
        And An event should be published with "<status>"
            | External ID | status    |
            | TEST-001	  | COMPLETED |

    @R20-932
    Scenario: Batch request processed with event publish for an non-existing customer
        Given The batch request has the customer with "<External ID>"
        When The request has a valid data to be updated
        Then The batch request should be processed
        And An event should be published with "<status>"
            | External ID | status    |
            | TEST-002    | COMPLETED |

    @R20-932
    Scenario: Batch request processed with event publish for an invalid customer
        Given The batch request has the customer with "<External ID>"
        When The request has a valid data to be updated
        Then The batch request should be processed
        And An event should be published with "<status>"
            | External ID | status |
            | TEST-003    | FAILED |
