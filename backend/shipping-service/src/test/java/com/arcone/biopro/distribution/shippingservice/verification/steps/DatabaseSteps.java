package com.arcone.biopro.distribution.shippingservice.verification.steps;

import com.arcone.biopro.distribution.shippingservice.verification.support.DatabaseService;
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
        var query = String.format("DELETE FROM bld_shipment_item_packed WHERE unit_number = '%s'", unitNumber);
        databaseService.executeSql(query).block();
    }
}
