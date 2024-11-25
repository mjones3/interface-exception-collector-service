@ui @AOA-40
Feature: Second Verification Notification Tab
    As a distribution technician,
    I want to rescan products that have been flagged as unsuitable during the second verification process,
    So that I can remove them from the shipment.

    Background:
        Given I cleaned up from the database the packed item that used the unit number "W822530106087,W822530106089,W822530106088,W822530106090,W822530106091,W822530106092, W822530106093".
        And I cleaned up from the database, all shipments with order number "118,119,120,121, 122, 123".

    Rule: I should see a notification stating that the units should be rescanned to be removed.
    Rule: I should be able to scan the unit number and product code of the products identified as unsuitable.
    Rule: I should be able to see and confirm an acknowledgment message every time an unsuitable product is rescanned (discarded, quarantined, etc.).
    Rule: I should have an option to fill more products only when all the unsuitable products are removed.
    Rule: I should be able to see the progress bar that reflects the number of products removed.
    Rule: I should see the list of removed products.
    Rule: I should be able to fill more products to replace the units removed.
    Rule: I should be able to complete the shipment once all unacceptable products are removed.
    @DIS-207
    Scenario Outline: Second verification unsuitable products - remove units.
        Given I have a shipment for order "<Order Number>" with the units "<Suitable UN>,<Unsuitable UN>" and product codes "<Suitable Code>,<Unsuitable Code>" "verified".
        And The verified unit "<Unsuitable UN>" is unsuitable with status "<Unsuitable Status>" and message "<Message>".
        And The second verification configuration is "enabled".
        And I am on the verify products page with "notifications" tab active.
        And I should see a notification banner: "One or more products have changed status. You must rescan the products to be removed.".
        And The fill more products option should be "disabled".
        When I scan the unit "<Unsuitable UN>" with product code "<Unsuitable Code>".
        Then I should see a "Acknowledgment Message" message: "<Message>".
        When I confirm the acknowledgment message.
        Then I should see the unit "<Unsuitable UN>" with code "<Unsuitable Code>" added to the removed products section with unsuitable status "<Unsuitable Status>".
        And I should see the log of removed products being updated.
        And The complete shipment option should be enabled.
        And The fill more products option should be "enabled".

        Examples:
            | Order Number | Suitable Code | Suitable UN   | Unsuitable Code | Unsuitable UN | Unsuitable Status | Message                                                                                         |
            | 120          | E0685V00      | W822530106090 | E0685V00        | W822530106091 | Discarded         | This product has already been discarded for BROKEN in the system. Place in biohazard container. |

    Rule: I should restart the second verification process when I scan a unit that is not required to be removed.
    @DIS-207
    Scenario Outline: Second verification unsuitable products - remove units - rescan all products.
        Given I have a shipment for order "<Order Number>" with the units "<Suitable UN>,<Unsuitable UN>" and product codes "<Suitable Code>,<Unsuitable Code>" "verified".
        And The verified unit "<Unsuitable UN>" is unsuitable with status "<Unsuitable Status>" and message "<Message>".
        And The second verification configuration is "enabled".
        And I am on the verify products page with "notifications" tab active.
        When I scan the unit "W822530106044" with product code "E0685V00".
        Then I should see a "Warning" message: "The verification does not match all products in this order. Please re-scan all the products.".
        And I should not see the unit "<Unsuitable UN>" with code "<Unsuitable Code>" added to the removed products section with unsuitable status "<Unsuitable Status>".
        And The complete shipment option should not be enabled.
        And I should be redirected to the verify products page.
        And I should see the verified products section empty.

        Examples:
            | Order Number | Suitable Code | Suitable UN   | Unsuitable Code | Unsuitable UN | Unsuitable Status |
            | 120          | E0685V00      | W822530106090 | E0685V00        | W822530106091 | Discarded         |

    Rule: I should restart the second verification process when I scan a unit that is already removed.
    @DIS-207
    Scenario Outline: Second verification unsuitable products - remove units twice - rescan all products.
        Given I have a shipment for order "<Order Number>" with the units "<Suitable UN>,<Unsuitable UN>" and product codes "<Suitable Code>,<Unsuitable Code>" "verified".
        And The verified unit "<Unsuitable UN>" is unsuitable with status "<Unsuitable Status>" and message "<Message>".
        And The verified unit "<Suitable UN>" is unsuitable with status "<Unsuitable Status>" and message "<Message>".
        And The second verification configuration is "enabled".
        And I am on the verify products page with "notifications" tab active.
        When I scan the unit "<Unsuitable UN>" with product code "<Unsuitable Code>".
        Then I should see a "Acknowledgment Message" message: "<Message>".
        And I confirm the acknowledgment message.
        When I rescan the unit "<Unsuitable UN>" with product code "<Unsuitable Code>".
        Then I should see a "Warning" message: "The verification does not match all products in this order. Please re-scan all the products.".
        And I should be redirected to the verify products page.
        And The complete shipment option should not be enabled.
        And I should see the verified products section empty.

        Examples:
            | Order Number | Suitable Code | Suitable UN   | Unsuitable Code | Unsuitable UN | Unsuitable Status | Message                                                                                         |
            | 120          | E0685V00      | W822530106090 | E0685V00        | W822530106091 | Discarded         | This product has already been discarded for BROKEN in the system. Place in biohazard container. |

    Rule: I should be able to scan unit number and product code.
    Rule: I should not be able to enter unit number and product code manually.
    @DIS-207
    Scenario Outline: Restrict Manual Entry Unit Number.
        Given I have a shipment for order "<Order Number>" with the units "<Suitable UN>,<Unsuitable UN>" and product codes "<Suitable Code>,<Unsuitable Code>" "unsuitable verified".
        And The verified unit "<Unsuitable UN>" is unsuitable with status "Discarded" and message "This product has already been discarded for BROKEN in the system. Place in biohazard container.".
        And The second verification configuration is "enabled".
        And I am on the verify products page with "notifications" tab active.
        When I focus out leaving "Unit Number" empty.
        Then I should see a field validation error message "Unit Number is Required".
        When I "<Action>" the "Unit Number" "<Field Value>".
        Then I should see a field validation error message "<Field Error Message>".
        And The "Product Code" field should be "disabled".
        Examples:
            | Order Number | Suitable Code | Suitable UN   | Unsuitable Code | Unsuitable UN | Action | Field Value   | Field Error Message    |
            | 122          | E0685V00      | W822530106090 | E0685V00        | W822530106091 | Type   | W822530106093 | Scan Unit Number       |
            | 122          | E0685V00      | W822530106093 | E0685V00        | W822530106091 | Type   | =W82253010608 | Unit Number is Invalid |
            | 122          | E0685V00      | W822530106093 | E0685V00        | W822530106091 | Scan   | w232323232    | Unit Number is Invalid |

    Rule: I should not be able to enter unit number and product code manually.
    Rule: I should be able to scan unit number and product code.
    @DIS-207
    Scenario Outline: Restrict Manual Entry Product Code.
        Given I have a shipment for order "<Order Number>" with the units "<Suitable UN>,<Unsuitable UN>" and product codes "<Suitable Code>,<Unsuitable Code>" "unsuitable verified".
        And The verified unit "<Unsuitable UN>" is unsuitable with status "Discarded" and message "This product has already been discarded for BROKEN in the system. Place in biohazard container.".
        And The second verification configuration is "enabled".
        And I am on the verify products page with "notifications" tab active.
        When I "<Action>" the "<Field Name>" "<Field Value>".
        Then The "Product Code" field should be "enabled".
        When I focus out leaving "<Second Field Name>" empty.
        Then I should see a field validation error message "Product Code is Required".
        When I "<Second Action>" the "<Second Field Name>" "<Second Field Value>".
        Then I should see a field validation error message "<Field Error Message>".
        Examples:
            | Order Number | Suitable Code | Suitable UN   | Unsuitable Code | Unsuitable UN | Action | Field Name  | Field Value   | Field Error Message     | Second Action | Second Field Name | Second Field Value |
            | 123          | E0685V00      | W822530106090 | E0685V00        | W822530106091 | Scan   | Unit Number | W822530106094 | Scan Product Code       | Type          | Product Code      | E0685V00           |
            | 123          | E0685V00      | W822530106093 | E0685V00        | W822530106091 | Scan   | Unit Number | W822530106094 | Product Code is Invalid | Scan          | Product Code      | 121abc             |
            | 123          | E0685V00      | W822530106093 | E0685V00        | W822530106091 | Scan   | Unit Number | W822530106094 | Product Code is Invalid | Type          | Product Code      | =<1212             |
