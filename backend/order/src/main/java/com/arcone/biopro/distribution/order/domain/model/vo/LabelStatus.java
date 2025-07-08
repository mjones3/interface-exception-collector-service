package com.arcone.biopro.distribution.order.domain.model.vo;

import java.util.List;



public record LabelStatus(String value, String description) {

    private static final String LABELED = "LABELED";
    private static final String UNLABELED = "UNLABELED";
    private static final List<String> validStatus = List.of(LABELED,UNLABELED);

    public static void validateLabelStatus(String value) {
        if (!validStatus.contains(value)) {
            throw new IllegalArgumentException("Invalid Label Status");
        }
    }

    public static LabelStatus LABELED() {
        return new LabelStatus(LABELED, "Labeled");
    }
    public static LabelStatus UNLABELED() {
        return new LabelStatus(UNLABELED, "Unlabeled");
    }


    public static LabelStatus getInstance(String value) {
        validateLabelStatus(value);
        if(LABELED.equals(value)){
            return LABELED();
        }else if(UNLABELED.equals(value)){
            return  UNLABELED();
        }
        return null;
    }

}
