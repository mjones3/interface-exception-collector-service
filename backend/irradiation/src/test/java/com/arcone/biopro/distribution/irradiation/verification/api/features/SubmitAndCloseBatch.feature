# Feature Unit Number reference: W777725004xxx

@api @LAB-622 @LAB-620 @AOA-61 @hzd9
Feature: Submit and close batch

  Background:
    Given I have a device "AUTO-DEVICE200" at location "123456789" with status "ACTIVE"
    And I have an existing batch with products:
      | Unit Number   | Product Code | Lot Number | Expiration Date |
      | W777725004001 | E068500      | LOT001     | 2024-03-15      |
      | W777725004002 | E068600      | LOT001     | 2024-02-10      |
      | W777725004003 | E453100      | LOT001     | 2024-01-25      |

  Scenario: I successfully close batch with all products irradiated
    When I complete the batch with end time "2024-01-15T12:00:00Z" and items:
      | Unit Number   | Product Code | Is Irradiated |
      | W777725004001 | E068500      | true          |
      | W777725004002 | E068600      | true          |
      | W777725004003 | E453100      | true          |
    Then the batch should be successfully completed
    And I should see the batch completion success message "Batch completed successfully"
    And all products should be updated with new product codes:
        | Unit Number   | Original Product Code | New Product Code |
        | W777725004001 | E068500               | E066800          |
        | W777725004002 | E068600               | E066900          |
        | W777725004003 | E453100               | E452600          |
    And product modified events should be published for all irradiated items with expiration dates:
      | Unit Number   | Product Code | Expiration Date | Expiration Time |
      | W777725004001 | E066800      | 02/12/2024      | 23:59           |
      | W777725004002 | E066900      | 02/10/2024      | 23:59           |
      | W777725004003 | E452600      | 01/25/2024      | 23:59           |

  Scenario: I successfully close batch with some products not irradiated
    When I complete the batch with end time "2024-01-15T12:00:00Z" and items:
      | Unit Number   | Product Code | Is Irradiated |
      | W777725004001 | E068500      | true          |
      | W777725004002 | E068600      | false         |
      | W777725004003 | E453100      | false         |
    Then the batch should be successfully completed
    And I should see the batch completion success message "Batch completed successfully"
    And irradiated products should be updated with new product codes:
      | Unit Number   | Original Product Code | New Product Code |
      | W777725004001 | E068500              | E066800          |
    And product modified events should be published for irradiated items only with expiration date:
      | Unit Number   | Product Code | Expiration Date | Expiration Time |
      | W777725004001 | E066800      | 02/12/2024      | 23:59           |
    And quarantine events should be published for non-irradiated items with reason "IRRADIATION_INCOMPLETE"
    And I should see quarantine notification "Products not irradiated have been quarantined"

  Scenario: I successfully close batch with no products irradiated
    When I complete the batch with end time "2024-01-15T12:00:00Z" and items:
      | Unit Number   | Product Code | Is Irradiated |
      | W777725004001 | E068500      | false         |
      | W777725004002 | E068600      | false         |
      | W777725004003 | E453100      | false         |
    Then the batch should be successfully completed
    And I should see the batch completion success message "Batch completed successfully"
    And no product modified events should be published
    And quarantine events should be published for all items with reason "IRRADIATION_INCOMPLETE"
    And I should see quarantine notification "Products not irradiated have been quarantined"
