package com.arcone.biopro.distribution.order.verification.steps;

import com.arcone.biopro.distribution.order.verification.support.DatabaseQueries;
import com.arcone.biopro.distribution.order.verification.support.DatabaseService;
import com.arcone.biopro.distribution.order.verification.support.SharedContext;
import com.arcone.biopro.distribution.order.verification.support.TestUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

public class DatabaseSteps {
    @Autowired
    private DatabaseService databaseService;
    @Autowired
    private TestUtils testUtils;
    @Autowired
    private SharedContext context;

    @And("I cleaned up from the database, all shipments with order number {string}.")
    public void cleanUpShipments(String orderNumber) {
        var query0 = String.format("""
            delete from bld_shipment_item_packed where shipment_item_id in (select id
                                                                               from bld_shipment_item where shipment_id in (select id from bld_shipment where order_number in (%s)));
            """, orderNumber);
        databaseService.executeSql(query0).block();

        var query1 = String.format("""
            delete from bld_shipment_item_short_date_product where shipment_item_id in (select id
                                                                                          from bld_shipment_item where shipment_id in (select id from bld_shipment where order_number in (%s)));
            """, orderNumber);
        databaseService.executeSql(query1).block();

        var query2 = String.format("""
            delete from "bld_shipment_item" where shipment_id in (select id from bld_shipment where order_number in (%s));
            """, orderNumber);
        databaseService.executeSql(query2).block();

        var query3 = String.format("""
                        delete from bld_shipment where order_number in (%s);
            """, orderNumber);
        databaseService.executeSql(query3).block();
    }

    @Given("I cleaned up from the database the orders with external ID {string}.")
    public void cleanUpOrders(String externalId) {
        var externalIdParam = testUtils.formatSqlCommaSeparatedInParamList(externalId);
        var childQuery = DatabaseQueries.deleteOrderItemsByExternalId(externalIdParam);
        databaseService.executeSql(childQuery).block();
        var query = DatabaseQueries.deleteOrdersByExternalId(externalIdParam);
        databaseService.executeSql(query).block();
    }

    @Given("I cleaned up from the database the orders with order numbers {string}.")
    public void cleanUpOrdersByOrderNumbers(String orderNumbers) {
        var query = DatabaseQueries.deleteOrdersByOrderNumbers(orderNumbers);
        databaseService.executeSql(query).block();
    }

    @And("I cleaned up from the database the orders with external ID starting with {string}.")
    public void cleanUpOrdersStartingWith(String externalIdPrefix) {
        String[] prefixList = testUtils.getCommaSeparatedList(externalIdPrefix);

        for (String prefix : prefixList) {
            var shipmentQuery = DatabaseQueries.deleteShipmentsByOrderExternalIdStartingWith(prefix);
            databaseService.executeSql(shipmentQuery).block();
            var childQuery = DatabaseQueries.deleteOrderItemsByExternalIdStartingWith(prefix);
            databaseService.executeSql(childQuery).block();
            var query = DatabaseQueries.deleteOrdersByExternalIdStartingWith(prefix);
            databaseService.executeSql(query).block();
        }
    }

    @And("I have restored the default configuration for the order priority colors.")
    public void restoreDefaultPriorityColors() {
        var query = DatabaseQueries.restoreDefaultPriorityColors();
        databaseService.executeSql(query).block();
    }

    @And("A back order {string} be created with the same external ID and status {string}.")
    public void ifConfiguredABackOrderMustBeCreatedWithTheSameExternalID(String option, String status) {
        var query = DatabaseQueries.countBackOrders(context.getExternalId(), context.getOrderId());
        var data = databaseService.fetchData(query);

        if (option.equalsIgnoreCase("should")) {
            var records = data.first().blockOptional();
            assert records.isPresent();
            var orderData = records.get();
            Assert.assertEquals(orderData.get("status"), status);
            Assert.assertEquals(orderData.get("external_id"), context.getExternalId());
        } else if (option.equalsIgnoreCase("should not")) {
            var records = data.first().blockOptional();
            assert records.isEmpty();
        } else {
            Assert.fail("Invalid value. Use 'Should' or 'Should Not' to define the correct configuration.");
        }
    }

    @And("I have the back order configuration set to {string}.")
    public void iHaveTheBackOrderConfigurationSetTo(String config) {
        boolean backOrderConfig = config.equalsIgnoreCase("true");
        var query = DatabaseQueries.updateBackOrderConfiguration(backOrderConfig);
        databaseService.executeSql(query).block();
        context.setBackOrderConfig(backOrderConfig);
    }

    @And("I have Shipped {string} products of each item line.")
    public void iHaveShippedShippedQuantityProducts(String quantity) {
        var query = DatabaseQueries.insertBioProOrderShipmentQuantity(quantity, context.getOrderId().toString());
        databaseService.executeSql(query).block();
    }

    @Given("I have removed from the database all the configurations for the location {string}.")
    public void iHaveRemovedFromTheDatabaseAllTheConfigurationsForTheLocation(String external_id) {
        var deletePropertySQL = DatabaseQueries.removeLocationPropertyByExternalId(external_id);
        databaseService.executeSql(deletePropertySQL).block();

        var deleteConfigurationSQL = DatabaseQueries.removeLocationByExternalId(external_id);
        databaseService.executeSql(deleteConfigurationSQL).block();
    }

    @Given("The location {string} is configured.")
    public void theLocationIsConfigured(String locationCode) {
        int randomId = (int) (Math.random() * 10000) + 1;
        // lk_location
        var createConfigurationSQL = "INSERT INTO lk_location (id, external_id, code, name, city, state, postal_code, address_line_1, active, create_date, modification_date) " +
            "VALUES (" + randomId + ", '" + locationCode + "', '" + locationCode + "', '" + locationCode + "', 'city', 'state', '000000', 'address_line_1', true, now(), now())";
        databaseService.executeSql(createConfigurationSQL).block();

        // lk_location_property
        var locationPropertySQL = "INSERT INTO lk_location_property (location_id, property_key, property_value) " +
            "VALUES (" + randomId + ", 'RPS_PARTNER_PREFIX', 'AUTO'), " +
            " ((" + randomId + "), 'RPS_LOCATION_SHIPMENT_CODE', 'AUTO'), " +
            " ((" + randomId + "), 'RPS_LOCATION_CARTON_CODE', '" + "MH1" + "'), " +
            " ((" + randomId + "), 'RPS_CARTON_PARTNER_PREFIX', 'AUTO'), " +
            " ((" + randomId + "), 'TZ', 'America/New_York'), " +
            " ((" + randomId + "), 'PHONE_NUMBER', '123-456-7894'), " +
            " ((" + randomId + "), 'RPS_USE_PARTNER_PREFIX', 'true');";
        databaseService.executeSql(locationPropertySQL).block();

        context.setLocationCode(locationCode);
    }
}
