package com.arcone.biopro.distribution.shipping.verification.steps;

import com.arcone.biopro.distribution.shipping.verification.support.DatabaseService;
import com.arcone.biopro.distribution.shipping.verification.support.SharedContext;
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

    @Autowired
    private SharedContext context;

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
                        delete from bld_shipment_item_removed where shipment_id in (select id from bld_shipment where order_number in (%s));
            """, orderNumber);
        databaseService.executeSql(query3).block();

        var query4 = String.format("""
                        delete from bld_shipment where order_number in (%s);
            """, orderNumber);
        databaseService.executeSql(query4).block();
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

    @And("The shipment status is {string}.")
    public void theShipmentStatusIs(String status) {
        var query = "UPDATE bld_shipment SET status = '" + status + "' WHERE ID = " + context.getShipmentId();
        databaseService.executeSql(query).block();
    }

    @Given("I cleaned up from the database the product locations history that used the unit number {string}.")
    public void cleanProductLocationHistory(String unitNumber) {
        unitNumber = unitNumber.replace(",", "','");
        var query = String.format("DELETE FROM bld_product_location_history WHERE unit_number in ('%s')", unitNumber);
        databaseService.executeSql(query).block();
    }

    @Given("I cleaned up from the database the external transfer information that used the customer code {string}.")
    public void cleanExternalTransfer(String customerCode) {
        customerCode = customerCode.replace(",", "','");

        var query = String.format("DELETE FROM bld_external_transfer_item WHERE  external_transfer_id in (select id from bld_external_transfer where customer_code_to in ('%s') )", customerCode);
        databaseService.executeSql(query).block();

        var queryParent = String.format("DELETE FROM bld_external_transfer WHERE customer_code_to in ('%s')", customerCode);
        databaseService.executeSql(queryParent).block();

    }

    @And("The data entered must not be saved after canceling the process.")
    public void theDataEnteredMustNotBeSavedAfterCancelingTheProcess() {
        var query = String.format("SELECT id FROM bld_external_transfer WHERE id = %s", context.getExternalTransferId());
        var result = databaseService.executeSql(query).block();
        assert result == null;
    }
}
