#    Distribution UI Improvements- Orders
#    Date of Testing: 12/18/2024
#    Documented by: Kristine Belanger
#    Reviewed by: Date:


@disabled @ui @AOA-39 @RSA20-310
Feature: Order Search- Apheresis Plasma and Apheresis Red Cells

    Scenario: Create date field displays as required when user starts entering information in any field except Order number
        Given I am in the Distribution service of BioPro.
        And I select the product order filter.
        When I enter information in the Order Number field.
        Then the Create Date field does not display as a required field.

    Scenario: Create date field displays as required when user starts entering information in any field except Order number
        Given I am in the Distribution service of BioPro.
        And I select the product order filter.
        When I make a selection in the Order Status field.
        Then the Create Date field displays as a required field.

    Scenario: Create date field displays as required when user starts entering information in any field except Order number
        Given I am in the Distribution service of BioPro.
        And I select the product order filter.
        When I make a selection in the Order Priority field.
        Then the Create Date field displays as a required field.

    Scenario: Create date field displays as required when user starts entering information in any field except Order number
        Given I am in the Distribution service of BioPro.
        And I select the product order filter.
        When I make a selection in the Ship to Customer field.
        Then the Create Date field displays as a required field.

    Scenario: Create date field displays as required when user starts entering information in any field except Order number
        Given I am in the Distribution service of BioPro.
        And I select the product order filter.
        When I enter information in the Create Date field.
        Then the Create Date field displays as a required field.

    Scenario: Create date field displays as required when user starts entering information in any field except Order number
        Given I am in the Distribution service of BioPro.
        And I select the product order filter.
        When I enter information in the Desired Shipment Date field.
        Then the Create Date field displays as a required field.

    Scenario: Display error message when user enters non-number values for the Create Date field
        Given I am in the Distribution service of BioPro.
        And I select the product order filter.
        When I enter non-numeric values for Create Date.
        Then the message “Date is Invalid” is displayed under the Create Date field.

    Scenario: Display error message when user enters non-number values for the Desired Shipment Date field
        Given I am in the Distribution service of BioPro.
        And I select the product order filter.
        When I enter non-numeric values for Desired Shipment Date.
        Then the message “Date is Invalid” is displayed under the Desired Shipment Date field.

    Scenario: Display required field message for Create Date field
        Given I am in the Distribution service of BioPro.
        And I select the product order filter.
        When I make a selection in the Order Status field.
        Then the message “Date is Required” displays under the Create Date field.

    Scenario: Display required field message for Create Date field
        Given I am in the Distribution service of BioPro.
        And I select the product order filter.
        When I make a selection in the Order Priority field.
        Then the message “Date is Required” displays under the Create Date field.

    Scenario: Display required field message for Create Date field
        Given I am in the Distribution service of BioPro.
        And I select the product order filter.
        When I make a selection in the Ship to Customer.
        Then the message “Date is Required” displays under the Create Date field.

    Scenario: Display required field message for Create Date field
        Given I am in the Distribution service of BioPro.
        And I select the product order filter.
        When I begin entering information in the Desired Shipment Date.
        Then the message “Date is Required” displays under the Create Date field.

    Scenario: Create Date field should accept the date format as MM/DD/YYYY
        Given I am in the Distribution service of BioPro.
        And I select the product order filter.
        When I enter a two-digit month value in the Create Date field.
        And I enter a two-digit day value in the Create Date field.
        And I enter a four-digit year value in the Create Date field.
        Then the date is accepted.

    Scenario: Create Date field should accept the date format as MM/DD/YYYY
        Given I am in the Distribution service of BioPro.
        And I select the product order filter.
        When I enter a one-digit month value in the Create Date field.
        And I enter a one-digit day value in the Create Date field.
        And I enter a four-digit year value in the Create Date field.
        Then the month is converted to a two-digit value.
        And the day is converted to a two-digit value.
        And the date is accepted.

    Scenario: Create Date field should accept the date format as MM/DD/YYYY
        Given I am in the Distribution service of BioPro.
        And I select the product order filter.
        When I enter a two-digit month value in the Create Date field.
        And I enter a two-digit day value in the Create Date field.
        And I enter a two-digit year value in the Create Date field.
        Then the message “Date is Invalid” displays under the Create Date field.

    Scenario: Desired Shipment Date field should accept the date format as MM/DD/YYYY
        Given I am in the Distribution service of BioPro.
        And I select the product order filter.
        When I enter a two-digit month value in the Desired Shipment Date field.
        And I enter a two-digit day value in the Desired Shipment field.
        And I enter a four-digit year value in the Desired Shipment field.
        Then the date is accepted.

    Scenario: Desired Shipment Date field should accept the date format as MM/DD/YYYY
        Given I am in the Distribution service of BioPro.
        And I select the product order filter.
        When I enter a one-digit month value in the Desired Shipment Date field.
        And I enter a one-digit day value in the Desired Shipment Date field.
        And I enter a four-digit year value in the Desired Shipment Date field.
        Then the month is converted to a two-digit value.
        And the day is converted to a two-digit value.
        And the date is accepted.

    Scenario: Desired Shipment Date field should accept the date format as MM/DD/YYYY
        Given I am in the Distribution service of BioPro.
        And I select the product order filter.
        When I enter a two-digit month value in the Desired Shipment Date field.
        And I enter a two-digit day value in the Desired Shipment Date field.
        And I enter a two-digit year value in the Desired Shipment Date field.
        Then the message “Date is Invalid” displays under the Desired Shipment Date field.

    Scenario: Close icon will remove typed information in search input for multi-select fields
        Given I am in the Distribution service of BioPro.
        And I select the product order filter.
        When I enter information in the Filter Order Status field in the Order Status field.
        Then the Close Icon will display.
        And when the Close Icon is clicked, the information that was entered in the Filter Order Status field will be removed.

    Scenario: Close icon will remove typed information in search input for multi-select fields
        Given I am in the Distribution service of BioPro.
        And I select the product order filter.
        When I enter information in the Filter Order Priority field in the Order Priority field.
        Then the Close Icon will display.
        And when the Close Icon is clicked, the information that was entered in the Filter Order Priority field will be removed.

    Scenario: Close icon will remove typed information in search input for multi-select fields
        Given I am in the Distribution service of BioPro.
        And I select the product order filter.
        When I enter information in the Filter Ship to Customer field in the Ship to Customer field.
        Then the Close Icon will display.
        And when the Close Icon is clicked, the information that was entered in the Filter Ship to Customer field will be removed.

    Scenario: Filter for multi-select fields displays previously selected values when user filters results in dropdown
        Given I am in the Distribution service of BioPro.
        And I select the product order filter.
        When I select the value “OPEN” in the Order Status field.
        And I select the dropdown in the Order Status field.
        Then the value Open remains visible in the Order Status field.

    Scenario: Filter for multi-select fields displays previously selected values when user filters results in dropdown
        Given I am in the Distribution service of BioPro.
        And I select the product order filter.
        When I select the value “STAT” in the Order Priority field.
        And I select the dropdown in the Order Priority field.
        Then the value STAT remains visible in the Order Priority field.

    Scenario: Filter for multi-select fields displays previously selected values when user filters results in dropdown
        Given I am in the Distribution service of BioPro.
        And I select the product order filter.
        When I select the value “PIONEER HEALTH SERVICES” in the Ship to Customer field.
        And I select the dropdown in the Ship to Customer field.
        Then the value Pioneer Health Services remains visible in the Ship to Customer field.

    Scenario: Multiple selections for the multi-select fields should display in the following format {firstValue} (+{count other|others)
        Given I am in the Distribution service of BioPro.
        And I select the product order filter.
        When I select the value “ALL” in the Order Status field.
        Then Open(+2 others) is visible in the Order Status field.

    Scenario: Multiple selections for the multi-select fields should display in the following format {firstValue} (+{count other|others)
        Given I am in the Distribution service of BioPro.
        And I select the product order filter.
        When I select the value “ALL” in the Order Priority field.
        Then STAT(+4 others) is visible in the Order Priority field.

    Scenario: Multiple selections for the multi-select fields should display in the following format {firstValue} (+{count other|others)
        Given I am in the Distribution service of BioPro.
        And I select the product order filter.
        When I select the value “ALL” in the Ship to Customer field.
        Then Creative Testing Solutions(+4 others) is visible in the Ship to Customer field.

    Scenario: Search Order filter adheres to UI/UX standards
        Given I am in the Distribution service of BioPro.
        When I select the product order filter.
        Then it adheres to the current UI/UX design standards.

    Scenario: No count of characters is visible for the Order Number field
        Given I am in the Distribution service of BioPro.
        And I select the product order filter.
        When I begin entering information in the Order Number field.
        Then no character count information is displayed below the Order Number field.
