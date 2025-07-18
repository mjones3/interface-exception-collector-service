# Feature Unit Number reference: W777725002xxx
@skipOnPipeline
@ui @LAB-576 @AOA-61 @hzd9
Feature: Starts Irradiation Process

    Rule: As a distribution specialist, I want to add products into an irradiation batch so that I can start the irradiation process
        @LAB-576 @LAB-575 @LAB-574
        Scenario Outline: I can start an irradiation batch.
            Given I have a device "<Blood Center Id>" at location "<Location>" with status "ACTIVE"
            And I have the following inventory products:
                | Unit Number   | Product Code | Status    | Location  | Product Family               |
                | W777725002001 | E033600      | AVAILABLE | 123456789 | RED_BLOOD_CELLS_LEUKOREDUCED |
                | W777725002002 | E068600      | AVAILABLE | 123456789 | RED_BLOOD_CELLS_LEUKOREDUCED |
            And I login to Distribution module
            And I select the location "MDL Hub 1"

            When I navigate to "Start Irradiation" in "Irradiation"
            Then I verify that I am taken to the page "Start Irradiation" in "Irradiation"
            Then I verify that the "Lot Number" field is "enabled"
            Then I verify that the "Irradiator Id" field is "enabled"
            Then I verify that the "Unit Number" field is "disabled"
            And I verify that I am "Unable" to "Submit"

            When I scan the irradiator id "<Blood Center Id>"
            Then I verify that the "Unit Number" field is "disabled"
            And I verify that the "Irradiator Id" field is "disabled"
            And I verify that I am "Unable" to "Submit"

            When I scan the lot number "<Lot Number 1>"
            Then I verify that the "Unit Number" field is "enabled"
            And I verify that the "Irradiator Id" field is "disabled"
            And I verify that I am "Unable" to "Submit"

            When I scan the unit number "=<Unit Number 1>00" in the irradiation page
            Then I verify the product "<Product Code 1>" is displayed for selection

            When I select the product "<Product Code 1>"
            Then I verify that the unit number "<Unit Number 1>" with product "<Description 1>" was added to the batch
            And I verify that I am "Able" to "Submit"
            And I verify that the "Irradiator Id" field is "disabled"

            When I scan the lot number "<Lot Number 2>"
            And I scan the unit number "=<Unit Number 2>00" in the irradiation page
            When I select the product "<Product Code 2>"
            Then I verify that the unit number "<Unit Number 2>" with product "<Description 2>" was added to the batch

            And I choose to "Submit"
            And I see the "Success" message "Batch submitted successfully"

            Examples:
                | Unit Number 1 | Product Code 1 | Description 1 | Unit Number 2 | Product Code 2 | Description 2     | Blood Center Id | Location  | Lot Number 1 | Lot Number 2 |
                | W777725002001 | E033600        | AS1 LR RBC    | W777725002002 | E068600        | APH AS3 LR RBC C2 | AUTO-IRRAD001   | 123456789 | Lot1234      | Lot5678      |

        @LAB-615
        Scenario Outline: I should be notified if the product selected has a Quarantine that stops manufacturing
            Given I have a device "<Blood Center Id>" at location "<Location>" with status "ACTIVE"
            And I have the following inventory products:
                | Unit Number   | Product Code   | Status     | Stop Manufacturing | Location  |
                | <Unit Number> | <Product Code> | QUARANTINE | Yes                | 123456789 |
            And I login to Distribution module
            And I select the location "MDL Hub 1"
            And I navigate to "Start Irradiation" in "Irradiation"
            And I scan the irradiator id "<Blood Center Id>"
            And I scan the lot number "<Lot Number>"

            When I scan the unit number "=<Unit Number>00" in the irradiation page
            And I select the product "<Product Code>"
            Then I see the "Warning" message "This product has been quarantined and cannot be irradiated"
            And I verify that the unit number "<Unit Number>" with product "<Description>" was not added to the batch

            Examples:
                | Unit Number   | Product Code | Description | Blood Center Id | Location  | Lot Number |
                | W777725002003 | E003300      | AS1 LR RBC  | AUTO-IRRAD002   | 123456789 | Lot1234    |

        @LAB-615
        Scenario Outline: I should be notified if the product selected has a Non stopping manufacturing Quarantine
            Given I have a device "<Blood Center Id>" at location "<Location>" with status "ACTIVE"
            And I have the following inventory products:
                | Unit Number   | Product Code   | Status     | Stop Manufacturing | Location  |
                | <Unit Number> | <Product Code> | QUARANTINE | No                 | 123456789 |
            And I login to Distribution module
            And I select the location "MDL Hub 1"
            And I navigate to "Start Irradiation" in "Irradiation"
            And I scan the irradiator id "<Blood Center Id>"
            And I scan the lot number "<Lot Number>"

            When I scan the unit number "=<Unit Number>00" in the irradiation page
            And I select the product "<Product Code>"
            And I verify that the unit number "<Unit Number>" with product "<Description>" was added to the batch
            # Need to check if the Card has the Quarantine indicator

            Examples:
                | Unit Number   | Product Code | Description | Blood Center Id | Location  | Lot Number |
                | W777725002004 | E003300      | AS1 LR RBC  | AUTO-IRRAD003   | 123456789 | Lot1234    |

        @LAB-615
        Scenario Outline: I should be notified if the product selected has been discarded
            Given I have a device "<Blood Center Id>" at location "<Location>" with status "ACTIVE"
            And I have the following inventory products:
                | Unit Number   | Product Code   | Status    | Reason  | Location  |
                | <Unit Number> | <Product Code> | Discarded | EXPIRED | 123456789 |
            And I login to Distribution module
            And I select the location "MDL Hub 1"
            And I navigate to "Start Irradiation" in "Irradiation"
            And I scan the irradiator id "<Blood Center Id>"
            And I scan the lot number "<Lot Number>"

            When I scan the unit number "=<Unit Number>00" in the irradiation page
            And I select the product "<Product Code>"
            Then I see the "Warning" message "This product has already been discarded for EXPIRED in the system. Place in biohazard container."
            And I verify that the unit number "<Unit Number>" with product "<Description>" was not added to the batch

            Examples:
                | Unit Number   | Product Code | Description | Blood Center Id | Location  | Lot Number |
                | W777725002005 | E003300      | AS1 LR RBC  | AUTO-IRRAD004   | 123456789 | Lot1234    |

        @LAB-615
        Scenario Outline: I should see an acknowledgement message if the selected product is an Unsuitable or Expired.
            Given I have a device "<Blood Center Id>" at location "123456789" with status "ACTIVE"
            And I have the following inventory products:
                | Unit Number   | Product Code   | Status    | Location  | Unsuitable Reason   | Expired   |
                | <Unit Number> | <Product Code> | AVAILABLE | 123456789 | <Unsuitable Reason> | <Expired> |
            And I login to Distribution module
            And I select the location "MDL Hub 1"
            And I navigate to "Start Irradiation" in "Irradiation"
            And I scan the irradiator id "<Blood Center Id>"
            And I scan the lot number "Lot1234"

            When I scan the unit number "=<Unit Number>00" in the irradiation page
            And I select the product "<Product Code>"

            Then I see the confirmation message with title "Discarded" and message "<Message>"
            # Step fails because of the TITLE
            And I confirm the confirmation message
            And I verify that the unit number "<Unit Number>" with product "<Description>" was not added to the batch

            Examples:
                | Unit Number   | Product Code | Description | Unsuitable Reason              | Expired | Blood Center Id | Message                                                                                         |
                | W777725002006 | E003300      | AS1 LR RBC  | POSITIVE_REACTIVE_TEST_RESULTS | NO      | AUTO-IRRAD005   | This product has beend discard for Positive Reactive Test Results. Place in biohazard container |
                | W777725002007 | E003300      | AS1 LR RBC  |                                | YES     | AUTO-IRRAD006   | This product is expired and has been discarded. Place in biohazard container                    |

        @LAB-615
        Scenario Outline: I should be notified if the unit number is not in the current location
            Given I have a device "<Blood Center Id>" at location "<Location>" with status "ACTIVE"
            And I have the following inventory products:
                | Unit Number   | Product Code   | Status    | Location  |
                | <Unit Number> | <Product Code> | AVAILABLE | 234567891 |
            And I login to Distribution module
            And I select the location "MDL Hub 1"
            And I navigate to "Start Irradiation" in "Irradiation"
            And I scan the irradiator id "<Blood Center Id>"
            And I scan the lot number "<Lot Number>"

            When I scan the unit number "=<Unit Number>00" in the irradiation page
            Then I see the "Warning" message "No products eligible for irradiation"
            # POP-UP DISPLAYED TWICE
            And I verify that the unit number "<Unit Number>" with product "<Description>" was not added to the batch

            Examples:
                | Unit Number   | Product Code | Description | Blood Center Id | Location  | Lot Number |
                | W777725002008 | E003300      | AS1 LR RBC  | AUTO-IRRAD007   | 123456789 | Lot1234    |

        @LAB-615
        Scenario Outline: I should be notified if the product was already irradiated
            Given I have a device "<Blood Center Id>" at location "<Location>" with status "ACTIVE"
            And I have the following inventory products:
                | Unit Number   | Product Code   | Status    | Location  |
                | <Unit Number> | <Product Code> | AVAILABLE | 123456789 |
            And the product "<Product Code>" in the unit "<Unit Number>" was already irradiated in a completed batch for device "<Blood Center Id>"
            And I login to Distribution module
            And I select the location "MDL Hub 1"
            And I navigate to "Start Irradiation" in "Irradiation"
            And I scan the irradiator id "<Blood Center Id>"
            And I scan the lot number "<Lot Number>"

            When I scan the unit number "=<Unit Number>00" in the irradiation page
            And I select the product "<Product Code>"
            Then I see the "Warning" message "This unit has been quarantined and manufacturing cannot be completed"
            And I verify that the unit number "<Unit Number>" with product "<Description>" was not added to the batch

            Examples:
                | Unit Number   | Product Code | Description | Blood Center Id | Location  | Lot Number |
                | W777725002009 | E003300      | AS1 LR RBC  | AUTO-IRRAD008   | 123456789 | Lot1234    |

        @LAB-615
        Scenario Outline: I should be notified if the product is not configured for irradiation
            Given I have a device "<Blood Center Id>" at location "<Location>" with status "ACTIVE"
            And I have the following inventory products:
                | Unit Number   | Product Code   | Status    | Location  |
                | <Unit Number> | <Product Code> | AVAILABLE | 123456789 |
            And I login to Distribution module
            And I select the location "MDL Hub 1"
            And I navigate to "Start Irradiation" in "Irradiation"
            And I scan the irradiator id "<Blood Center Id>"
            And I scan the lot number "<Lot Number>"

            When I scan the unit number "=<Unit Number>00" in the irradiation page
            And I select the product "<Product Code>"
            Then I see the "Warning" message "Product not configured for Irradiation"
            And I verify that the unit number "<Unit Number>" with product "<Description>" was not added to the batch

            Examples:
                | Unit Number   | Product Code | Description | Blood Center Id | Location  | Lot Number |
                | W777725002010 | E0869V00     | AS1 LR RBC  | AUTO-IRRAD009   | 123456789 | Lot1234    |

        @LAB-615
        Scenario Outline: I should be notified if the selected product is currently being irradiated
            Given I have the following inventory products:
                | Unit Number   | Product Code   | Status    | Location  |
                | <Unit Number> | <Product Code> | AVAILABLE | 123456789 |
            And the product "<Product Code>" in the unit "<Unit Number>" was already irradiated in a opened batch for device "<Blood Center Id 1>"
            And I login to Distribution module
            And I select the location "MDL Hub 1"
            And I navigate to "Start Irradiation" in "Irradiation"
            And I scan the irradiator id "<Blood Center Id 2>"
            And I scan the lot number "<Lot Number>"

            When I scan the unit number "=<Unit Number>00" in the irradiation page
            Then I see the "Warning" message "No products eligible for irradiation"
            And I verify that the unit number "<Unit Number>" with product "<Description>" was not added to the batch

            Examples:
                | Unit Number   | Product Code | Description | Blood Center Id 1 | Lot Number | Blood Center Id 2 |
                | W777725002011 | E003300      | AS1 LR RBC  | AUTO-IRRAD010     | Lot1234    | AUTO-IRRAD010     |

