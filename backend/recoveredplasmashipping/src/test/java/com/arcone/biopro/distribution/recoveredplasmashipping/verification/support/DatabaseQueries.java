package com.arcone.biopro.distribution.recoveredplasmashipping.verification.support;

public class DatabaseQueries {

    public static final String GET_LAST_SHIPMENT_ID = "SELECT last_value as id from bld_recovered_plasma_shipment_id_seq";
    public static final String GET_LAST_SHIPMENT_NUMBER = "SELECT last_value as number from bld_recovered_plasma_shipment_number_seq";

    public static String DELETE_SHIPMENTS_BY_CODE(String code) {
        return "DELETE FROM bld_recovered_plasma_shipment WHERE shipment_number LIKE '%" + code + "%'";
    }

    public static String FETCH_SHIPMENT_BY_ID(Integer id) {
        return "SELECT * FROM bld_recovered_plasma_shipment WHERE id = " + id;
    }

}
