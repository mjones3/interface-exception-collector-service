package com.arcone.biopro.distribution.receiving.verification.steps;

import com.arcone.biopro.distribution.receiving.verification.support.DatabaseQueries;
import com.arcone.biopro.distribution.receiving.verification.support.DatabaseService;
import com.arcone.biopro.distribution.receiving.verification.support.SharedContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
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
}
