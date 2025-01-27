package com.arcone.biopro.distribution.inventory.verification.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ISBTProductUtil {

    private static final String JSON_FILE_PATH = "/json/isbt_products.json";
    private static Map<String, Map<String, String>> productMap;

    static {
        try {
            loadProductData();
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Failed to load ISBT product data: " + e.getMessage());
        }
    }

    // Load product data from JSON
    private static void loadProductData() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<List<Map<String, String>>> typeRef = new TypeReference<>() {
        };

        // Use InputStream to read the resource from the classpath
        try (InputStream inputStream = ISBTProductUtil.class.getResourceAsStream(JSON_FILE_PATH)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + JSON_FILE_PATH);
            }
            List<Map<String, String>> products = objectMapper.readValue(inputStream, typeRef);

            productMap = new HashMap<>();

            for (Map<String, String> product : products) {
                String productCode = product.get("Product Code");   // e.g., "E0869"
                String divisionCode = product.get("Division Code"); // e.g., "00"
                String key = productCode + divisionCode;            // e.g., "E086900"

                // Store product details using the combined key
                productMap.put(key, product);
            }
        }
    }

    // Get full product details by full product code (productCode + divisionCode)
    public static Map<String, String> getProductDetails(String fullProductCode) {
        if (productMap.get(fullProductCode) != null) {
            return productMap.get(fullProductCode);
        }
        if (fullProductCode == null || fullProductCode.length() < 8) {
            throw new IllegalArgumentException("Invalid product code: " + fullProductCode);
        }
        String productCodeKey = fullProductCode.substring(0, 5); // First 5 characters
        String divisionCode = fullProductCode.substring(6, 8);   // Next 2 characters
        String key = productCodeKey + divisionCode;

        return productMap.get(key);
    }

    // Get Product Family
    public static String getProductFamily(String fullProductCode) {
        Map<String, String> details = getProductDetails(fullProductCode);
        return details != null ? details.get("Product Family") : null;
    }

    // Get Product Description
    public static String getProductDescription(String fullProductCode) {
        Map<String, String> details = getProductDetails(fullProductCode);
        return details != null ? details.get("Product Description") : null;
    }

    // Get ISBT Product Description
    public static String getISBTProductDescription(String fullProductCode) {
        Map<String, String> details = getProductDetails(fullProductCode);
        return details != null ? details.get("ISBT Product Description") : null;
    }

    // Get Product Temperature Category
    public static String getProductTemperatureCategory(String fullProductCode) {
        Map<String, String> details = getProductDetails(fullProductCode);
        return details != null ? details.get("Temperature Category") : null;
    }

    // Get Expiration in Days
    public static int getExpirationInDays(String fullProductCode) {
        Map<String, String> details = getProductDetails(fullProductCode);
        if (details != null) {
            String expirationStr = details.get("Expiration in Days");
            if (expirationStr != null) {
                try {
                    return Integer.parseInt(expirationStr);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid expiration value for product code: " + fullProductCode);
                }
            }
        }
        throw new IllegalArgumentException("Expiration in Days not found for product code: " + fullProductCode);
    }
}
