# Feature Unit Number reference: W036825016000
@api @LAB-440 @skipOnPipeline
Feature: Get Inventory

    Scenario Outline: Inventory by Unit Number
        Given I have the following inventories:
            | Unit Number   | Product Code | Location   | Status    | Collection Location | Collection TimeZone | Is Labeled | Expires In Days | Quarantine Reasons                                     | Discard Reason           | Unsuitable Reason | Comments                  | Volumes                          |
            | W036825016001 | E0869V00     | LOCATION_1 | AVAILABLE | LOCATION_1          | America/New_York    | true       | 5               |                                                        |                          |                   |                           | Anticoagulant - 50, Volume - 500 |
            | W036825016002 | E0869VA0     | LOCATION_2 | AVAILABLE | LOCATION_2          | America/Los_Angeles | true       | 5               |                                                        |                          |                   |                           |                                  |
            | W036825016003 | E0869VB0     | LOCATION_2 | AVAILABLE | LOCATION_2          | America/Los_Angeles | true       | 5               | ABS_POSITIVE, PENDING_FURTHER_REVIEW_INSPECTION, OTHER |                          |                   | Quarantine other comments |                                  |
            | W036825016004 | E0869VC0     | LOCATION_1 | AVAILABLE | LOCATION_1          | America/New_York    | true       | -1              |                                                        |                          |                   |                           |                                  |
            | W036825016005 | E0869VD0     | LOCATION_1 | DISCARDED | LOCATION_1          | America/New_York    | true       | 5               |                                                        | ADDITIVE_SOLUTION_ISSUES |                   |                           |                                  |
            | W036825016006 | E0869VD0     | LOCATION_1 | AVAILABLE | LOCATION_1          | America/New_York    | true       | 1               |                                                        |                          | ACTIVE_DEFERRAL   |                           |                                  |
            | W036825016006 | E1624V00     | LOCATION_1 | AVAILABLE | LOCATION_1          | America/New_York    | true       | 1               |                                                        |                          | TIMING_RULES      |                           |                                  |
            | W036825016007 | E0869VD0     | LOCATION_1 | DISCARDED | LOCATION_1          | America/New_York    | true       | 5               |                                                        | OTHER                    |                   | Some comments             |                                  |
            | W036825016008 | E0869VD0     | LOCATION_1 | AVAILABLE | LOCATION_1          | America/New_York    | false      | 5               |                                                        |                          |                   |                           |                                  |
            | W036825016009 | E0869VD0     | LOCATION_1 | PACKED    | LOCATION_1          | America/New_York    | true       | 5               |                                                        |                          |                   |                           |                                  |
            | W036825016010 | E0869VD0     | LOCATION_1 | SHIPPED   | LOCATION_1          | America/New_York    | true       | 5               |                                                        |                          |                   |                           |                                  |
            | W036825016011 | E0869VD0     | LOCATION_1 | PACKED    | LOCATION_1          | America/New_York    | true       | -1              |                                                        |                          |                   |                           |                                  |

        When I request a inventory with unit number "<Unit Number>"
        Then I receive the following from get inventory by unit number:
            | Unit Number   | Product Code   | Temperature Category   | Location   | Collection Location   | Collection TimeZone   | Volumes   | Quarantine Reasons   | Discard Reason   | Unsuitable Reason   | Expired   |
            | <Unit Number> | <Product Code> | <Temperature Category> | <Location> | <Collection Location> | <Collection TimeZone> | <Volumes> | <Quarantine Reasons> | <Discard Reason> | <Unsuitable Reason> | <Expired> |

        Examples:
            | Unit Number   | Product Code | Temperature Category | Location   | Volumes                          | Collection Location | Collection TimeZone | Quarantine Reasons                                     | Discard Reason           | Unsuitable Reason | Expired |
            | W036825016001 | E0869V00     | FROZEN               | LOCATION_1 | Anticoagulant - 50, Volume - 500 | LOCATION_1          | America/New_York    |                                                        |                          |                   | False   |
            | W036825016002 | E0869VA0     | FROZEN               | LOCATION_2 |                                  | LOCATION_2          | America/Los_Angeles |                                                        |                          |                   | False   |
            | W036825016003 | E0869VB0     | FROZEN               | LOCATION_2 |                                  | LOCATION_2          | America/Los_Angeles | ABS_POSITIVE, PENDING_FURTHER_REVIEW_INSPECTION, OTHER |                          |                   | False   |
            | W036825016004 | E0869VC0     | FROZEN               | LOCATION_1 |                                  | LOCATION_1          | America/New_York    |                                                        |                          |                   | True    |
            | W036825016005 | E0869VD0     | FROZEN               | LOCATION_1 |                                  | LOCATION_1          | America/New_York    |                                                        | ADDITIVE_SOLUTION_ISSUES |                   | False   |
            | W036825016006 | E0869VD0     | FROZEN               | LOCATION_1 |                                  | LOCATION_1          | America/New_York    |                                                        |                          | ACTIVE_DEFERRAL   | False   |
            | W036825016006 | E1624V00     | FROZEN               | LOCATION_1 |                                  | LOCATION_1          | America/New_York    |                                                        |                          | TIMING_RULES      | False   |
            | W036825016007 | E0869VD0     | FROZEN               | LOCATION_1 |                                  | LOCATION_1          | America/New_York    |                                                        |                          |                   | False   |
            | W036825016008 | E0869VD0     | FROZEN               | LOCATION_1 |                                  | LOCATION_1          | America/New_York    |                                                        | OTHER                    |                   | False   |
            | W036825016009 | E0869VD0     | FROZEN               | LOCATION_1 |                                  | LOCATION_1          | America/New_York    |                                                        |                          |                   | False   |
            | W036825016010 | E0869VD0     | FROZEN               | LOCATION_1 |                                  | LOCATION_1          | America/New_York    |                                                        |                          |                   | False   |
            | W036825016011 | E0869VD0     | FROZEN               | LOCATION_1 |                                  | LOCATION_1          | America/New_York    |                                                        |                          |                   | True    |


    Scenario Outline: Inventory by Unit Number and Product Code
        Given I have the following inventories:
            | Unit Number   | Product Code | Location   | Status    | Collection Location | Collection TimeZone | Is Labeled | Expires In Days | Quarantine Reasons                                     | Discard Reason           | Unsuitable Reason | Comments                  | Volumes                          |
            | W036825016012 | E0869V00     | LOCATION_1 | AVAILABLE | LOCATION_1          | America/New_York    | true       | 5               |                                                        |                          |                   |                           | Anticoagulant - 50, Volume - 500 |
            | W036825016013 | E0869VA0     | LOCATION_2 | AVAILABLE | LOCATION_2          | America/Los_Angeles | true       | 5               |                                                        |                          |                   |                           |                                  |
            | W036825016014 | E0869VB0     | LOCATION_2 | AVAILABLE | LOCATION_2          | America/Los_Angeles | true       | 5               | ABS_POSITIVE, PENDING_FURTHER_REVIEW_INSPECTION, OTHER |                          |                   | Quarantine other comments |                                  |
            | W036825016015 | E0869VC0     | LOCATION_1 | AVAILABLE | LOCATION_1          | America/New_York    | true       | -1              |                                                        |                          |                   |                           |                                  |
            | W036825016016 | E0869VD0     | LOCATION_1 | DISCARDED | LOCATION_1          | America/New_York    | true       | 5               |                                                        | ADDITIVE_SOLUTION_ISSUES |                   |                           |                                  |
            | W036825016017 | E0869VD0     | LOCATION_1 | AVAILABLE | LOCATION_1          | America/New_York    | true       | 1               |                                                        |                          | ACTIVE_DEFERRAL   |                           |                                  |
            | W036825016017 | E1624V00     | LOCATION_1 | AVAILABLE | LOCATION_1          | America/New_York    | true       | 1               |                                                        |                          | TIMING_RULES      |                           |                                  |
            | W036825016018 | E0869VD0     | LOCATION_1 | DISCARDED | LOCATION_1          | America/New_York    | true       | 5               |                                                        | OTHER                    |                   | Some comments             |                                  |
            | W036825016019 | E0869VD0     | LOCATION_1 | AVAILABLE | LOCATION_1          | America/New_York    | false      | 5               |                                                        |                          |                   |                           |                                  |
            | W036825016020 | E0869VD0     | LOCATION_1 | PACKED    | LOCATION_1          | America/New_York    | true       | 5               |                                                        |                          |                   |                           |                                  |
            | W036825016021 | E0869VD0     | LOCATION_1 | SHIPPED   | LOCATION_1          | America/New_York    | true       | 5               |                                                        |                          |                   |                           |                                  |
            | W036825016022 | E0869VD0     | LOCATION_1 | PACKED    | LOCATION_1          | America/New_York    | true       | -1              |                                                        |                          |                   |                           |                                  |
        When I request a inventory with unit number "<Unit Number>" and product code "<Product Code>"
        Then I receive the following from get inventory by unit number:
            | Unit Number   | Product Code   | Temperature Category   | Location   | Collection Location   | Collection TimeZone   | Volumes   | Quarantine Reasons   | Discard Reason   | Unsuitable Reason   |
            | <Unit Number> | <Product Code> | <Temperature Category> | <Location> | <Collection Location> | <Collection TimeZone> | <Volumes> | <Quarantine Reasons> | <Discard Reason> | <Unsuitable Reason> |
        Examples:
            | Unit Number   | Product Code | Temperature Category | Location   | Volumes                          | Collection Location | Collection TimeZone | Quarantine Reasons                                     | Discard Reason           | Unsuitable Reason | Expired |
            | W036825016012 | E0869V00     | FROZEN               | LOCATION_1 | Anticoagulant - 50, Volume - 500 | LOCATION_1          | America/New_York    |                                                        |                          |                   | False   |
            | W036825016013 | E0869VA0     | FROZEN               | LOCATION_2 |                                  | LOCATION_2          | America/Los_Angeles |                                                        |                          |                   | False   |
            | W036825016014 | E0869VB0     | FROZEN               | LOCATION_2 |                                  | LOCATION_2          | America/Los_Angeles | ABS_POSITIVE, PENDING_FURTHER_REVIEW_INSPECTION, OTHER |                          |                   | False   |
            | W036825016015 | E0869VC0     | FROZEN               | LOCATION_1 |                                  | LOCATION_1          | America/New_York    |                                                        |                          |                   | True    |
            | W036825016016 | E0869VD0     | FROZEN               | LOCATION_1 |                                  | LOCATION_1          | America/New_York    |                                                        | ADDITIVE_SOLUTION_ISSUES |                   | False   |
            | W036825016017 | E0869VD0     | FROZEN               | LOCATION_1 |                                  | LOCATION_1          | America/New_York    |                                                        |                          | ACTIVE_DEFERRAL   | False   |
            | W036825016017 | E1624V00     | FROZEN               | LOCATION_1 |                                  | LOCATION_1          | America/New_York    |                                                        |                          | TIMING_RULES      | False   |
            | W036825016018 | E0869VD0     | FROZEN               | LOCATION_1 |                                  | LOCATION_1          | America/New_York    |                                                        |                          |                   | False   |
            | W036825016019 | E0869VD0     | FROZEN               | LOCATION_1 |                                  | LOCATION_1          | America/New_York    |                                                        | OTHER                    |                   | False   |
            | W036825016020 | E0869VD0     | FROZEN               | LOCATION_1 |                                  | LOCATION_1          | America/New_York    |                                                        |                          |                   | False   |
            | W036825016021 | E0869VD0     | FROZEN               | LOCATION_1 |                                  | LOCATION_1          | America/New_York    |                                                        |                          |                   | False   |
            | W036825016022 | E0869VD0     | FROZEN               | LOCATION_1 |                                  | LOCATION_1          | America/New_York    |                                                        |                          |                   | True    |











