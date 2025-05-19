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
import java.util.concurrent.atomic.AtomicInteger;

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
    private Map printSummaryResponseData;


    @Given("I have a {string} shipment with the Customer Code as {string} , Product Type as {string}, Carton Tare Weight as {string}, Shipment Date as {string}, Transportation Reference Number as {string}, Location Code as {string} and the unit numbers as {string} and product codes as {string} and product types {string}.")
    public void iHaveAClosedShipmentWithTheCustomerCodeAsProductTypeAsCartonTareWeightAsShipmentDateAsTransportationReferenceNumberAsLocationCodeAsAndTheUnitNumbersAsAndProductCodesAs(String shipmentStatus,String customerCode, String productType, String cartonTare
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

        createShipmentController.updateShipmentStatus(shipmentId,shipmentStatus);

    }

    @When("I request to print the shipping summary report.")
    public void iRequestToPrintTheShippingSummaryReport() {
        this.printSummaryResponse = createShipmentController.printShippingSummaryReport(
            this.shipmentId,
            sharedContext.getLocationCode()
        );

        Assert.assertNotNull(this.printSummaryResponse);
    }

    @Then("I should have the following information in the shipping summary report response:")
    public void iShouldHaveTheFollowingInformationInTheShippingSummaryReportResponse(DataTable dataTable) {
        Assert.assertNotNull(dataTable);
        Assert.assertNotNull(printSummaryResponse);

        this.printSummaryResponseData = (Map)  printSummaryResponse.get("data");

        Map<String, String> table = dataTable.asMap(String.class, String.class);

        if(table.get("Report Title") != null){
            Assert.assertEquals(printSummaryResponseData.get("reportTitle").toString(),table.get("Report Title"));
        }
        if(table.get("Header Section") != null){
            Assert.assertEquals(printSummaryResponseData.get("headerStatement").toString(),table.get("Header Section"));
        }
        if(table.get("Testing Statement") != null){
            Assert.assertEquals(printSummaryResponseData.get("testingStatement").toString(),table.get("Testing Statement"));
        }

        if(table.get("Ship To Customer Name") != null){
            Assert.assertEquals(printSummaryResponseData.get("shipToCustomerName").toString(), table.get("Ship To Customer Name"));
        }
        if(table.get("Ship To Customer Address") != null){
            Assert.assertEquals(printSummaryResponseData.get("shipToAddress").toString(), table.get("Ship To Customer Address"));
        }
        if(table.get("Ship From Facility Name") != null){
            Assert.assertEquals(printSummaryResponseData.get("shipFromBloodCenterName").toString(), table.get("Ship From Facility Name"));
        }
        if(table.get("Ship From Facility Address") != null){
            Assert.assertEquals(printSummaryResponseData.get("shipFromLocationAddress").toString(), table.get("Ship From Facility Address"));
        }
        if(table.get("Ship From Phone") != null){
            Assert.assertEquals(printSummaryResponseData.get("shipFromPhoneNumber").toString(), table.get("Ship From Phone"));
        }
        if(table.get("Shipment Details Transportation Reference Number") != null){
            Assert.assertEquals(printSummaryResponseData.get("shipmentDetailTransportationReferenceNumber").toString(), table.get("Shipment Details Transportation Reference Number"));
        }
        if(table.get("Shipment Details Shipment Number Prefix") != null){
            Assert.assertTrue(printSummaryResponseData.get("shipmentDetailShipmentNumber").toString().contains(table.get("Shipment Details Shipment Number Prefix")));
        }
        if(table.get("Shipment Closed Date/Time") != null){
            Assert.assertNotNull(printSummaryResponseData.get("closeDateTime").toString());
        }
        if(table.get("Shipment Details Product Type") != null){
            Assert.assertEquals(printSummaryResponseData.get("shipmentDetailProductType").toString(), table.get("Shipment Details Product Type"));
        }
        if(table.get("Shipment Details Product Code") != null){
            Assert.assertEquals(printSummaryResponseData.get("shipmentDetailProductCode").toString(), table.get("Shipment Details Product Code"));
        }
        if(table.get("Shipment Details Total Number of Cartons") != null){
            Assert.assertEquals(printSummaryResponseData.get("shipmentDetailTotalNumberOfCartons").toString(), table.get("Shipment Details Total Number of Cartons"));
        }
        if(table.get("Shipment Details Total Number of Products") != null){
            Assert.assertEquals(printSummaryResponseData.get("shipmentDetailTotalNumberOfProducts").toString(), table.get("Shipment Details Total Number of Products"));
        }
        if(table.get("Carton Information Carton Number Prefix") != null){
            verifyCartonInformation("cartonNumber",table.get("Carton Information Carton Number Prefix"),"contains");
        }
        if(table.get("Carton Information Product Code") != null){
            verifyCartonInformation("productCode",table.get("Carton Information Product Code"),"equals");
        }
        if(table.get("Carton Information Product Description") != null){
            verifyCartonInformation("productDescription",table.get("Carton Information Product Description"),"equals");
        }
        if(table.get("Carton Information Total Number of Products") != null){
            verifyCartonInformation("totalProducts",table.get("Carton Information Total Number of Products"),"equals");
        }
        if(table.get("Shipment Closing Details Employee Name") != null){
            Assert.assertEquals(printSummaryResponseData.get("employeeName").toString(), table.get("Shipment Closing Details Employee Name"));
        }
        if(table.get("Shipment Closing Details Date") != null){
            Assert.assertEquals(printSummaryResponseData.get("shipDate").toString(), testUtils.parseDataKeyword(table.get("Shipment Closing Details Date")));
        }
    }

    private void verifyCartonInformation(String cartonProperty, String tableValue , String verificationType){
        AtomicInteger index = new AtomicInteger(0);
        var cartonList = (List) printSummaryResponseData.get("cartonList");
        cartonList.forEach(carton -> {
            var c = (LinkedHashMap) carton;
            if("contains".equals(verificationType)){
                Assert.assertTrue(c.get(cartonProperty).toString().contains(testUtils.getCommaSeparatedList(tableValue)[index.getAndIncrement()]));
            }else if("equals".equals(verificationType)){
                log.debug("checking carton information {} with table {} index {}" ,c.get(cartonProperty).toString() , testUtils.getCommaSeparatedList(tableValue)[index.get()], index.get());
                Assert.assertEquals(c.get(cartonProperty).toString(),(testUtils.getCommaSeparatedList(tableValue)[index.getAndIncrement()]));
            }
        });
    }

    @Then("The element {string} for the property {string} {string} be visible in the shipping summary report.")
    public void theElementForThePropertyBeDisplayInTheShippingSummaryReport(String element, String elementProperty, String shouldBeNot) {
        this.printSummaryResponseData = (Map)  printSummaryResponse.get("data");
        if("should".equals(shouldBeNot)){
            Assert.assertTrue((Boolean) printSummaryResponseData.get(elementProperty));
        }else if ("should not".equals(shouldBeNot)){
            Assert.assertFalse((Boolean) printSummaryResponseData.get(elementProperty));
        }
    }

    @Then("The element {string} for the property {string} should have the value {string} in the shipping summary report.")
    public void theElementForThePropertyShouldHaveTheInTheShippingSummaryReport(String element, String elementProperty, String propertyValue) {
        this.printSummaryResponseData = (Map)  printSummaryResponse.get("data");
        Assert.assertEquals(printSummaryResponseData.get(elementProperty).toString(), propertyValue);
    }
}
