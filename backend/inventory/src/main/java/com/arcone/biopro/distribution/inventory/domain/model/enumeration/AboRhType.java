package com.arcone.biopro.distribution.inventory.domain.model.enumeration;

import lombok.Getter;

@Getter
public enum AboRhType {

    ON("O Negative"),
    OP("O Positive"),
    AN("A Negative"),
    AP("A Positive"),
    BN("B Negative"),
    BP("B Positive"),
    ABN("AB Negative"),
    ABP("AB Positive");

    private final String aboRhDescription;

    AboRhType(String aboRhDescription) {
        this.aboRhDescription = aboRhDescription;
    }

}


