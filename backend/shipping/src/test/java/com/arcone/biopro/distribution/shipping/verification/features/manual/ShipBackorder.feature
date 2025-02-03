@manual @disabled @AOA-152
Feature: Ship Backorders

#        Date of testing: 01/30/2025
#        Manually tested and documented by: German Berros, Ruby Dizon
#        Supported by:
#        Result: Passed


    @DIS-286
    Scenario: Fulfilling a back order
        Given the order with External ID 1 and BioPro Order ID 1 has been partially completed,
        And I have selected the back order with External ID 1 and BioPro Order ID 2,
        When I navigate to the Order Details page and open the Pick List,
        Then I see the Pick List with requested products and any available short-dated products,
        And a shipment with a Shipment ID is generated,
        And the order status is updated to “In Progress.”

    @DIS-286
    Scenario: Filling products to a back order
        Given I am on the Fill Products page,
        When I enter valid unit numbers and product codes,
        And I mark the visual inspection as Satisfactory (if configured),
        Then the products are added to the Packed Products section.

    @DIS-286
    Scenario: Completing a back order
        When I verify all products filled
        And I select to complete the shipment,
        Then the shipment is completed,
        And the order and shipment statuses are updated to “Completed.”

    @DIS-286
    Scenario Outline: Shipping several back orders with same external ID
        Given I have an order with External Order ID “<EXTERNAL_ID>”, BioPro Order ID “<BIOPRO_ID>” and the products requested are “<TOTAL_PRODUCTS>”,
        When I ship 1 product from this order,
        And the order “<BIOPRO ID>” is marked as COMPLETED,
        Then the system creates a new back order with External Order ID “<EXTERNAL_ID>”, BioPro Order ID “<NEW_BIOPRO_ID>” and the products requested are “<REMAINING_PRODUCTS>”.

        Examples:
            | EXTERNAL_ID | BIOPRO_ID | TOTAL_PRODUCTS | NEW_BIOPRO_ID | REMAINING_PRODUCTS |
            | 1           | 1         | 4              | 2             | 3                  |
            | 1           | 2         | 3              | 3             | 2                  |
            | 1           | 3         | 2              | 4             | 1                  |
            | 1           | 4         | 1              | N/A           | 0                  |
