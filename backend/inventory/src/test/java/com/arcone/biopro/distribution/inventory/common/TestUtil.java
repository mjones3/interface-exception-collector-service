package com.arcone.biopro.distribution.inventory.common;

import com.arcone.biopro.distribution.inventory.domain.model.vo.Quarantine;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.List;

public class TestUtil {

    public static String randomString(int length) {
        return "W" + RandomStringUtils.random(length - 1, false, true);
    }

    public static List<Quarantine> createQuarantines() {
        return List.of(
            new Quarantine(1L, "ABS_POSITIVE", null),
            new Quarantine(2L, "BCA_UNIT_NEEDED", null),
            new Quarantine(3L, "CCP_ELIGIBLE", null),
            new Quarantine(4L, "FAILED_VISUAL_INSPECTION", null),
            new Quarantine(5L, "OTHER_SEE_COMMENTS", "The blood bag is in quarantine for safety testing to ensure it’s free from contaminants and safe for transfusion.")

        );
    }

}
