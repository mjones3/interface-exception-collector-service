package com.arcone.biopro.distribution.recoveredplasmashipping.verification.support;

public class DatabaseQueries {

    public static final String GET_LAST_SHIPMENT_ID = "SELECT last_value as id from bld_recovered_plasma_shipment_id_seq";
    public static final String GET_LAST_SHIPMENT_NUMBER = "SELECT last_value as number from bld_recovered_plasma_shipment_number_seq";

    public static String DELETE_SHIPMENTS_BY_CODE(String code) {
        return "DELETE FROM bld_recovered_plasma_shipment WHERE shipment_number LIKE '%" + code + "%'";
    }

    public static String DELETE_CARTONS_BY_SHIPMENT_CODE(String code) {
        return "DELETE FROM bld_recovered_plasma_shipment_carton WHERE recovered_plasma_shipment_id IN (SELECT id FROM bld_recovered_plasma_shipment WHERE shipment_number LIKE '%" + code + "%')";
    }

    public static String DELETE_CARTON_ITEMS_BY_SHIPMENT_CODE(String code) {
        return "DELETE FROM bld_recovered_plasma_shipment_carton_item WHERE carton_id IN (SELECT id FROM bld_recovered_plasma_shipment_carton WHERE recovered_plasma_shipment_id IN (SELECT id FROM bld_recovered_plasma_shipment WHERE shipment_number LIKE '%" + code + "%'))";
    }

    public static String FETCH_SHIPMENT_BY_ID(Integer id) {
        return "SELECT * FROM bld_recovered_plasma_shipment WHERE id = " + id;
    }

    public static String REMOVE_SHIPMENTS_BY_LOCATION_AND_TRANSPORTATION_REF_NUMBER(String location, String transportationRefNumber) {
        return "DELETE FROM bld_recovered_plasma_shipment WHERE location_code = '" + location + "' AND transportation_reference_number = '" + transportationRefNumber + "'";
    }

    public static String REMOVE_CARTONS_BY_LOCATION_AND_TRANSPORTATION_REF_NUMBER(String location, String transportationRefNumber) {
        return "DELETE FROM bld_recovered_plasma_shipment_carton WHERE recovered_plasma_shipment_id IN (SELECT id FROM bld_recovered_plasma_shipment WHERE location_code = '" + location + "' AND transportation_reference_number = '" + transportationRefNumber + "')";
    }

    public static String REMOVE_CARTON_ITEMS_BY_LOCATION_AND_TRANSPORTATION_REF_NUMBER(String location, String transportationRefNumber) {
        return "DELETE FROM bld_recovered_plasma_shipment_carton_item WHERE carton_id IN (SELECT id FROM bld_recovered_plasma_shipment_carton WHERE recovered_plasma_shipment_id IN (SELECT id FROM bld_recovered_plasma_shipment WHERE location_code = '" + location + "' AND transportation_reference_number = '" + transportationRefNumber + "'))";
    }

    public static String INSERT_SHIPMENT(String customerCode, String locationCode, String productType, String status, String shipmentNumber, String scheduleDate) {
        return String.format(
            """
                INSERT INTO bld_recovered_plasma_shipment (
                    -- Required fields
                    customer_code,
                    location_code,
                    product_type,
                    status,
                    shipment_number,
                    create_employee_id,
                    shipment_date,
                    carton_tare_weight,
                    customer_name,
                    customer_state,
                    customer_postal_code,
                    customer_country,
                    customer_country_code,
                    customer_city,
                    customer_address_line1,
                    create_date,
                    modification_date

                ) VALUES (
                    -- Required fields
                    '%s',                               -- customer_code
                    '%s',                               -- location_code
                    '%s',                               -- product_type
                    '%s',                               -- status
                    '%s',                               -- shipment_number
                    '5db1da0b-6392-45ff-86d0-17265ea33226', -- create_employee_id
                    '%s',                                -- shipment_date
                    10.5,                                -- carton_tare_weight
                    'Customer Name',                      -- customer_name
                    'CA',                                -- customer_state
                    '12345',                             -- customer_postal_code
                    'United States',                     -- customer_country
                    'US',                                -- customer_country_code
                    'San Francisco',                     -- customer_city
                    '123 Main St',                       -- customer_address_line1
                    CURRENT_TIMESTAMP,                   -- create_date
                    CURRENT_TIMESTAMP                   -- modification_date
                )
                """
            , customerCode, locationCode, productType, status, shipmentNumber, scheduleDate
        );
    }

    public static String FETCH_SHIPMENT_CRITERIA_BY_CUSTOMER_AND_PRODUCT_TYPE(String customerCode, String productType) {
        return String.format(
            """
                select lrpsc.customer_code, lrpsc.product_type, lrpptpc.product_code , lrpsci."type" , lrpsci.value
                        from lk_recovered_plasma_shipment_criteria lrpsc
                        inner join lk_recovered_plasma_product_type lrppt on lrppt.product_type = lrpsc.product_type
                        inner join lk_recovered_plasma_product_type_product_code lrpptpc on lrpptpc.product_type_id  = lrppt.id
                        inner join lk_recovered_plasma_shipment_criteria_item lrpsci on lrpsci.recovered_plasma_shipment_criteria_id  = lrpsc.id
                        where lrpsc.customer_code = '%s' and lrpsc.product_type = '%s' and lrpsc.active = true
                """
            , customerCode, productType
        );
    }

    public static String UPDATE_MIN_VOLUME_CUSTOMER_PRODUCT_CRITERIA(String customerCode, String productType, String criteriaType, String criteriaValue) {
        criteriaValue = criteriaValue.equals("<null>") ? null : "'" + criteriaValue + "'";

        return String.format(
            """
                UPDATE lk_recovered_plasma_shipment_criteria_item
                SET value = %s
                WHERE recovered_plasma_shipment_criteria_id IN (
                    SELECT id
                    FROM lk_recovered_plasma_shipment_criteria
                    WHERE customer_code = '%s'
                    AND product_type = '%s'
                    AND active = true
                )
                AND "type" = '%s'
                """
            , criteriaValue, customerCode, productType, criteriaType
        );
    }

    public static String RESET_SHIPMENT_CRITERIA_CONFIG(String recoveredPlasmaShipmentCriteriaId, String type, String value, String message, String messageType) {
        return String.format(
            """
                UPDATE lk_recovered_plasma_shipment_criteria_item
                SET value = %s, message = '%s', message_type = '%s'
                WHERE recovered_plasma_shipment_criteria_id = %s
                AND type = '%s'
                """
            , value, message, messageType, recoveredPlasmaShipmentCriteriaId, type
        );
    }

    public static String UPDATE_MAX_PRODUCTS_CUSTOMER_CRITERIA(String customerCode, String productType, String maxValue) {
        return String.format(
            """
                UPDATE lk_recovered_plasma_shipment_criteria_item
                SET value = %s
                WHERE recovered_plasma_shipment_criteria_id IN (
                    SELECT id
                    FROM lk_recovered_plasma_shipment_criteria
                    WHERE customer_code = '%s'
                    AND product_type = '%s'
                    AND active = true
                )
                AND "type" = 'MAXIMUM_UNITS_BY_CARTON'
                """
            , maxValue, customerCode, productType
        );
    }

    public static String UPDATE_MIN_PRODUCTS_CUSTOMER_CRITERIA(String customerCode, String productType, String minValue) {
        return String.format(
            """
                UPDATE lk_recovered_plasma_shipment_criteria_item
                SET value = %s
                WHERE recovered_plasma_shipment_criteria_id IN (
                    SELECT id
                    FROM lk_recovered_plasma_shipment_criteria
                    WHERE customer_code = '%s'
                    AND product_type = '%s'
                    AND active = true
                )
                AND "type" = 'MINIMUM_UNITS_BY_CARTON'
                """
            , minValue, customerCode, productType
        );
    }

    public static String INSERT_PACKED_PRODUCT(String cartonId, String unitNumber, String productCode, String productType) {
        return String.format(
            """
                INSERT INTO bld_recovered_plasma_shipment_carton_item (
                    carton_id,
                    unit_number,
                    product_code,
                    product_type,
                    product_description,
                    create_date,
                    modification_date,
                    packed_by_employee_id,
                    volume,
                    status,
                    weight,
                    expiration_date,
                    abo_rh
                ) VALUES (
                    '%s',              -- carton_id
                    '%s',              -- unit_number
                    '%s',              -- product_code
                    '%s',              -- product_type
                    (select product_type_description from lk_recovered_plasma_product_type where product_type = '%s'), -- product_description
                    CURRENT_TIMESTAMP, -- create_date
                    CURRENT_TIMESTAMP,  -- modification_date,
                    '5db1da0b-6392-45ff-86d0-17265ea33226',
                    250,
                    'PACKED',
                    100,
                    CURRENT_TIMESTAMP,
                    'AP'
                )
                """
            , cartonId, unitNumber, productCode, productType, productType
        );
    }

    public static String UPDATE_SYSTEM_CONFIGURATION(String processType , String propertyKey, String propertyValue) {
        return String.format(
            """
                UPDATE lk_system_process_property
                SET property_value = '%s'
                WHERE system_process_type = '%s' AND property_key = '%s'
                """
            , propertyValue, processType, propertyKey
        );
    }
}
