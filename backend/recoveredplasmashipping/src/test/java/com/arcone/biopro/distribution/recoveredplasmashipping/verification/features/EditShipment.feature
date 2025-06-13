@AOA-89
Feature: Edit Shipment


    Background:
        Given I have removed from the database all the configurations for the location "123456789_DIS351".
        And I have removed from the database all shipments which code contains with "DIS35100".
        And The location "123456789_DIS351" is configured with prefix "DIS_351", shipping code "DIS35100", carton prefix "BPM" and prefix configuration "Y".

        Rule: I should be able to modify the customer, product type, transportation number and ship date when the status is OPEN.
        Rule: I should be required to provide a comment for modifying the shipment.
        Rule: I should be able to see the history of modifications from a shipment.
        @ui @DIS-351
        Scenario: Successful edit a shipment
            Given I have a shipment created with the Customer Code as "409" , Product Type as "RP_NONINJECTABLE_LIQUID_RT", Carton Tare Weight as "100", Shipment Date as "<tomorrow>", Transportation Reference Number as "DIS-351" and Location Code as "123456789_DIS351".
            And I navigate to the shipment details page for the last shipment created.
            Then I should see the following shipment information:
                | Field                      | Value                      |
                | Shipment Number Prefix     | DIS_351DIS351              |
                | Customer Code              | 409                        |
                | Customer Name              | SOUTHERN BIOLOGICS         |
                | Product Type               | RP NONINJECTABLE LIQUID RT |
                | Shipment Status            | OPEN                       |
                | Shipment Date              | <tomorrow>                 |
                | Transportation Ref. Number | DIS-351                    |
                | Total Cartons              | 0                          |
            And The edit shipment option should be "enabled".
            When I choose to edit the shipment.
            Then I should see the following fields in edit form:
                | Field                           | Value                      | Status  |
                | Customer                        | Southern Biologics         | enabled |
                | Product Type                    | RP NONINJECTABLE LIQUID RT | enabled |
                | Shipment Date                   | <tomorrow_formatted>       | enabled |
                | Transportation Reference Number | DIS-351                    | enabled |
            And I have entered all the fields:
                | Field                           | Value                      |
                | Customer                        | Prothya                    |
                | Product Type                    | RP FROZEN WITHIN 120 HOURS |
                | Shipment Date                   | <tomorrow>                 |
                | Transportation Reference Number | DIS-351 modified           |
                | Comments                        | Testing Edit Comments      |
            When I choose to submit the shipment.
            Then I should see a "SUCCESS" message: "Shipment updated successfully".
            And I should see the following shipment information:
                | Field                      | Value                      |
                | Shipment Number Prefix     | DIS_351DIS351              |
                | Customer Code              | 408                        |
                | Customer Name              | Prothya                    |
                | Product Type               | RP FROZEN WITHIN 120 HOURS |
                | Shipment Status            | OPEN                       |
                | Shipment Date              | <tomorrow>                 |
                | Transportation Ref. Number | DIS-351 MODIFIED           |
                | Total Cartons              | 0                          |
            When I switch to the Shipment History tab.
            Then I should see the following rows in the history table:
                | User                                 | Date              | Comments              |
                | 4c973896-5761-41fc-8217-07c5d13a004b | <today_formatted> | Testing Edit Comments |


        Rule: I should not be able to modify the customer, product type when the status is not OPEN.
        Rule: I should be able to modify the ship date and transportation number when the status is not OPEN.
        @ui @DIS-351
        Scenario: Successful edit a shipment that is not open
            Given I have a "IN_PROGRESS" shipment with the Customer Code as "409" , Product Type as "RP_NONINJECTABLE_LIQUID_RT", Carton Tare Weight as "100", Shipment Date as "<tomorrow>", Transportation Reference Number as "DIS-351", Location Code as "123456789_DIS351" and the unit numbers as "W036898351757" and product codes as "E6022V00" and product types "RP_NONINJECTABLE_LIQUID_RT".
            And I navigate to the shipment details page for the last shipment created.
            Then I should see the following shipment information:
                | Field                      | Value                      |
                | Shipment Number Prefix     | DIS_351DIS351              |
                | Customer Code              | 409                        |
                | Customer Name              | SOUTHERN BIOLOGICS         |
                | Product Type               | RP NONINJECTABLE LIQUID RT |
                | Shipment Status            | IN PROGRESS                |
                | Shipment Date              | <tomorrow>                 |
                | Transportation Ref. Number | DIS-351                    |
                | Total Cartons              | 1                          |
            And The edit shipment option should be "enabled".
            When I choose to edit the shipment.
            Then I should see the following fields in edit form:
                | Field                           | Value                      | Status   |
                | Customer                        | Southern Biologics         | disabled |
                | Product Type                    | RP NONINJECTABLE LIQUID RT | disabled |
                | Shipment Date                   | <tomorrow_formatted>       | enabled  |
                | Transportation Reference Number | DIS-351                    | enabled  |
            And I have entered all the fields:
                | Field                           | Value                 |
                | Shipment Date                   | <tomorrow>            |
                | Transportation Reference Number | DIS-351 modified      |
                | Comments                        | Testing Edit Comments |
            When I choose to submit the shipment.
            Then I should see a "SUCCESS" message: "Shipment updated successfully".
            And I should see the following shipment information:
                | Field                      | Value                      |
                | Shipment Number Prefix     | DIS_351DIS351              |
                | Customer Code              | 409                        |
                | Customer Name              | SOUTHERN BIOLOGICS         |
                | Product Type               | RP NONINJECTABLE LIQUID RT |
                | Shipment Status            | IN PROGRESS                |
                | Shipment Date              | <tomorrow>                 |
                | Transportation Ref. Number | DIS-351 modified           |
                | Total Cartons              | 1                          |



        Rule: I should not be able to modify the shipment when the status is PROCESSING.
        @api @DIS-351
        Scenario: Edit shipment processing status
            Given I have a "PROCESSING" shipment with the Customer Code as "409" , Product Type as "RP_NONINJECTABLE_LIQUID_RT", Carton Tare Weight as "100", Shipment Date as "<tomorrow>", Transportation Reference Number as "DIS-351", Location Code as "123456789_DIS351" and the unit numbers as "W036898351757" and product codes as "E6022V00" and product types "RP_NONINJECTABLE_LIQUID_RT".
            When I request to edit a shipment with the values:
                | Field                           | Value                      |
                | Customer Code                   | 408                        |
                | Product Type                    | RP_FROZEN_WITHIN_120_HOURS |
                | Carton Tare Weight              | 1000                       |
                | Shipment Date                   | <tomorrow>                 |
                | Transportation Reference Number | <null>                     |
                | Location Code                   | 123456789_TEST             |
                | Comments                        | Testing Edit Comments      |
            Then I should receive a "WARN" message response "Shipment is processing and cannot be modified".


        Rule: I should be able to modify the customer, product type, transportation number and ship date when the status is OPEN.
        Rule: I should not be able to modify the customer, product type when the status is not OPEN.
        Rule: I should be required to provide a comment for modifying the shipment.
        Rule: I should be able to see the history of modifications from a shipment.
        @api @DIS-351
        Scenario Outline: Edit shipment multiple statuses
            Given I have a "<Shipment Status>" shipment with the Customer Code as "409" , Product Type as "RP_NONINJECTABLE_LIQUID_RT", Carton Tare Weight as "100", Shipment Date as "<tomorrow>", Transportation Reference Number as "DIS-351", Location Code as "123456789_DIS351" and the unit numbers as "W036898351757" and product codes as "E6022V00" and product types "RP_NONINJECTABLE_LIQUID_RT".
            When I request to edit a shipment with the values:
                | Field                           | Value                      |
                | Customer Code                   | 408                        |
                | Product Type                    | RP_FROZEN_WITHIN_120_HOURS |
                | Carton Tare Weight              | 2000                       |
                | Shipment Date                   | <next_week>                |
                | Transportation Reference Number | DIS-351-updated            |
                | Location Code                   | 123456789_TEST             |
                | Comments                        | Comments DIS-351           |
            Then I should receive a "SUCCESS" message response "Shipment updated successfully".
            And The following fields should be updated "<ModifiedFields>".
            When I request the shipment modify history.
            Then The modify history should contain "<ShipmentHistory>".
            Examples:
                | Shipment Status | ModifiedFields                                                          | ShipmentHistory                                               |
                | OPEN            | productType,transportationReferenceNumber,shipmentDate,cartonTareWeight | 5db1da0b-6392-45ff-86d0-17265ea33226,<today>,Comments DIS-351 |
                | IN_PROGRESS     | shipmentDate,transportationReferenceNumber                              | 5db1da0b-6392-45ff-86d0-17265ea33226,<today>,Comments DIS-351 |
                | CLOSED          | shipmentDate,transportationReferenceNumber                              | 5db1da0b-6392-45ff-86d0-17265ea33226,<today>,Comments DIS-351 |


        Rule: I should be able to see the history of modifications from a shipment.
        @api @DIS-351
        Scenario Outline: Shipment History
            Given I have a "<Shipment Status>" shipment with the Customer Code as "409" , Product Type as "RP_NONINJECTABLE_LIQUID_RT", Carton Tare Weight as "100", Shipment Date as "<tomorrow>", Transportation Reference Number as "DIS-351", Location Code as "123456789_DIS351" and the unit numbers as "W036898351757" and product codes as "E6022V00" and product types "RP_NONINJECTABLE_LIQUID_RT".
            When I request to edit a shipment with the values:
                | Field                           | Value                      |
                | Customer Code                   | 408                        |
                | Product Type                    | RP_FROZEN_WITHIN_120_HOURS |
                | Carton Tare Weight              | 2000                       |
                | Shipment Date                   | <next_week>                |
                | Transportation Reference Number | DIS-351-updated            |
                | Location Code                   | 123456789_TEST             |
                | Comments                        | Comments DIS-351           |
            Then I should receive a "SUCCESS" message response "Shipment updated successfully".
            And The following fields should be updated "<ModifiedFields>".
            When I request the shipment modify history.
            Then The modify history should contain "<ShipmentHistory>".
            Examples:
                | Shipment Status | ModifiedFields                                                          | ShipmentHistory                                               |
                | OPEN            | productType,transportationReferenceNumber,shipmentDate,cartonTareWeight | 5db1da0b-6392-45ff-86d0-17265ea33226,<today>,Comments DIS-351 |
                | IN_PROGRESS     | shipmentDate,transportationReferenceNumber                              | 5db1da0b-6392-45ff-86d0-17265ea33226,<today>,Comments DIS-351 |
                | CLOSED          | shipmentDate,transportationReferenceNumber                              | 5db1da0b-6392-45ff-86d0-17265ea33226,<today>,Comments DIS-351 |


        Rule: I should be able to see the history of modifications from a shipment.
        @api @DIS-351
        Scenario Outline: Shipment History for multiple edits
            Given I have a "<Shipment Status>" shipment with the Customer Code as "409" , Product Type as "RP_NONINJECTABLE_LIQUID_RT", Carton Tare Weight as "100", Shipment Date as "<tomorrow>", Transportation Reference Number as "DIS-351", Location Code as "123456789_DIS351" and the unit numbers as "W036898351757" and product codes as "E6022V00" and product types "RP_NONINJECTABLE_LIQUID_RT".
            When I request to edit a shipment with the values:
                | Field                           | Value                      |
                | Customer Code                   | 408                        |
                | Product Type                    | RP_FROZEN_WITHIN_120_HOURS |
                | Carton Tare Weight              | 2000                       |
                | Shipment Date                   | <today>                    |
                | Transportation Reference Number | DIS-351-updated            |
                | Location Code                   | 123456789_TEST             |
                | Comments                        | Comments DIS-351 first     |
            Then I should receive a "SUCCESS" message response "Shipment updated successfully".
            When I request to edit a shipment with the values:
                | Field                           | Value                      |
                | Customer Code                   | 409                        |
                | Product Type                    | RP_NONINJECTABLE_LIQUID_RT |
                | Carton Tare Weight              | 100                        |
                | Shipment Date                   | <next_week>                |
                | Transportation Reference Number | DIS-351-updated            |
                | Location Code                   | 123456789_TEST             |
                | Comments                        | Comments DIS-351 second    |
            Then I should receive a "SUCCESS" message response "Shipment updated successfully".
            When I request the shipment modify history.
            Then The modify history should contain "<ShipmentHistory>".
            Examples:
                | Shipment Status | ShipmentHistory                                                                                                                          |
                | OPEN            | 5db1da0b-6392-45ff-86d0-17265ea33226,<today>,Comments DIS-351 first;5db1da0b-6392-45ff-86d0-17265ea33226,<today>,Comments DIS-351 second |
