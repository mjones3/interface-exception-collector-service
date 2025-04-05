Feature: Add Cartons to Shipment

    Background:
        Given I am logged in as a Distribution Technician
        And I am at location "MH1"
        And there exists a shipment with number "SHP2024001"

    Scenario: Successfully add a carton to shipment with auto-generated unique number
        When I initiate adding a new carton to the shipment
        Then a unique carton number should be generated with format:
            | Partner Prefix | Location Code | Sequence Number |
            | BPM           | MH1           | 1234            |
        And the generated carton number should be "BPMMH11234"
        When I confirm adding the carton
        Then I should see a success notification "Carton BPMMH11234 has been added to the shipment"
        And the carton should appear in the shipment's carton list
        And the carton sequence number should be incremented for the next carton

    Scenario: Attempt to add carton with duplicate number
        Given a carton with number "BPMMH11234" already exists
        When I attempt to add a carton with the same number "BPMMH11234"
        Then I should see an error notification "Carton number must be unique for the blood center"
        And the carton should not be added to the shipment

    Scenario: View carton details after addition
        Given a carton "BPMMH11234" exists in the shipment
        When I select the carton from the list
        Then I should see the following carton details:
            | Field           | Value      |
            | Carton Number   | BPMMH11234 |
            | Location Code   | MH1        |
            | Sequence Number | 1234       |
            | Partner Prefix  | BPM        |

    Scenario: View sequential carton numbers in shipment
        Given I have added multiple cartons to the shipment
        When I view the list of cartons
        Then I should see the cartons in sequential order
        And each carton should have an incrementing sequence number
        And the carton list should display:
            | Carton Number | Sequence Number |
            | BPMMH11234   | 1234           |
            | BPMMH11235   | 1235           |
            | BPMMH11236   | 1236           |

    Scenario: Attempt to add carton from different location
        Given I am logged in at location "MH1"
        When I attempt to add a carton with location code "MH2"
        Then I should see an error notification "Cannot add cartons from different location"
        And the carton should not be added to the shipment

    Scenario: View shipment details with updated carton count
        Given I have added a carton to the shipment
        When I view the shipment details
        Then I should see the updated total number of cartons
        And I should see the following shipment information:
            | Field         | Value       |
            | Total Cartons | 1           |
            | Shipment ID   | SHP2024001  |

    Scenario: System failure while adding carton
        Given the system encounters an error while adding a carton
        When I attempt to add a new carton
        Then I should see an error notification "Failed to add carton. Please try again"
        And the carton should not be added to the shipment

    Scenario: View list of cartons in shipment
        Given multiple cartons have been added to the shipment
        When I view the shipment's carton list
        Then I should see all cartons associated with the shipment
        And for each carton I should see:
            | Field           | Value      |
            | Carton Number   | BPMMH11234 |
            | Location Code   | MH1        |
            | Sequence Number | 1234       |
            | Status         | Active      |
