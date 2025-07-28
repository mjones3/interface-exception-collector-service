#Manual Test of Pick List Printing
#Date of testing: 07/23/2025
#Manually tested and documented by: Ruby Dizon, German Berros
#Result: PASSED – Working as expected
#Reviewed by: Archana Nallapeddi
#Review Date: 07/25/2025

@manual @disabled
Feature: Ship Apheresis Plasma

    Rule: I should be able to print the pick list with the order criteria and short-dated products information, if available
    @DIS-27
    Scenario: Print pick list that includes the order criteria and short-dated products information.
    Given I am in the Order Details page
    When I select to print the pick list
    Then the pick list is generated and displayed, including the order criteria and short-dated products information, if available.


    Rule: I should be able to print the packing slip in the pdf format for each box when the shipment is completed.
     @DIS-50
     Scenario: Print packing slip in pdf format after shipment completion.
     Given I am in the Shipment Details page
     And the shipment has been completed
     When I select to print the packing slip
     Then the packing slip is generated and displayed in PDF format.


    Rule: I should be able to view and print the shipping label in pdf format when the shipment is completed.
     @DIS-51
     Scenario: Print shipping label in pdf format after shipment completion.
     Given I am in the Shipment Details page
     And the shipment has been completed
     When I select to print the shipping label
     Then the shipping label is generated and displayed in PDF format.

