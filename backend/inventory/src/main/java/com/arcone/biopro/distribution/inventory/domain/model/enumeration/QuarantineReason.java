package com.arcone.biopro.distribution.inventory.domain.model.enumeration;

import lombok.Getter;

@Getter
public enum QuarantineReason {

    ABS_POSITIVE("quarantine-reason.abs_positive"),
    BCA_UNIT_NEEDED("quarantine-reason.bca_unit_needed"),
    CCP_ELIGIBLE(",quarantine-reason.ccp_eligible"),
    FAILED_VISUAL_INSPECTION("quarantine-reason.failed_visual_inspection"),
    HOLD_UNTIL_EXPIRATION("quarantine-reason.hold_until_expiration"),
    IN_PROCESS_HOLD("quarantine-reason.in_process_hold"),
    PENDING_FURTHER_REVIEW_INSPECTION("quarantine-reason.pending_further_review_inspection"),
    SAVE_PLASMA_FOR_CTS("quarantine-reason.save_plasma_for_cts"),
    OTHER_SEE_COMMENTS("quarantine-reason.other_see_comments"),
    UNDER_INVESTIGATION("quarantine-reason.under_investigation");

    final String description;

    QuarantineReason(String description) {
        this.description = description;
    }
}
