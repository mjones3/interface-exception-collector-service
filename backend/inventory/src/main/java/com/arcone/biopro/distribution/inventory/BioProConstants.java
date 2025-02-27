package com.arcone.biopro.distribution.inventory;

public interface BioProConstants {

    String APPLICATION_NAME = "$name";
    String EXPIRED = "EXPIRED";
    String TEXT_CONFIG_DELIMITER = "; ";

    String UNIT_UNSUITABLE_TOPIC = "UnitUnsuitable";
    String PRODUCT_UNSUITABLE_TOPIC = "ProductUnsuitable";
    String SHIPMENT_COMPLETED_TOPIC = "ShipmentCompleted";
    String LABEL_APPLIED_TOPIC = "LabelApplied";
    String CHECK_IN_COMPLETED_TOPIC = "CheckInCompleted";
    String APHERESIS_RBC_PRODUCT_CREATED_TOPIC = "ApheresisRBCProductCreated";
    String APHERESIS_PLASMA_PRODUCT_CREATED_TOPIC = "ApheresisPlasmaProductCreated";
    String WHOLEBLOOD_CREATED_TOPIC = "WholeBloodProductCreated";
    String PAYLOAD = "payload";

}
