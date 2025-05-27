# Feature Unit Number reference: W777725016000
@api @LAB-440 @skipOnPipeline
Feature: Get Inventory

    Scenario Outline: Inventory by Unit Number
        Given I have the following inventories:
            | Unit Number   | Product Code | Location   | Status    | Collection Location | Collection TimeZone | Is Labeled | Expires In Days | Quarantine Reasons                                     | Discard Reason           | Unsuitable Reason | Comments                  | Volumes                          |
            | W777725016001 | E0869V00     | LOCATION_1 | AVAILABLE | LOCATION_1          | America/New_York    | true       | 5               |                                                        |                          |                   |                           | Anticoagulant - 50, Volume - 500 |
            | W777725016002 | E0869VA0     | LOCATION_2 | AVAILABLE | LOCATION_2          | America/Los_Angeles | true       | 5               |                                                        |                          | ACTIVE_DEFERRAL   |                           |                                  |
            | W777725016003 | E0869VB0     | LOCATION_2 | AVAILABLE | LOCATION_2          | America/Los_Angeles | true       | 5               | ABS_POSITIVE, PENDING_FURTHER_REVIEW_INSPECTION, OTHER |                          |                   | Quarantine other comments |                                  |
            | W777725016004 | E0869VC0     | LOCATION_1 | AVAILABLE | LOCATION_1          | America/New_York    | true       | -1              |                                                        |                          |                   |                           |                                  |
            | W777725016005 | E0869VD0     | LOCATION_1 | DISCARDED | LOCATION_1          | America/New_York    | true       | 5               |                                                        | ADDITIVE_SOLUTION_ISSUES |                   |                           |                                  |
            | W777725016006 | E0869VD0     | LOCATION_1 | AVAILABLE | LOCATION_1          | America/New_York    | true       | 1               |                                                        |                          |                   |                           |                                  |
            | W777725016006 | E1624V00     | LOCATION_1 | AVAILABLE | LOCATION_1          | America/New_York    | true       | 1               |                                                        |                          |                   |                           |                                  |
            | W777725016007 | E0869VD0     | LOCATION_1 | DISCARDED | LOCATION_1          | America/New_York    | true       | 5               |                                                        | OTHER                    |                   | Some comments             |                                  |
            | W777725016008 | E0869VD0     | LOCATION_1 | AVAILABLE | LOCATION_1          | America/New_York    | false      | 5               |                                                        |                          |                   |                           |                                  |
            | W777725016009 | E0869VD0     | LOCATION_1 | PACKED    | LOCATION_1          | America/New_York    | true       | 5               |                                                        |                          |                   |                           |                                  |
            | W777725016010 | E0869VD0     | LOCATION_1 | SHIPPED   | LOCATION_1          | America/New_York    | true       | 5               |                                                        |                          |                   |                           |                                  |

        When I request a inventory with unit number "<Unit Number>"
        Then I receive the following from get inventory by unit number:
            | Unit Number   | Product Code   | Temperature Category   | Location   | Collection Location   | Collection TimeZone   | Volumes   | Quarantine Reasons   | Discard Reason   | Unsuitable Reason   | Expired   |
            | <Unit Number> | <Product Code> | <Temperature Category> | <Location> | <Collection Location> | <Collection TimeZone> | <Volumes> | <Quarantine Reasons> | <Discard Reason> | <Unsuitable Reason> | <Expired> |

        Examples:
            | Unit Number   | Product Code       | Temperature Category | Location   | Volumes                          | Collection Location | Collection TimeZone | Quarantine Reasons                                     | Discard Reason           | Unsuitable Reason | Expired |
            | W777725016001 | E0869V00           | FROZEN               | LOCATION_1 | Anticoagulant - 50, Volume - 500 | LOCATION_1          | America/New_York    |                                                        |                          |                   | False   |
            | W777725016002 | E0869VA0           | FROZEN               | LOCATION_2 |                                  | LOCATION_2          | America/Los_Angeles |                                                        |                          | ACTIVE_DEFERRAL   | False   |
            | W777725016003 | E0869VB0           | FROZEN               | LOCATION_2 |                                  | LOCATION_2          | America/Los_Angeles | ABS_POSITIVE, PENDING_FURTHER_REVIEW_INSPECTION, OTHER |                          |                   | False   |
            | W777725016004 | E0869VC0           | FROZEN               | LOCATION_1 |                                  | LOCATION_1          | America/New_York    |                                                        |                          |                   | True    |
            | W777725016005 | E0869VD0           | FROZEN               | LOCATION_1 |                                  | LOCATION_1          | America/New_York    |                                                        | ADDITIVE_SOLUTION_ISSUES |                   | False   |
            | W777725016006 | E0869VD0, E1624V00 | FROZEN               | LOCATION_1 |                                  | LOCATION_1          | America/New_York    |                                                        |                          |                   | False   |
            | W777725016007 | E0869VD0           | FROZEN               | LOCATION_1 |                                  | LOCATION_1          | America/New_York    |                                                        | OTHER                    |                   | False   |
            | W777725016008 | E0869VD0           | FROZEN               | LOCATION_1 |                                  | LOCATION_1          | America/New_York    |                                                        |                          |                   | False   |
            | W777725016009 | E0869VD0           | FROZEN               | LOCATION_1 |                                  | LOCATION_1          | America/New_York    |                                                        |                          |                   | False   |
            | W777725016010 | E0869VD0           | FROZEN               | LOCATION_1 |                                  | LOCATION_1          | America/New_York    |                                                        |                          |                   | False   |


    Scenario Outline: Inventory by Unit Number and Product Code
        Given I have the following inventories:
            | Unit Number   | Product Code | Location   | Status    | Collection Location | Collection TimeZone | Is Labeled | Expires In Days | Quarantine Reasons                                     | Discard Reason           | Unsuitable Reason | Comments                  | Volumes                          |
            | W777725016012 | E0869V00     | LOCATION_1 | AVAILABLE | LOCATION_1          | America/New_York    | true       | 5               |                                                        |                          |                   |                           | Anticoagulant - 50, Volume - 500 |
            | W777725016013 | E0869VA0     | LOCATION_2 | AVAILABLE | LOCATION_2          | America/Los_Angeles | true       | 5               |                                                        |                          |                   |                           |                                  |
            | W777725016014 | E0869VB0     | LOCATION_2 | AVAILABLE | LOCATION_2          | America/Los_Angeles | true       | 5               | ABS_POSITIVE, PENDING_FURTHER_REVIEW_INSPECTION, OTHER |                          |                   | Quarantine other comments |                                  |
            | W777725016015 | E0869VC0     | LOCATION_1 | AVAILABLE | LOCATION_1          | America/New_York    | true       | -1              |                                                        |                          |                   |                           |                                  |
            | W777725016016 | E0869VD0     | LOCATION_1 | DISCARDED | LOCATION_1          | America/New_York    | true       | 5               |                                                        | ADDITIVE_SOLUTION_ISSUES |                   |                           |                                  |
            | W777725016017 | E0869VD0     | LOCATION_1 | AVAILABLE | LOCATION_1          | America/New_York    | true       | 1               |                                                        |                          | ACTIVE_DEFERRAL   |                           |                                  |
            | W777725016017 | E1624V00     | LOCATION_1 | AVAILABLE | LOCATION_1          | America/New_York    | true       | 1               |                                                        |                          | TIMING_RULES      |                           |                                  |
            | W777725016018 | E0869VD0     | LOCATION_1 | DISCARDED | LOCATION_1          | America/New_York    | true       | 5               |                                                        | OTHER                    |                   | Some comments             |                                  |
            | W777725016019 | E0869VD0     | LOCATION_1 | AVAILABLE | LOCATION_1          | America/New_York    | false      | 5               |                                                        |                          |                   |                           |                                  |
            | W777725016020 | E0869VD0     | LOCATION_1 | PACKED    | LOCATION_1          | America/New_York    | true       | 5               |                                                        |                          |                   |                           |                                  |
            | W777725016021 | E0869VD0     | LOCATION_1 | SHIPPED   | LOCATION_1          | America/New_York    | true       | 5               |                                                        |                          |                   |                           |                                  |
            | W777725016022 | E0869VD0     | LOCATION_1 | PACKED    | LOCATION_1          | America/New_York    | true       | -1              |                                                        |                          |                   |                           |                                  |
        When I request a inventory with unit number "<Unit Number>" and product code "<Product Code>"
        Then I receive the following from get inventory by unit number and Product Code:
            | Unit Number   | Product Code   | Temperature Category   | Location   | Collection Location   | Collection TimeZone   | Volumes   | Quarantine Reasons   | Discard Reason   | Unsuitable Reason   | Expired   |
            | <Unit Number> | <Product Code> | <Temperature Category> | <Location> | <Collection Location> | <Collection TimeZone> | <Volumes> | <Quarantine Reasons> | <Discard Reason> | <Unsuitable Reason> | <Expired> |
        Examples:
            | Unit Number   | Product Code | Temperature Category | Location   | Volumes                          | Collection Location | Collection TimeZone | Quarantine Reasons                                     | Discard Reason           | Unsuitable Reason | Expired |
            | W777725016012 | E0869V00     | FROZEN               | LOCATION_1 | Anticoagulant - 50, Volume - 500 | LOCATION_1          | America/New_York    |                                                        |                          |                   | False   |
            | W777725016013 | E0869VA0     | FROZEN               | LOCATION_2 |                                  | LOCATION_2          | America/Los_Angeles |                                                        |                          |                   | False   |
            | W777725016014 | E0869VB0     | FROZEN               | LOCATION_2 |                                  | LOCATION_2          | America/Los_Angeles | ABS_POSITIVE, PENDING_FURTHER_REVIEW_INSPECTION, OTHER |                          |                   | False   |
            | W777725016015 | E0869VC0     | FROZEN               | LOCATION_1 |                                  | LOCATION_1          | America/New_York    |                                                        |                          |                   | True    |
            | W777725016016 | E0869VD0     | FROZEN               | LOCATION_1 |                                  | LOCATION_1          | America/New_York    |                                                        | ADDITIVE_SOLUTION_ISSUES |                   | False   |
            | W777725016017 | E0869VD0     | FROZEN               | LOCATION_1 |                                  | LOCATION_1          | America/New_York    |                                                        |                          | ACTIVE_DEFERRAL   | False   |
            | W777725016017 | E1624V00     | FROZEN               | LOCATION_1 |                                  | LOCATION_1          | America/New_York    |                                                        |                          | TIMING_RULES      | False   |
            | W777725016018 | E0869VD0     | FROZEN               | LOCATION_1 |                                  | LOCATION_1          | America/New_York    |                                                        | OTHER                    |                   | False   |
            | W777725016019 | E0869VD0     | FROZEN               | LOCATION_1 |                                  | LOCATION_1          | America/New_York    |                                                        |                          |                   | False   |
            | W777725016020 | E0869VD0     | FROZEN               | LOCATION_1 |                                  | LOCATION_1          | America/New_York    |                                                        |                          |                   | False   |
            | W777725016021 | E0869VD0     | FROZEN               | LOCATION_1 |                                  | LOCATION_1          | America/New_York    |                                                        |                          |                   | False   |
            | W777725016022 | E0869VD0     | FROZEN               | LOCATION_1 |                                  | LOCATION_1          | America/New_York    |                                                        |                          |                   | True    |











