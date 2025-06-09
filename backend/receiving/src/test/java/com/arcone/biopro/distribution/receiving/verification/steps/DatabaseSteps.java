package com.arcone.biopro.distribution.receiving.verification.steps;

import com.arcone.biopro.distribution.receiving.verification.support.DatabaseQueries;
import com.arcone.biopro.distribution.receiving.verification.support.DatabaseService;
import io.cucumber.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;

public class DatabaseSteps {
    @Autowired
    DatabaseService db;

    @Given("I have removed all created devices which ID contains {string}.")
    public void removeDeviceByIdContains(String key) {
        db.executeSql(DatabaseQueries.DELETE_DEVICE_BY_ID_LIKE(key)).block();
    }
}
