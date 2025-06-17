package com.arcone.biopro.distribution.receiving.domain.model.vo;

import com.arcone.biopro.distribution.receiving.domain.exception.TypeNotConfiguredException;

import java.util.List;

public record VisualInspection(String value) {

    private static final String SATISFACTORY = "SATISFACTORY";
    private static final String UNSATISFACTORY = "UNSATISFACTORY";
    private static final List<String> validTypes = List.of(SATISFACTORY,UNSATISFACTORY);

    public static VisualInspection SATISFACTORY() {
        return new VisualInspection(SATISFACTORY);
    }

    public static VisualInspection UNSATISFACTORY() {
        return new VisualInspection(UNSATISFACTORY);
    }


    public static VisualInspection getInstance(String value) {
        if (!validTypes.contains(value)) {
            throw new TypeNotConfiguredException("Visual Inspection Not Configured");
        }
        if (SATISFACTORY.equals(value)) {
            return SATISFACTORY();
        } else {
            return UNSATISFACTORY();
        }
    }
}
