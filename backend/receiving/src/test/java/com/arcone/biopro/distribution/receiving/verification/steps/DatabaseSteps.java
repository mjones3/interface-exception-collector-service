package com.arcone.biopro.distribution.receiving.verification.steps;

import com.arcone.biopro.distribution.receiving.verification.support.DatabaseQueries;
import com.arcone.biopro.distribution.receiving.verification.support.DatabaseService;
import com.arcone.biopro.distribution.receiving.verification.support.SharedContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

public class DatabaseSteps {
    @Autowired
    DatabaseService db;
    @Autowired
    SharedContext sharedContext;

    @Given("I have removed all created devices which ID contains {string}.")
    public void removeDeviceByIdContains(String key) {
        db.executeSql(DatabaseQueries.DELETE_DEVICE_BY_ID_LIKE(key)).block();
    }

    @Given("The following temperature thresholds are configured:")
    public void theFollowingTemperatureThresholdsAreConfigured(DataTable dataTable) {

        var headers = dataTable.row(0);
        for (int i = 1; i < dataTable.height(); i++) {
            var row = dataTable.row(i);
            String sql = DatabaseQueries.UPDATE_TEMPERATURE_ACCEPTABLE_CONFIG(row.get(headers.indexOf("Temperature Category")), row.get(headers.indexOf("Min Temperature")), row.get(headers.indexOf("Max Temperature")));

            db.executeSql(sql).block();
        }
    }

    @Given("The following transit time thresholds are configured:")
    public void theFollowingTransitTimeThresholdsAreConfigured(DataTable dataTable) {
        var headers = dataTable.row(0);
        for (int i = 1; i < dataTable.height(); i++) {
            var row = dataTable.row(i);
            String sql = DatabaseQueries.UPDATE_TRANSIT_TIME_ACCEPTABLE_CONFIG(row.get(headers.indexOf("Temperature Category")), row.get(headers.indexOf("Min Transit Time")), row.get(headers.indexOf("Max Transit Time")));

            db.executeSql(sql).block();
        }
    }

    @And("The location default timezone is configured as {string}")
    public void theLocationDefaultTimezoneIsConfiguredAs(String tz) {
        String sql = DatabaseQueries.UPDATE_LOCATION_TZ(sharedContext.getLocationCode(), tz);
        db.executeSql(sql).block();
    }

    @And("I have removed all imports using thermometer which code contains {string}.")
    public void iHaveRemovedAllImportsUsingThermometerWhichCodeContains(String thermometerCode) {
        String removeImportItemPropertySql = DatabaseQueries.DELETE_IMPORT_ITEM_PROPERTY_BY_THERMOMETER_CODE_LIKE(thermometerCode);
        String removeImportItemConsequenceSql = DatabaseQueries.DELETE_IMPORT_ITEM_CONSEQUENCE_BY_THERMOMETER_CODE_LIKE(thermometerCode);
        String removeImportItemSql = DatabaseQueries.DELETE_IMPORT_ITEM_BY_THERMOMETER_CODE_LIKE(thermometerCode);
        String removeImportSql = DatabaseQueries.DELETE_IMPORT_BY_THERMOMETER_CODE_LIKE(thermometerCode);

        db.executeSql(removeImportItemConsequenceSql).block();
        db.executeSql(removeImportItemPropertySql).block();
        db.executeSql(removeImportItemSql).block();
        db.executeSql(removeImportSql).block();
    }

    @And("The status of the import batch is {string}")
    public void theStatusOfTheImportBatchIs(String importStatus) {
        String sql = DatabaseQueries.UPDATE_IMPORT_STATUS_BY_ID( sharedContext.getCreateImportResponse().get("id").toString(), importStatus);
        db.executeSql(sql).block();
    }

    @And("The products with unit number {string} and product codes {string} should not be imported.")
    public void theProductsWithUnitNumberAndProductCodesShouldNotBeImported(String unitNumber, String productCode) {
        String sql = DatabaseQueries.COUNT_IMPORTED_PRODUCT_BY_UNIT_NUMBER_PRODUCT_CODE(unitNumber,productCode);
        var result = db.fetchData(sql).first().block();
        assert result != null;
        Assert.assertEquals(result.get("total").toString(),"0");
    }

    @And("I have removed all internal transfers which order number contains {string}")
    public void iHaveRemovedAllInternalTransfersWhichOrderNumberContains(String order_numbers) {

        String removeInternalItemsSql = DatabaseQueries.DELETE_INTERNAL_TRANSFER_ITEM_BY_ORDER_NUMBER_IN(order_numbers);
        String removeInternalTransferSql = DatabaseQueries.DELETE_INTERNAL_TRANSFER_BY_ORDER_NUMBER_IN(order_numbers);

        db.executeSql(removeInternalItemsSql).block();
        db.executeSql(removeInternalTransferSql).block();
    }
}
