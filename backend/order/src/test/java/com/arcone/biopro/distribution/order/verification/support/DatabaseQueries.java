package com.arcone.biopro.distribution.order.verification.support;

public class DatabaseQueries {
    public static String insertBioProOrder(String externalId, String locationCode, Integer priority, String deliveryType, String status){
        return String.format("INSERT INTO bld_order (external_id, location_code, priority, delivery_type, status, shipping_method, desired_shipping_date, billing_customer_code, billing_customer_name, shipping_customer_code, shipping_customer_name, create_date, modification_date, product_category, create_employee_id) " +
            "VALUES ('%s', '%s', '%s', '%s', '%s', 'FEDEX', '2024-12-25', 'A1235', 'BILLING NAME', 'A1235', 'Creative Testing Solutions', CURRENT_DATE, CURRENT_DATE, 'FROZEN', 'ee1bf88e-2137-4a17-835a-d43e7b738374')", externalId, locationCode, priority, deliveryType, status);
    }

    public static String countOrdersByExternalId(String externalId){
        return String.format("SELECT count(*) FROM bld_order WHERE external_id = '%s'", externalId);
    }

    public static String deleteOrderItemsByExternalId(String externalId){
        return String.format("DELETE FROM bld_order_item WHERE order_id in ( SELECT id from bld_order WHERE external_id in %s)", externalId);
    }

    public static String deleteOrderItemsByExternalIdStartingWith(String externalIdPrefix){
        return String.format("DELETE FROM bld_order_item WHERE order_id in ( SELECT id from bld_order WHERE external_id like '%s%%')", externalIdPrefix);
    }

    public static String deleteOrdersByExternalId(String externalId){
        return String.format("DELETE FROM bld_order WHERE external_id in %s", externalId);
    }

    public static String deleteOrdersByExternalIdStartingWith(String externalIdPrefix){
        return String.format("DELETE FROM bld_order WHERE external_id like '%s%%'", externalIdPrefix);
    }

    public static String updatePriorityColor(String priority, String colorHex){
        return String.format("UPDATE lk_lookup SET option_value = '%s' WHERE type = 'ORDER_PRIORITY_COLOR' and description_key = '%s'", colorHex, priority);
    }

    public static String restoreDefaultPriorityColors(){
        return """
            UPDATE lk_lookup SET option_value = '#ff3333' WHERE type = 'ORDER_PRIORITY_COLOR' and description_key = 'STAT';
            UPDATE lk_lookup SET option_value = '#ffb833' WHERE type = 'ORDER_PRIORITY_COLOR' and description_key = 'ASAP';
            UPDATE lk_lookup SET option_value = '#d7d6d3' WHERE type = 'ORDER_PRIORITY_COLOR' and description_key = 'ROUTINE';
            UPDATE lk_lookup SET option_value = '#97a6f2' WHERE type = 'ORDER_PRIORITY_COLOR' and description_key = 'SCHEDULED';
            UPDATE lk_lookup SET option_value = '#0930f6' WHERE type = 'ORDER_PRIORITY_COLOR' and description_key = 'DATE-TIME';
            """;
    }
}
