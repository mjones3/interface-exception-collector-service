@manual @disabled @ui @AOA-89
Feature: Print Carton Label

#Manual Test of Print Carton Label

#Date of testing:05/29/2025
#Manually tested and documented by:German Berros, Ruby Dizon
#Supported by:Michel Risucci, Allan Morelli Braga
#Result:PASSED – Working as expected


        Rule: I should be able to print the carton label with the shipment details, carton details, customer details and products information.
        Rule: The carton label should be printed on 4x4 label stock.
        @DIS-344
        Scenario: Successfully printing of carton label with required details on standard label format.
        Given a carton in the shipment has been successfully closed,
        And the details related to the shipment, the carton, the customer, and the product information are available,
        When I select to print the carton label,
        Then I should see the shipment details, carton details, customer details, and product information displayed on the label,
        And the label should be printed on 4x4 label stock.


        Rule: I should be able to reprint carton label if needed.
        @DIS-344
        Scenario: Reprint of previously generated carton label with same content.
        Given a carton label has already been printed,
        When I choose to reprint the label for the same carton,
        Then the system should allow me to reprint it with the same details as the original.


        Rule: The Transportation Reference Number displayed on the carton label is configurable.
        Rule: The Transportation Reference Number must be printed only if configured.
        @DIS-344
        Scenario Outline: Display of Transportation Reference Number based on configuration.
            Given the Transportation Reference Number is configurable,
            And it has been configured as “<config_setting>”
            When I choose to print the carton label,
            Then the Transportation Reference Number “<visibility>” on the label.
            Examples:
                | config_setting | visibility        |
                | Y              | should appear     |
                | N              | should not appear |


        Rule: The display format of the carton sequence number on the carton label is configurable.
        Rule: The carton sequence number must be displayed as n of ______ if configured by the blood center
        , where n is the current carton number and __ is the total number of cartons in the shipment that is manually entered.
        @DIS-344
        Scenario Outline: Display of carton sequence number format based on configuration.
        Given the display format of the carton sequence number is configurable,
        And it has been configured as “<config_setting>” in the system,
        When I choose to print the carton label,
        Then the carton sequence number should be displayed as “<visibility>” on the label.
            Examples:
                | config_setting | visibility                              |
                | Y              | Carton Sequence in Shipment n of ______ |
                | N              | Carton Sequence in Shipment n           |
