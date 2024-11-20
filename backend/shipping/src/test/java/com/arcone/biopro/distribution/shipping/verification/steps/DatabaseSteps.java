package com.arcone.biopro.distribution.shipping.verification.steps;

import com.arcone.biopro.distribution.shipping.verification.support.DatabaseService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
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

    @And("The check digit configuration is {string}.")
    public void updateCheckDigitConfiguration(String value) {
        value = value.equalsIgnoreCase("enabled") ? "true" : "false";
        var query = String.format("UPDATE lk_lookup SET option_value = '%s' WHERE type = 'SHIPPING_CHECK_DIGIT_ACTIVE'", value);
        databaseService.executeSql(query).block();
    }

    @And("The second verification configuration is {string}.")
    public void theSecondVerificationConfigurationIs(String value) {
        value = value.equalsIgnoreCase("enabled") ? "true" : "false";
        var query = String.format("UPDATE lk_lookup SET option_value = '%s' WHERE type = 'SHIPPING_SECOND_VERIFICATION_ACTIVE'", value);
        databaseService.executeSql(query).block();
    }

    @And("The shipment status for order {string} should be {string}.")
    public void iShouldSeeTheStatusOfTheShipmentAs(String orderNumber, String status) {
        var query = String.format("SELECT status FROM bld_shipment WHERE order_number = '%s'", orderNumber);
        var result = databaseService.fetchData(query).first().block();
        assert result != null;
        Assert.assertTrue(result.get("status").toString().equalsIgnoreCase(status));
    }
}
