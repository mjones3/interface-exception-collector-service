package com.arcone.biopro.distribution.recoveredplasmashipping.verification.steps;

import com.arcone.biopro.distribution.recoveredplasmashipping.verification.controllers.CartonTestingController;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.pages.ShipmentDetailsPage;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.DatabaseQueries;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.DatabaseService;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.SharedContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class CartonPackingSlipSteps {

    @Autowired
    private CartonTestingController cartonTestingController;

    @Autowired
    private SharedContext sharedContext;

    @Autowired
    DatabaseService databaseService;

    @Autowired
    private ShipmentDetailsPage shipmentDetailsPage;

    @Value("${selenium.headless.execution}")
    private boolean headless;


    @When("I request to print the carton packing slip.")
    public void iRequestToPrintCartonPackingSlip() {

        cartonTestingController.printCartonPackingSlip(
            sharedContext.getCreateCartonResponseList().getFirst().get("id").toString(),
            sharedContext.getLocationCode()
        );

        Assert.assertNotNull(sharedContext.getLastCartonPackingSlipResponse());

    }

    @Then("The carton packing slip should contain:")
    public void theCartonPackingSlipShouldContain(DataTable dataTable) {
        Assert.assertNotNull(dataTable);
        var headers = dataTable.row(0);
        for (var i = 1; i < dataTable.height(); i++) {
            var row = dataTable.row(i);
            log.debug("Checking property {}",row.get(headers.indexOf("Information Type")));

            if(row.get(headers.indexOf("Information Type")).contains("Prefix")){
                Assert.assertTrue(sharedContext.getLastCartonPackingSlipResponse().get(row.get(headers.indexOf("Response property"))).toString().contains(row.get(headers.indexOf("Information Value"))));
            }else if("products".equals(row.get(headers.indexOf("Response property")))){

                var products = (List) sharedContext.getLastCartonPackingSlipResponse().get(row.get(headers.indexOf("Response property")));

                if("Unit Numbers".equals(row.get(headers.indexOf("Information Type")))){
                    Assert.assertEquals(products.stream().map(s -> ((LinkedHashMap) s ).get("unitNumber")).collect(Collectors.joining(",")),row.get(headers.indexOf("Information Value")));
                } else if("Product Volume".equals(row.get(headers.indexOf("Information Type")))){
                    Assert.assertEquals(products.stream().map(s -> ((LinkedHashMap) s ).get("volume").toString()).collect(Collectors.joining(",")),row.get(headers.indexOf("Information Value")));
                }else if("Collection Date".equals(row.get(headers.indexOf("Information Type")))){
                    Assert.assertEquals(products.stream().map(s -> ((LinkedHashMap) s ).get("collectionDate")).collect(Collectors.joining(",")),row.get(headers.indexOf("Information Value")));
                }

            }else{
                Assert.assertEquals(sharedContext.getLastCartonPackingSlipResponse().get(row.get(headers.indexOf("Response property"))).toString(),row.get(headers.indexOf("Information Value")));
            }

        }

    }

    @Then("The element {string} for the property {string} {string} be display.")
    public void theElementForThePropertyBeDisplay(String element, String elementProperty, String shouldBeNot) {
        if("should".equals(shouldBeNot)){
            Assert.assertTrue((Boolean) sharedContext.getLastCartonPackingSlipResponse().get(elementProperty));
        }else if ("should not".equals(shouldBeNot)){
            Assert.assertFalse((Boolean) sharedContext.getLastCartonPackingSlipResponse().get(elementProperty));
        }
    }

    @And("I have reset the carton packing slip system configurations following values:")
    public void iHaveResetTheCartonPackingSlipSystemConfigurationsToHaveTheFollowingValues(DataTable dataTable) {
        var headers = dataTable.row(0);
        for (var i = 1; i < dataTable.height(); i++) {
            var row = dataTable.row(i);
            databaseService.executeSql(DatabaseQueries.UPDATE_SYSTEM_CONFIGURATION(row.get(headers.indexOf("process_type"))
                , row.get(headers.indexOf("system_configuration_key")), row.get(headers.indexOf("system_configuration_value")))).block();


        }
    }

    @And("I should be able to see the Carton Packing Slip details.")
    public void iShouldBeAbleToSeeTheCartonPackingSlipDetails() throws InterruptedException {
        if (!headless) {
            shipmentDetailsPage.viewCartonPackingSlip();
        } else {
            log.info("Skipping print packing slip. Test in headless mode.");
        }
    }

}
