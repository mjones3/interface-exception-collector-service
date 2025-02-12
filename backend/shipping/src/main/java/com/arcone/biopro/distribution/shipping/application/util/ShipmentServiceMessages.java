package com.arcone.biopro.distribution.shipping.application.util;

public class ShipmentServiceMessages {

    //    Error messages
    public static final String SHIPMENT_NOT_FOUND_ERROR = "Shipment not found";
    public static final String SHIPMENT_OPEN_ERROR = "Error opening shipment";
    public static final String SHIPMENT_COMPLETED_ERROR = "Shipment already completed";
    public static final String SHIPMENT_ITEM_NOT_FOUND_ERROR = "Shipment item not found";
    public static final String PRODUCT_ALREADY_USED_ERROR = "Product already used";
    public static final String PRODUCT_CRITERIA_QUANTITY_ERROR = "Quantity exceeded";
    public static final String PRODUCT_CRITERIA_BLOOD_TYPE_ERROR = "Blood type does not match";
    public static final String PRODUCT_CRITERIA_FAMILY_ERROR = "Product Family does not match";
    public static final String PRODUCT_CRITERIA_VISUAL_INSPECTION_ERROR = "This product has been discarded for failed visual inspection in the system. Place in biohazard container.";
    public static final String INVENTORY_TEST_ERROR = "Inventory test failed";
    public static final String INVENTORY_SERVICE_NOT_AVAILABLE_ERROR = "Inventory Service is down. Contact Support.";
    public static final String INVENTORY_EXPIRED_ERROR = "This product is expired and has been discarded. Place in biohazard container.";
    public static final String INVENTORY_DISCARDED_ERROR = "This product is discarded and cannot be shipped";
    public static final String INVENTORY_QUARANTINED_ERROR = "This product is quarantined and cannot be shipped";
    public static final String INVENTORY_NOT_FOUND_ERROR = "This product is not in the inventory and cannot be shipped";
    public static final String FACILITY_NOT_FOUND_ERROR = "Facility not found";
    public static final String INVENTORY_VALIDATION_FAILED = "Inventory Validation failed";
    public static final String SECOND_VERIFICATION_NOT_COMPLETED_ERROR = "Shipment cannot be completed because second verification is not completed";
    public static final String SECOND_VERIFICATION_UNIT_NOT_PACKED_ERROR = "The verification does not match all products in this order. Please re-scan all the products.";
    public static final String SECOND_VERIFICATION_ALREADY_COMPLETED_ERROR = "This product has already been verified. Please re-scan all the products in the order.";
    public static final String SHIPMENT_VALIDATION_COMPLETED_ERROR = "One or more products have changed status. You must rescan the products to be removed.";
    public static final String SECOND_VERIFICATION_UNIT_NOT_TOBE_REMOVED_ERROR = "The verification does not match all products in this order. Please re-scan all the products.";
    public static final String SHIPMENT_WITH_INELIGIBLE_PRODUCTS_ERROR = "Shipment cannot be completed because contains product(s) that are not eligible for shipping.";
    public static final String SECOND_VERIFICATION_WITH_INELIGIBLE_PRODUCTS_ERROR = "Second Verification cannot be cancelled because there are product(s) that should be removed from the shipment.";
    public static final String SECOND_VERIFICATION_CANCEL_CONFIRMATION = "When cancelling, all verified products will be removed. Are you sure you want to cancel and remove all the products?";
    public static final String SECOND_VERIFICATION_WITH_SHIPMENT_COMPLETED_ERROR = "Second Verification cannot be cancelled because the shipment is already completed.";
    public static final String UNPACK_PRODUCT_NOT_FOUND_ERROR = "Product cannot be removed because does not exist";
    public static final String UNPACK_SHIPMENT_ITEM_NOT_FOUND_ERROR = "Product cannot be removed because line item does not exist";
    public static final String UNPACK_SHIPMENT_NOT_FOUND_ERROR = "Product cannot be removed because shipment does not exist";
    public static final String UNPACK_SHIPMENT_COMPLETED_ERROR = "Product cannot be removed because shipment is completed";


    //    Success messages
    public static final String SHIPMENT_COMPLETED_SUCCESS = "Shipment completed";
    public static final String SECOND_VERIFICATION_CANCEL_SUCCESS = "Second verification cancellation completed";
    public static final String UNPACK_ITEM_SUCCESS = "Product(s) successfully removed";

}
