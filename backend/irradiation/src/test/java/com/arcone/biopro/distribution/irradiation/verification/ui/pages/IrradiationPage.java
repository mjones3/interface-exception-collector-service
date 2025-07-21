package com.arcone.biopro.distribution.irradiation.verification.ui.pages;

/**
 * Common interface for irradiation pages (Start and Close)
 * This interface defines the common methods that both StartIrradiationPage and CloseIrradiationPage must implement.
 */
public interface IrradiationPage {
    
    /**
     * Checks if an input field is enabled
     * 
     * @param fieldName The name of the field to check
     * @return true if the field is enabled, false otherwise
     */
    boolean inputFieldIsEnabled(String fieldName);
    
    /**
     * Scans an irradiator device ID
     * 
     * @param irradiatorDeviceId The ID to scan
     */
    void scanIrradiatorDeviceId(String irradiatorDeviceId);
    
    /**
     * Scans a unit number
     * 
     * @param unitNumber The unit number to scan
     */
    void scanUnitNumber(String unitNumber);
    
    /**
     * Checks if a unit number card exists for the given unit number and product
     * 
     * @param unitNumber The unit number to check
     * @param product The product to check
     * @return true if the card exists, false otherwise
     */
    boolean unitNumberCardExists(String unitNumber, String product);
    
    /**
     * Counts the number of cards for a given unit number and product
     * 
     * @param unitNumber The unit number to count
     * @param product The product to count
     * @return The number of cards found
     */
    int unitNumberProductCardCount(String unitNumber, String product);
    
    /**
     * Checks if a product is in a specific status
     * 
     * @param unitNumber The unit number to check
     * @param product The product to check
     * @param status The status to check for
     * @return true if the product is in the specified status, false otherwise
     */
    boolean isProductInStatus(String unitNumber, String product, String status);
}