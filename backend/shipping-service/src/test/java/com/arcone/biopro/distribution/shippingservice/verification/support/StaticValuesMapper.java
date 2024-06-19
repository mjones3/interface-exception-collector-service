package com.arcone.biopro.distribution.shippingservice.verification.support;


import java.util.HashMap;
import java.util.Map;

public class StaticValuesMapper {

    public Map<String, String> shipmentFulfillmentRequestAttributes() {
        var map = new HashMap<String, String>();
        map.put("Id", "id");
        map.put("Order Number", "orderNumber");
        map.put("Priority", "priority");
        map.put("Status", "status");
        map.put("Create Date", "createDate");
        return map;
    }

    public Map<String, String> shipmentItemAttributes() {
        var map = new HashMap<String, String>();
        map.put("Shipment Id", "shipmentId");
        map.put("Product Family", "productFamily");
        map.put("Blood Type", "bloodType");
        map.put("Product Quantity", "quantity");
        map.put("Comments", "comments");
        map.put("Short Date Products", "shortDateProducts");
        map.put("Order Number", "orderNumber");
        return map;
    }

    public Map<String, String> shipmentItemShortDateAttributes() {
        var map = new HashMap<String, String>();
        map.put("Id", "id");
        map.put("Shipment Item Id", "shipmentItemId");
        map.put("Unit Number", "unitNumber");
        map.put("Product Code", "productCode");
        map.put("Storage Location", "storageLocation");
        map.put("Comments", "comments");
        map.put("Create Date", "createDate");
        map.put("Modification Date", "modificationDate");
        return map;
    }
}
