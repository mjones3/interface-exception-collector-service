package com.arcone.biopro.distribution.irradiation.verification.utils;

public class CheckDigitUtil {

    public static String calculateDigitCheck(String unitNumber) {
        var ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ*";
        var sum = 0;
        for (int i = 0; i < unitNumber.length(); i++) {
            var val = ALPHABET.indexOf(unitNumber.charAt(i));
            sum = ((sum + val) * 2) % 37;
        }

        var calculatedChecksum = (38 - (sum % 37)) % 37; // Modulo 37 calculation
        return String.valueOf(ALPHABET.charAt(calculatedChecksum));
    }
}
