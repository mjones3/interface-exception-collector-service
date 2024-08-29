package com.arcone.biopro.distribution.order.verification.support;

public class DatabaseQueries {
    public static String insertBioProOrder(String externalId, String locationCode, Integer priority, String deliveryType, String status) {
        return String.format("INSERT INTO bld_order (external_id, location_code, priority, delivery_type, status, shipping_method, desired_shipping_date, billing_customer_code, billing_customer_name, shipping_customer_code, shipping_customer_name, create_date, modification_date, product_category, create_employee_id) " +
            "VALUES ('%s', '%s', '%s', '%s', '%s', 'FEDEX', '2024-12-25', 'A1235', 'BILLING NAME', 'A1235', 'Creative Testing Solutions', CURRENT_DATE, CURRENT_DATE, 'FROZEN', 'ee1bf88e-2137-4a17-835a-d43e7b738374')", externalId, locationCode, priority, deliveryType, status);
    }

    public static String insertBioProOrderWithDetails(String externalId, String locationCode, Integer priority, String deliveryType, String status, String shipmentType, String shippingMethod, String productCategory, String desiredShipDate, String shippingCustomerCode, String shippingCustomerName, String billingCustomerCode, String billingCustomerName, String comments) {
        return String.format("INSERT INTO bld_order (external_id, location_code, priority, delivery_type, status, shipment_type, shipping_method, product_category, desired_shipping_date, shipping_customer_code, shipping_customer_name, billing_customer_code, billing_customer_name, comments, create_employee_id, create_date, modification_date) " +
            "VALUES ('%s', '%s', %s, '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', 'ee1bf88e-2137-4a17-835a-d43e7b738374',current_date, current_date)", externalId, locationCode, priority, deliveryType, status, shipmentType, shippingMethod, productCategory, desiredShipDate, shippingCustomerCode, shippingCustomerName, billingCustomerCode, billingCustomerName, comments);
    }

    public static String insertBioProOrderItem(String externalId, String productFamily, String bloodType, Integer quantity, String comments) {
        return String.format("INSERT INTO bld_order_item (order_id, product_family, blood_type, quantity, comments, create_date, modification_date) " +
            "VALUES ((SELECT id FROM bld_order WHERE external_id = '%s'), '%s', '%s', %s, '%s',current_date ,current_date)", externalId, productFamily, bloodType, quantity, comments);
    }

    public static String countOrdersByExternalId(String externalId) {
        return String.format("SELECT count(*) FROM bld_order WHERE external_id = '%s'", externalId);
    }

    public static String deleteOrderItemsByExternalId(String externalId) {
        return String.format("DELETE FROM bld_order_item WHERE order_id in ( SELECT id from bld_order WHERE external_id in %s)", externalId);
    }

    public static String deleteOrderItemsByExternalIdStartingWith(String externalIdPrefix) {
        return String.format("DELETE FROM bld_order_item WHERE order_id in ( SELECT id from bld_order WHERE external_id like '%s%%')", externalIdPrefix);
    }

    public static String deleteOrdersByExternalId(String externalId) {
        return String.format("DELETE FROM bld_order WHERE external_id in %s", externalId);
    }

    public static String deleteOrdersByExternalIdStartingWith(String externalIdPrefix) {
        return String.format("DELETE FROM bld_order WHERE external_id like '%s%%'", externalIdPrefix);
    }

    public static String updatePriorityColor(String priority, String colorHex) {
        return String.format("UPDATE lk_lookup SET option_value = '%s' WHERE type = 'ORDER_PRIORITY_COLOR' and description_key = '%s'", colorHex, priority);
    }

    public static String restoreDefaultPriorityColors() {
        return """
            DELETE FROM lk_lookup WHERE type = 'ORDER_PRIORITY_COLOR';
            INSERT into lk_lookup (id, type, description_key, option_value, order_number, active) values ( 27, 'ORDER_PRIORITY_COLOR','STAT', '#ff3333',1, true);
            INSERT into lk_lookup (id, type, description_key, option_value, order_number, active) values ( 28, 'ORDER_PRIORITY_COLOR','ASAP', '#ffb833',2, true);
            INSERT into lk_lookup (id, type, description_key, option_value, order_number, active) values ( 29, 'ORDER_PRIORITY_COLOR','ROUTINE', '#d7d6d3',3, true);
            INSERT into lk_lookup (id, type, description_key, option_value, order_number, active) values ( 30, 'ORDER_PRIORITY_COLOR','SCHEDULED', '#97a6f2',4, true);
            INSERT into lk_lookup (id, type, description_key, option_value, order_number, active) values ( 31, 'ORDER_PRIORITY_COLOR', 'DATE-TIME', '#0930f6',5, true);
            """;
    }

    public static String getOrderId(String externalId) {
        return String.format("SELECT id FROM bld_order WHERE external_id = '%s'", externalId);
    }

    public static String deleteShipmentsByOrderExternalIdStartingWith(String externalIdPrefix) {
        return String.format("DELETE FROM bld_order_shipment WHERE order_id in ( SELECT id from bld_order WHERE external_id like '%s%%')", externalIdPrefix);
    }

    public static String insertBioProOrderShipment(String externalId){
        return String.format("INSERT INTO bld_order_shipment (order_id, shipment_id, shipment_status, create_date, modification_date) " +
            "VALUES ((SELECT id FROM bld_order WHERE external_id = '%s'), 1, 'OPEN', current_date, current_date)", externalId);
    }
}
