package com.arcone.biopro.distribution.receiving.verification.support;

public class DatabaseQueries {

    public static String DELETE_DEVICE_BY_ID_LIKE(String key) {
        return String.format("DELETE FROM BLD_DEVICE WHERE blood_center_id like '%%%s%%'", key);
    }

    public static String UPDATE_TEMPERATURE_ACCEPTABLE_CONFIG(String temperatureCategory, String minTemperature, String maxTemperature) {
        return String.format("UPDATE lk_product_consequence SET result_value = 'TEMPERATURE >= %s && TEMPERATURE <= %s' WHERE result_property = 'TEMPERATURE' AND acceptable is true AND product_category = '%s'", minTemperature, maxTemperature, temperatureCategory);
    }

    public static String UPDATE_TRANSIT_TIME_ACCEPTABLE_CONFIG(String temperatureCategory, String minTransitTime, String maxTransitTime) {
        return String.format("UPDATE lk_product_consequence SET result_value = 'TRANSIT_TIME >= %s && TRANSIT_TIME <= %s' WHERE result_property = 'TRANSIT_TIME' AND acceptable is true AND product_category = '%s'", minTransitTime, maxTransitTime, temperatureCategory);
    }

    public static String UPDATE_LOCATION_TZ(String locationCode, String tz) {
        return String.format("UPDATE lk_location_property SET property_value = '%s' WHERE location_id = (SELECT id FROM lk_location WHERE code = '%s') AND property_key = 'TZ'", tz, locationCode);
    }
}
