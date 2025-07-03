package com.arcone.biopro.distribution.irradiation.domain.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProductCodeUtil {

    private static final String FINAL_PRODUCT_CODE_WITH_SIXTH_DIGIT = "^([A-Z][A-Z0-9]\\d{3})([A-Z0-9])([A-Z0-9]{2})$";

    private static final String FINAL_PRODUCT_CODE_WITHOUT_DIGIT = "^([A-Z][A-Z0-9]\\d{3})([A-Z0-9]{2})$";

    public static String retrieveFinalProductCodeWithoutSixthDigit(String finalProductCode) {
        if(isAFinalProductCodeWithSixthDigit(finalProductCode)) {
            return withoutSixthDigit(finalProductCode);
        }
        return finalProductCode;
    }

    private static String withoutSixthDigit(String finalProductCode) {
        Pattern pattern = Pattern.compile(FINAL_PRODUCT_CODE_WITH_SIXTH_DIGIT, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(finalProductCode);
        if (matcher.find()) {
            return matcher.group(1).trim() + matcher.group(3).trim();
        }
        return finalProductCode;
    }

    public static boolean isAFinalProductCodeWithSixthDigit(String productCode) {
        return productCode.matches(FINAL_PRODUCT_CODE_WITH_SIXTH_DIGIT);
    }

    public static boolean isAFinalProductCode(String productCode) {
        return productCode.matches(FINAL_PRODUCT_CODE_WITHOUT_DIGIT);
    }
}
