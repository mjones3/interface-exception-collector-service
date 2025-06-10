package com.arcone.biopro.distribution.receiving.domain.model.vo;

import com.arcone.biopro.distribution.receiving.domain.exception.TypeNotConfiguredException;

import java.util.List;

public record AboRh(String value, String description) {

      private static final String ON = "ON";
      private static final String OP = "OP";
      private static final String AN = "AN";
      private static final String AP = "AP";
      private static final String BN = "BN";
      private static final String BP = "BP";
      private static final String ABN = "ABN";
      private static final String ABP = "ABP";


    private static final List<String> validAboRh = List.of(ON,OP,AN,AP,BN,BP,ABN,ABP);

    public static void validateAboRh(String value) {
        if (!validAboRh.contains(value)) {
            throw new TypeNotConfiguredException("ABO_RH_NOT_CONFIGURED");
        }
    }

    public static AboRh ON() {
        return new AboRh(ON, "O Negative");
    }
    public static AboRh AN() {
        return new AboRh(AN, "A Negative");
    }
    public static AboRh BN() {
        return new AboRh(BN, "B Negative");
    }
    public static AboRh ABN() {
        return new AboRh(ABN, "AB Negative");
    }
    public static AboRh OP() {
        return new AboRh(OP, "O Positive");
    }
    public static AboRh AP() {
        return new AboRh(AP, "A Positive");
    }
    public static AboRh BP() {
        return new AboRh(BP, "B Positive");
    }
    public static AboRh ABP() {
        return new AboRh(ABP, "AB Positive");
    }

    public static AboRh getInstance(String value) {
        validateAboRh(value);
        switch (value) {
            case AP:
                return AP();
            case AN:
                return AN();
            case BN:
                return BN();
            case BP:
                return BP();
            case ABN:
                return ABN();
            case ABP:
                return ABP();
            default:
                return null;
        }
    }
}
