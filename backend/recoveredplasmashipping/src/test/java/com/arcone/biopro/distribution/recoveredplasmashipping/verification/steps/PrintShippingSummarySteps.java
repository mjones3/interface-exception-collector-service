package com.arcone.biopro.distribution.recoveredplasmashipping.verification.steps;

import com.arcone.biopro.distribution.recoveredplasmashipping.verification.controllers.CartonTestingController;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.controllers.CreateShipmentController;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.pages.ShipmentDetailsPage;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.DatabaseService;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.SharedContext;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.TestUtils;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
public class PrintShippingSummarySteps {

    @Autowired
    private SharedContext sharedContext;

    @Autowired
    DatabaseService databaseService;

    @Autowired
    private ShipmentDetailsPage shipmentDetailsPage;

    @Autowired
    private CreateShipmentController createShipmentController;

    @Autowired
    private TestUtils testUtils;

    private String shipmentId;

    @Autowired
    private CartonTestingController cartonTestingController;

    private Map printSummaryResponse;


    @Given("I have a closed shipment with the Customer Code as {string} , Product Type as {string}, Carton Tare Weight as {string}, Shipment Date as {string}, Transportation Reference Number as {string}, Location Code as {string} and the unit numbers as {string} and product codes as {string} and product types {string}.")
    public void iHaveAClosedShipmentWithTheCustomerCodeAsProductTypeAsCartonTareWeightAsShipmentDateAsTransportationReferenceNumberAsLocationCodeAsAndTheUnitNumbersAsAndProductCodesAs(String customerCode, String productType, String cartonTare
        , String shipmentDate, String transportationRefNumber, String locationCode, String unitNumbers, String productCodes , String productTypes) {


        createShipmentController.createShipment(customerCode, productType, Float.parseFloat(cartonTare), testUtils.parseDataKeyword(shipmentDate), transportationRefNumber, locationCode);

        Assertions.assertNotNull(sharedContext.getShipmentCreateResponse());

        this.shipmentId = sharedContext.getShipmentCreateResponse().get("id").toString();

        String[] unitNumbersArray = testUtils.getCommaSeparatedList(unitNumbers);
        String[] productCodesArray = testUtils.getCommaSeparatedList(productCodes);
        String[] productTypesArray = testUtils.getCommaSeparatedList(productTypes);

        for (int i = 0; i < unitNumbersArray.length; i++) {

            var cartonCreated = cartonTestingController.createCarton(this.shipmentId);
            Assertions.assertNotNull(cartonCreated);
            Map data = (Map) cartonCreated.get("data");

            var cartonId = data.get("id").toString();

            cartonTestingController.insertVerifiedProduct(cartonId, unitNumbersArray[i], productCodesArray[i], productTypesArray[i]);
            cartonTestingController.updateCartonStatus(cartonId,"CLOSED");
        }

        createShipmentController.updateShipmentStatus(shipmentId,"CLOSED");

    }

    @When("I request to print the shipping summary report.")
    public void iRequestToPrintTheShippingSummaryReport() {
        this.printSummaryResponse = createShipmentController.printShippingSummaryReport(
            this.shipmentId,
            sharedContext.getLocationCode()
        );

        Assert.assertNotNull(sharedContext.getLastShippingSummaryReportResponse());
    }

    @Then("I should have the following information in the shipping summary report response:")
    public void iShouldHaveTheFollowingInformationInTheShippingSummaryReportResponse(DataTable dataTable) {
        Assert.assertNotNull(dataTable);
        Assert.assertNotNull(printSummaryResponse);

        Map<String, String> table = dataTable.asMap(String.class, String.class);

        if(table.get("Report Title") != null){
            Assert.assertEquals(printSummaryResponse.get("reportTitle").toString(),table.get("Report Title"));
        }
        if(table.get("Header Section") != null){
            Assert.assertEquals(printSummaryResponse.get("headerStatement").toString(),table.get("Header Section"));
        }
        if(table.get("Testing Statement") != null){
            Assert.assertEquals(printSummaryResponse.get("testingStatement").toString(),table.get("Testing Statement"));
        }

                /*| Ship To	Customer Name                            |                                                                                                                                        |
                | Ship To	Customer Address                         |                                                                                                                                        |
                | Ship From	Facility Name                          |                                                                                                                                        |
                | Ship From	Facility Address                       |                                                                                                                                        |
                | Shipment Details	Transportation Reference Number |                                                                                                                                        |
                | Shipment Details	Shipment Number                 |                                                                                                                                        |
                | Shipment Details	Shipment Date/Time              |                                                                                                                                        |
                | Shipment Details	Product Type                    |                                                                                                                                        |
                | Shipment Details	Product Code                    |                                                                                                                                        |
                | Shipment Details	Total Number of Cartons         |                                                                                                                                        |
                | Shipment Details	Total Number of Products        |                                                                                                                                        |
                | Carton Information	Carton Number                 |                                                                                                                                        |
                | Carton Information	Product Code                  |                                                                                                                                        |
                | Carton Information	Product Description           |                                                                                                                                        |
                | Carton Information	Total Number of Products      |                                                                                                                                        |
                | Testing Statement                                | All products in this shipment meet FDA and ARC-One Solutions testing requirements. These units are acceptable for further manufacture. |
                | Shipment Closing Details	Employee Name           |                                                                                                                                        |
                | Shipment Closing Details	Date                    |                                                                                                                                        |
        */

        if(table.get("Ship To Customer Name") != null){
            Assert.assertEquals(printSummaryResponse.get("shipToCustomerName").toString(), table.get("Ship To Customer Name"));
        }
        if(table.get("Ship To Customer Address") != null){
            Assert.assertEquals(printSummaryResponse.get("shipToAddress").toString(), table.get("Ship To Customer Address"));
        }
        if(table.get("Ship From Facility Name") != null){
            Assert.assertEquals(printSummaryResponse.get("shipFromBloodCenterName").toString(), table.get("Ship From Facility Name"));
        }
        if(table.get("Ship From Facility Address") != null){
            Assert.assertEquals(printSummaryResponse.get("shipFromLocationAddress").toString(), table.get("Ship From Facility Address"));
        }
        if(table.get("Shipment Details Transportation Reference Number") != null){
            Assert.assertEquals(printSummaryResponse.get("shipmentDetailTransportationReferenceNumber").toString(), table.get("Shipment Details Transportation Reference Number"));
        }
        if(table.get("Shipment Details Shipment Number Prefix") != null){
            Assert.assertTrue(printSummaryResponse.get("shipmentDetailShipmentNumber").toString().contains(table.get("Shipment Details Shipment Number Prefix")));
        }
        if(table.get("Shipment Details Shipment Date/Time") != null){
            Assert.assertEquals(printSummaryResponse.get("shipDate").toString(), table.get("Shipment Details Shipment Date/Time"));
        }
        if(table.get("Shipment Details Product Type") != null){
            Assert.assertEquals(printSummaryResponse.get("shipmentDetailProductType").toString(), table.get("Shipment Details Product Type"));
        }
        if(table.get("Shipment Details Product Code") != null){
            Assert.assertEquals(printSummaryResponse.get("shipmentDetailProductCode").toString(), table.get("Shipment Details Product Code"));
        }
        if(table.get("Shipment Details Total Number of Cartons") != null){
            Assert.assertEquals(printSummaryResponse.get("shipmentDetailTotalNumberOfCartons").toString(), table.get("Shipment Details Total Number of Cartons"));
        }
        if(table.get("Shipment Details Total Number of Products") != null){
            Assert.assertEquals(printSummaryResponse.get("shipmentDetailTotalNumberOfProducts").toString(), table.get("Shipment Details Total Number of Products"));
        }
        if(table.get("Carton Information Carton Number Prefix") != null){
            AtomicReference<Integer> index = new AtomicReference<>(0);
            var cartonList = (List) printSummaryResponse.get("cartonList");
            cartonList.forEach(carton -> {
                    var c = (LinkedHashMap) carton;
                    Assert.assertTrue(c.get("cartonNumber").toString().contains(testUtils.getCommaSeparatedList(table.get("Carton Number Prefix"))[index.get()]));
            });
        }
        if(table.get("Carton Information Product Code") != null){
            Assert.assertEquals(getCartonInformationSplitByComma("productCode"), table.get("Carton Information Product Code"));
        }
        if(table.get("Carton Information Product Description") != null){
            Assert.assertEquals(getCartonInformationSplitByComma("productDescription"), table.get("Carton Information Product Code"));
        }
        if(table.get("Carton Information Total Number of Products") != null){
            Assert.assertEquals(getCartonInformationSplitByComma("totalProducts"), table.get("Carton Information Product Code"));
        }
        if(table.get("Shipment Closing Details Employee Name") != null){
            Assert.assertEquals(printSummaryResponse.get("employeeName").toString(), table.get("Shipment Closing Details Employee Name"));
        }

    }


    private String getCartonInformationSplitByComma(String property){
        var cartonList = (List) printSummaryResponse.get("cartonList");
        return cartonList.stream().map(s -> ((LinkedHashMap) s ).get(property).toString()).sorted().collect(Collectors.joining(",")).toString();
    }
}
