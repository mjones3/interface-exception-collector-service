package com.arcone.biopro.distribution.shippingservice.verification.steps;

import com.arcone.biopro.distribution.shippingservice.verification.support.DatabaseService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class DatabaseSteps {

    @Autowired
    private DatabaseService databaseService;

    @Given("I cleaned up from the database the packed item that used the unit number {string}.")
    public void cleanUpPackedUnit(String unitNumber) {
        unitNumber = unitNumber.replace(",", "','");
        var query = String.format("DELETE FROM bld_shipment_item_packed WHERE unit_number in ('%s')", unitNumber);
        databaseService.executeSql(query).block();
    }

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
}
