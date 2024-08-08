package com.arcone.biopro.distribution.order.verification.support;

public class DatabaseQueries {
    public static String insertBioProOrder(String externalId, String locationCode, String priority, String status){
        return String.format("INSERT INTO bld_order (external_id, location_code, priority, status, shipping_method, desired_shipping_date, billing_customer_code, billing_customer_name, shipping_customer_code, shipping_customer_name, create_date, modification_date, product_category, create_employee_id) " +
            "VALUES ('%s', '%s', '%s', '%s', 'FEDEX', '2020-04-25', 'A1235', 'BILLING NAME', 'A1235', 'SHIPPING NAME', CURRENT_DATE, CURRENT_DATE, 'FROZEN', 'ee1bf88e-2137-4a17-835a-d43e7b738374')", externalId, locationCode, priority, status);
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
}
