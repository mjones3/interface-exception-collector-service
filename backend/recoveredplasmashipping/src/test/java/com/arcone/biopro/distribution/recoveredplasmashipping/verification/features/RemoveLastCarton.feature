@AOA-89
Feature: Remove Last Carton from Shipment

    Background:
        Given I have removed from the database all the configurations for the location "123456789_DIS357".
        And I have removed from the database all shipments which code contains with "DIS35700".
        #And I have removed from the database all shipments from location "123456789_DIS357" with transportation ref number "DIS-357".
        And The location "123456789_DIS357" is configured with prefix "DIS_357", shipping code "DIS35700", carton prefix "BPM" and prefix configuration "Y".


    Rule: I should only be able to remove the last carton of a shipment.
    Rule: I should see a success message when the carton is removed.
    @api @DIS-357
    Scenario: Successfully removing the carton from a shipment
        Given I have a "IN_PROGRESS" shipment with the Customer Code as "409" , Product Type as "RP_NONINJECTABLE_LIQUID_RT", Carton Tare Weight as "100", Shipment Date as "<tomorrow>", Transportation Reference Number as "DIS-357", Location Code as "123456789_DIS357" and the unit numbers as "W036898357757,W036898357758,W036898357756" and product codes as "E6022V00,E2534V00,E5880V00" and product types "RP_NONINJECTABLE_LIQUID_RT,RP_NONINJECTABLE_FROZEN,RP_FROZEN_WITHIN_72_HOURS".
        And The carton sequence "1, 2, 3" has status as "CLOSED, OPEN, OPEN".
        When I request to remove the carton sequence "1" from the shipment.
        Then I should receive a "SYSTEM" message response "Carton remove error. Contact Support.".
        When I request the last created shipment data.
        Then The find shipment response should have the following information:
            | Information          | Value                |
            | Total Cartons        | 3                    |
            | Carton Number Prefix | BPMMH1,BPMMH1,BPMMH1 |
            | Sequence Number      | 1,2,3                |
            | Carton Status        | CLOSED,OPEN,OPEN     |

        When I request to remove the carton sequence "3" from the shipment.
        Then I should receive a "SUCCESS" message response "Carton successfully removed".
        When I request the last created shipment data.
        Then The find shipment response should have the following information:
            | Information          | Value         |
            | Total Cartons        | 2             |
            | Carton Number Prefix | BPMMH1,BPMMH1 |
            | Sequence Number      | 1,2           |
            | Carton Status        | CLOSED,OPEN   |
            | Shipment Status      | IN_PROGRESS   |


    Rule: I should not be able to remove a carton when the shipment status is closed.
    @api @DIS-357
    Scenario: Attempt to remove a carton from a closed shipment
        Given I have a "CLOSED" shipment with the Customer Code as "409" , Product Type as "RP_NONINJECTABLE_LIQUID_RT", Carton Tare Weight as "100", Shipment Date as "<tomorrow>", Transportation Reference Number as "DIS-357", Location Code as "123456789_DIS357" and the unit numbers as "W036898357757,W036898357758,W036898357756" and product codes as "E6022V00,E2534V00,E5880V00" and product types "RP_NONINJECTABLE_LIQUID_RT,RP_NONINJECTABLE_FROZEN,RP_FROZEN_WITHIN_72_HOURS".
        When I request to remove the carton sequence "1" from the shipment.
        Then I should receive a "SYSTEM" message response "Carton remove error. Contact Support.".
        When I request the last created shipment data.
        Then The find shipment response should have the following information:
            | Information          | Value                |
            | Total Cartons        | 3                    |
            | Carton Number Prefix | BPMMH1,BPMMH1,BPMMH1 |
            | Sequence Number      | 1,2,3                |
            | Carton Status        | CLOSED,CLOSED,CLOSED |



    Rule: I should not be able to remove a closed carton
        @api @DIS-357
        Scenario: Attempt to remove a closed carton
            Given I have a "IN_PROGRESS" shipment with the Customer Code as "409" , Product Type as "RP_NONINJECTABLE_LIQUID_RT", Carton Tare Weight as "100", Shipment Date as "<tomorrow>", Transportation Reference Number as "DIS-357", Location Code as "123456789_DIS357" and the unit numbers as "W036898357757,W036898357758,W036898357756" and product codes as "E6022V00,E2534V00,E5880V00" and product types "RP_NONINJECTABLE_LIQUID_RT,RP_NONINJECTABLE_FROZEN,RP_FROZEN_WITHIN_72_HOURS".
            And The carton sequence "1, 2, 3" has status as "CLOSED, CLOSED, CLOSED ".
            When I request to remove the carton sequence "1" from the shipment.
            Then I should receive a "SYSTEM" message response "Carton remove error. Contact Support.".
            When I request the last created shipment data.
            Then The find shipment response should have the following information:
                | Information          | Value                |
                | Total Cartons        | 3                    |
                | Carton Number Prefix | BPMMH1,BPMMH1,BPMMH1 |
                | Sequence Number      | 1,2,3                |
                | Carton Status        | CLOSED,CLOSED,CLOSED |


    Rule: I should be requested to confirm the removal of a carton.
    Rule: I should only be able to remove the last carton of a shipment.
    Rule: I should see a success message when the carton is removed.
    Rule: The system should update the status of the shipment back to OPEN when there is only one carton and it is removed.
        @ui @DIS-357
        Scenario: Successfully removing the last carton from a shipment.
            Given I have a "IN_PROGRESS" shipment with the Customer Code as "409" , Product Type as "RP_NONINJECTABLE_LIQUID_RT", Carton Tare Weight as "100", Shipment Date as "<tomorrow>", Transportation Reference Number as "DIS-357", Location Code as "123456789_DIS357" and the unit numbers as "W036898357757" and product codes as "E6022V00" and product types "RP_NONINJECTABLE_LIQUID_RT".
            And The carton sequence "1" has status as "OPEN ".
            And I navigate to the shipment details page for the last shipment created.
            Then I should see the following shipment information:
                | Field                      | Value                      |
                | Shipment Number Prefix     | DIS_357DIS357              |
                | Customer Code              | 409                        |
                | Customer Name              | SOUTHERN BIOLOGICS         |
                | Product Type               | RP NONINJECTABLE LIQUID RT |
                | Shipment Status            | IN PROGRESS                |
                | Shipment Date              | <tomorrow>                 |
                | Transportation Ref. Number | DIS-357                    |
                | Total Cartons              | 1                          |
                | Carton Status              | BPMMH1,1,OPEN              |
            And The remove option should be available for the carton number prefix "BPMMH1" and sequence number "1" and status "OPEN".
            When I choose to remove the carton number prefix "BPMMH1" and sequence number "1" and status "OPEN".
            Then I should see a "Remove Confirmation" acknowledgment message: "Carton will be removed. Are you sure you want to continue?".
            When I confirm to remove the carton.
            Then I should see a "SUCCESS" message: "Carton successfully removed".
            When I request the last created shipment data.
            Then The find shipment response should have the following information:
                | Information                | Value                      |
                | Shipment Number Prefix     | DIS_357DIS357              |
                | Customer Code              | 409                        |
                | Customer Name              | SOUTHERN BIOLOGICS         |
                | Product Type               | RP NONINJECTABLE LIQUID RT |
                | Shipment Status            | OPEN                       |
                | Shipment Date              | <tomorrow>                 |
                | Transportation Ref. Number | DIS-357                    |
                | Total Cartons              | 0                          |



