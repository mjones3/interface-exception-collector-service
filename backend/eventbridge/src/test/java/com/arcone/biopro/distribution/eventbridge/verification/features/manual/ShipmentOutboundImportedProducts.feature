#Manual Test of Imported Products - Shipping Outbound interface
#Date of testing: 06/20/2025
#Manually tested and documented by: German Berros, Ruby Dizon
#Result: PASSED – Working as expected
#Reviewed by: Archana Nallapeddi
#Review Date: 06/20/2025

@manual @disabled @AOA-109
Feature: PI-5 Imports - For Configured Products

    Rule: The system should outbound events with imported shipped products.
        @DIS-427
        Scenario: Closing a shipment with imported products and triggering a Shipment Completed Outbound event.
            Given there is a shipment with imported products,
            And the shipment is ready to be completed,
            When I select to complete the shipment,
            Then I should get a confirmation that the shipment was successfully completed,
            And I should confirm that all expected information related to the shipment and products, has been shared with external systems.

