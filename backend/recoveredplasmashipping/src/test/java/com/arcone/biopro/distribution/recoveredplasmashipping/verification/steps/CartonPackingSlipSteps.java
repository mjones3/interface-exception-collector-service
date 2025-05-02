package com.arcone.biopro.distribution.recoveredplasmashipping.verification.steps;

import com.arcone.biopro.distribution.recoveredplasmashipping.verification.controllers.CartonTestingController;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.SharedContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class CartonPackingSlipSteps {

    @Autowired
    private CartonTestingController cartonTestingController;

    @Autowired
    private SharedContext sharedContext;


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

        //| Response property       | Information Type                 | Information Value                                             |
        //Response: {data={cartonId=3, cartonNumber=BPMMH13, cartonSequence=1, totalProducts=2, dateTimePacked=05/02/2025 18:04, packedByEmployeeId=5db1da0b-6392-45ff-86d0-17265ea33226, cartonProductCode=E2488V00
        // , cartonProductDescription=LIQ CPD PLS MNI RT, testingStatement=Testing statement template: 5db1da0b-6392-45ff-86d0-17265ea33226
        // , shipFromBloodCenterName=ARC-One Solutions, shipFromLicenseNumber=2222
        // , shipFromLocationAddress=444 Main St. Charlotte, NC, {zipCode} USA
        // , shipToAddress=4801 Woodlane Circle Tallahassee, FL, 32303 USA, shipToCustomerName=Southern Biologics, shipmentNumber=BPM27653
        // , shipmentProductType=RP_NONINJECTABLE_LIQUID_RT, shipmentProductDescription=RP NONINJECTABLE LIQUID RT, shipmentTransportationReferenceNumber=DIS-343, displaySignature=true
        // , displayTransportationReferenceNumber=true, displayTestingStatement=true, displayLicenceNumber=true, products=[{unitNumber=W036898786808, volume=259, collectionDate=12/03/2011}
        // , {unitNumber=W036898786809, volume=259, collectionDate=12/03/2011}]}, notifications=[{message=Carton Packing Slip generated successfully, type=SUCCESS, code=13
        // , reason=null, action=null, details=null}], _links=null}

        //Expected :W03689878680[8,W036898786809]
        //Actual   :W03689878680[600,W03689878680700]


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
}
