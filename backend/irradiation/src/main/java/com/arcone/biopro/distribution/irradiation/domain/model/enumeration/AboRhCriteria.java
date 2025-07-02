package com.arcone.biopro.distribution.irradiation.domain.model.enumeration;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum AboRhCriteria {

    ON(List.of(AboRhType.ON)),
    OP(List.of(AboRhType.OP)),
    AN(List.of(AboRhType.AN)),
    AP(List.of(AboRhType.AP)),
    BN(List.of(AboRhType.BN)),
    BP(List.of(AboRhType.BP)),
    ABN(List.of(AboRhType.ABN)),
    ABP(List.of(AboRhType.ABP)),

    O(List.of(AboRhType.ON, AboRhType.OP)),
    A(List.of(AboRhType.AN, AboRhType.AP)),
    B(List.of(AboRhType.BN, AboRhType.BP)),
    AB(List.of(AboRhType.ABN, AboRhType.ABP)),

    ANY(Arrays.asList(AboRhType.values()));

    private final List<AboRhType> aboRhTypes;

    AboRhCriteria(List<AboRhType> aboRhTypes) {
        this.aboRhTypes = aboRhTypes;
    }

}


