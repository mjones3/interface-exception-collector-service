package com.arcone.biopro.distribution.order.verification.steps;

import com.arcone.biopro.distribution.order.verification.support.DatabaseQueries;
import com.arcone.biopro.distribution.order.verification.support.DatabaseService;
import com.arcone.biopro.distribution.order.verification.support.TestUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;

public class DatabaseSteps {
    @Autowired
    private DatabaseService databaseService;
    @Autowired
    private TestUtils testUtils;

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

    @And("I cleaned up from the database the orders with external ID starting with {string}.")
    public void cleanUpOrdersStartingWith(String externalIdPrefix) {
        var shipmentQuery = DatabaseQueries.deleteShipmentsByOrderExternalIdStartingWith(externalIdPrefix);
        databaseService.executeSql(shipmentQuery).block();
        var childQuery = DatabaseQueries.deleteOrderItemsByExternalIdStartingWith(externalIdPrefix);
        databaseService.executeSql(childQuery).block();
        var query = DatabaseQueries.deleteOrdersByExternalIdStartingWith(externalIdPrefix);
        databaseService.executeSql(query).block();
    }

    @And("I have restored the default configuration for the order priority colors.")
    public void restoreDefaultPriorityColors() {
        var query = DatabaseQueries.restoreDefaultPriorityColors();
        databaseService.executeSql(query).block();
    }
}
