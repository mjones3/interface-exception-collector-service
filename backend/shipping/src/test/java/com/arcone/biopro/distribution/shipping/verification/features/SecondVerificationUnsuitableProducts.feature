Feature: Second Verification Notification Tab
    As a distribution technician,
    I want to rescan products that have been flagged as unsuitable during the second verification process,
    So that I can remove them from the shipment.

    Background:
        Given I cleaned up from the database the packed item that used the unit number "W822530106087,W822530106089,W822530106088,W822530106090,W822530106091,W822530106092".
        And I cleaned up from the database, all shipments with order number "118,119,120,121".

    Rule: I should see a notification stating that the units should be rescanned to be removed.
    Rule: I should be able to scan the unit number and product code of the products identified as unsuitable.
    Rule: I should be able to see and confirm an acknowledgment message every time an unsuitable product is rescanned (discarded, quarantined, etc.).
    Rule: I should be able to see the progress bar that reflects the number of products removed.
    Rule: I should see the list of removed products.
    Rule: I should be able to fill more products to replace the units removed.
    Rule: I should be able to complete the shipment once all unacceptable products are removed.
    @ui @DST-207
    Scenario Outline: Second verification unsuitable products - remove units.
        Given I have a shipment for order "<Order Number>" with the units "<Suitable UN>,<Unsuitable UN>" and product codes "<Suitable Code>,<Unsuitable Code>" "unsuitable verified".
        And The second verification configuration is "enabled".
        And I am on the verify products page with "Notification" tab active.
        And I should see a "Notification" message: "One or more products have changed status. You must rescan the products to be removed.".
        When I scan the unit "<Unsuitable UN>" with product code "<Unsuitable Code>".
        Then I should see a "Acknowledgment Message" message: "<Message>".
        When I confirm the acknowledgment message.
        Then I should see the unit "<Unsuitable UN>" with code "<Unsuitable Code>" added to the removed products table with unsuitable status "<Unsuitable Status>".
        And I should see the log of removed products being updated.
        And The complete shipment option should be enabled.
        And The fill more products option should be enabled.

        Examples:
            | Order Number | Suitable Code | Suitable UN   | Unsuitable Code | Unsuitable UN | Unsuitable Status | Message                                                                                          |
            | 120          | E0685V00      | W822530106090 | E0685V00        | W822530106091 | Discarded         | This product has already been discarded for (reason) in the system. Place in biohazard container |

    Rule: I should restart the second verification process when I scan a unit that is not required to be removed.
    @ui @DST-207
    Scenario Outline: Second verification unsuitable products - remove units - rescan all products.
        Given I have a shipment for order "<Order Number>" with the units "<Suitable UN>,<Unsuitable UN>" and product codes "<Suitable Code>,<Unsuitable Code>" "unsuitable verified".
        And The second verification configuration is "enabled".
        And I am on the verify products page with "Notification" tab active.
        When I scan the unit "WXXXXXXXXXXXX" with product code "E000000".
        Then I should see a "Warning" message: "XXXXXXXXXXXXXXXXXXX. Please re-scan all the products.".
        And I should not see the unit added to the verified products table.
        And The complete shipment option should not be enabled.
        And I should be redirected to the verify products page.
        And I should see the verified products table empty.

        Examples:
            | Order Number | Suitable Code | Suitable UN   | Unsuitable Code | Unsuitable UN |
            | 120          | E0685V00      | W822530106090 | E0685V00        | W822530106091 |

    Rule: I should restart the second verification process when I scan a unit that is already removed.
    @ui @DST-207
    Scenario Outline: Second verification unsuitable products - remove units twice - rescan all products.
        Given I have a shipment for order "<Order Number>" with the units "<Suitable UN>,<Unsuitable UN>" and product codes "<Suitable Code>,<Unsuitable Code>" "unsuitable verified".
        And The second verification configuration is "enabled".
        And I am on the verify products page with "Notification" tab active.
        When I scan the unit "<Unsuitable UN>" with product code "<Unsuitable Code>".
        Then I should see a "Acknowledgment Message" message: "<Message>".
        And I confirm the acknowledgment message.
        When I scan the unit "<Unsuitable UN>" with product code "<Unsuitable Code>".
        Then I should see a "Warning" message: "XXXXXXXXXXXXXXXXXXX. Please re-scan all the products.".
        And I should be redirected to the verify products page.

        Examples:
            | Order Number | Suitable Code | Suitable UN   | Unsuitable Code | Unsuitable UN |
            | 120          | E0685V00      | W822530106090 | E0685V00        | W822530106091 |

    # FIXME Should we test again the new UNIT NUMBER and PRODUCT CODE inputs restricting manual entry?
