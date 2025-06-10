package com.arcone.biopro.distribution.receiving.verification.support;

public class DatabaseQueries {

    public static String DELETE_DEVICE_BY_ID_LIKE(String key) {
        return String.format("DELETE FROM BLD_DEVICE WHERE blood_center_id like '%%%s%%'", key);
    }
}
