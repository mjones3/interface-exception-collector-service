package com.arcone.biopro.distribution.inventory;

public interface BioProConstants {

    String APPLICATION_NAME = "$name";
    String EXPIRED = "EXPIRED";
    String TEXT_CONFIG_DELIMITER = "; ";

    String UNIT_UNSUITABLE_TOPIC = "UnitUnsuitable";
    String PRODUCT_UNSUITABLE_TOPIC = "ProductUnsuitable";
    String PRODUCT_STORED_TOPIC = "ProductStored";
    String PRODUCT_DISCARDED_TOPIC = "ProductDiscarded";
    String QUARANTINE_REMOVED_TOPIC = "QuarantineRemoved";
    String PRODUCT_RECOVERED_TOPIC = "ProductRecovered";
    String PRODUCT_QUARANTINED_TOPIC = "ProductQuarantined";
    String QUARANTINE_UPDATED_TOPIC = "QuarantineUpdated";
    String RECOVER_PLASMA_CARTON_PACKED_TOPIC = "RecoveredPlasmaCartonPacked";
    String RECOVER_PLASMA_CARTON_REMOVED_TOPIC = "RecoveredPlasmaCartonRemoved";
    String RECOVER_PLASMA_CARTON_UNPACKED_TOPIC = "RecoveredPlasmaCartonUnpacked";
    String SHIPMENT_COMPLETED_TOPIC = "ShipmentCompleted";
    String LABEL_APPLIED_TOPIC = "LabelApplied";
    String CHECK_IN_COMPLETED_TOPIC = "CheckInCompleted";
    String APHERESIS_RBC_PRODUCT_CREATED_TOPIC = "ApheresisRBCProductCreated";
    String APHERESIS_PLASMA_PRODUCT_CREATED_TOPIC = "ApheresisPlasmaProductCreated";
    String APHERESIS_PLATELET_PRODUCT_CREATED_TOPIC = "ApheresisPlateletProductCreated";
    String APHERESIS_PLASMA_PRODUCT_COMPLETED_TOPIC = "ApheresisPlasmaProductCompleted";
    String APHERESIS_RBC_PRODUCT_COMPLETED_TOPIC = "ApheresisRBCProductCompleted";
    String APHERESIS_PLATELET_PRODUCT_COMPLETED_TOPIC = "ApheresisPlateletProductCompleted";
    String WHOLEBLOOD_COMPLETED_TOPIC = "WholeBloodProductCompleted";
    String WHOLEBLOOD_CREATED_TOPIC = "WholeBloodProductCreated";
    String PAYLOAD = "payload";
    String UNIT_NUMBER = "unitNumber";
    String PRODUCT_CODE = "productCode";
    String UPDATE_TYPE = "updateType";
    String PROPERTIES = "properties";
    String LICENSURE = "LICENSURE";
    String LABELED = "LABELED";
    String QUARANTINED = "QUARANTINED";
    String LICENSED = "LICENSED";
    String UNLICENSED = "UNLICENSED";
    String INVENTORY_STATUS = "inventoryStatus";
    String INPUT_PRODUCTS = "inputProducts";
    String PRODUCT_VOLUME_TYPE = "volume";
    String PRODUCT_VOLUME_UNIT = "MILLILITERS";

}
