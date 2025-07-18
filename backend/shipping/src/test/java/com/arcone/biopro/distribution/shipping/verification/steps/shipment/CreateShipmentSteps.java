package com.arcone.biopro.distribution.shipping.verification.steps.shipment;

import com.arcone.biopro.distribution.shipping.verification.support.SharedContext;
import com.arcone.biopro.distribution.shipping.verification.support.TestUtils;
import com.arcone.biopro.distribution.shipping.verification.support.controllers.ShipmentTestingController;
import io.cucumber.java.en.Given;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@Slf4j
@SpringBootTest
public class CreateShipmentSteps {

    @Autowired
    private SharedContext context;

    @Autowired
    private ShipmentTestingController shipmentTestingController;

    @Given("I have a shipment for order {string} with the unit {string} and product code {string} {string}.")
    public void createPackedShipment(String orderNumber, String unitNumber, String productCode, String itemStatus){
        createPackedShipment(orderNumber,unitNumber,productCode,itemStatus,"PLASMA_TRANSFUSABLE","B");
    }

    @Given("I have a shipment for order {string} with the unit {string} and product code {string} {string} into the line item {string} and Blood Type {string}.")
    public void createPackedShipment(String orderNumber, String unitNumber, String productCode, String itemStatus,String productFamily , String bloodType ){
        context.setUnitNumber(unitNumber);
        context.setProductCode(productCode);
        context.setShipmentId(shipmentTestingController.createPackedShipment(orderNumber, List.of(unitNumber), List.of(productCode), itemStatus, productFamily, bloodType,List.of(unitNumber).size()));

        Assert.assertNotNull(context.getShipmentId());
        context.setTotalPacked(1);
    }

    @Given("I have a shipment for order {string} with the units {string} and product codes {string} {string}.")
    public void createPackedShipmentMultipleUnits(String orderNumber, String unitNumbers, String productCodes, String itemStatus){
        int countUnits = TestUtils.getCommaSeparatedList(unitNumbers).length;
        createPackedShipmentMultipleUnits(orderNumber,unitNumbers,productCodes,"PLASMA_TRANSFUSABLE","B",itemStatus,countUnits);
    }

    @Given("I have a shipment for order {string} with the units {string} and product codes {string} of family {string} and blood type {string} {string}, out of {int} requested.")
    public void createPackedShipmentMultipleUnits(String orderNumber, String unitNumbers, String productCodes, String productFamily, String bloodType, String itemStatus, Integer totalRequested){
        // itemStatus can be "packed", "verified", or "unsuitable-verified"

        var units = Arrays.stream(unitNumbers.split(",")).toList();
        var productCodeList = Arrays.stream(productCodes.split(",")).toList();

        context.setUnitNumber(units.getFirst());
        context.setProductCode(productCodeList.getFirst());
        context.setOrderNumber(Long.valueOf(orderNumber));
        context.setShipmentId(shipmentTestingController.createPackedShipment(orderNumber,units,productCodeList, itemStatus,productFamily,bloodType, totalRequested));

        Assert.assertNotNull(context.getShipmentId());
        context.setTotalPacked(units.size());

        if (itemStatus.equalsIgnoreCase("verified")){
            context.setTotalVerified(units.size());
        }
    }

    @Given("The shipment details are order Number {string}, customer ID {string}, Customer Name {string}, Product Details: Quantities {int}, Blood Types: {string}, Product Families {string}, Temperature Category as {string}, Shipment Type defined as {string}, Label Status as {string} and Quarantined Products as {string} with the units {string} and product codes {string} {string}")
    public void theShipmentDetailsAreOrdeNumberCustomerIDCustomerNameProductDetailsQuantitiesBloodTypesProductFamiliesTemperatureCategoryAsShipmentTypeDefinedAsLabelStatusAsAndQuarantinedProductsAsWithTheUnitsAndProductCodes(
        String orderNumber, String customerId, String customerName
        , int totalRequested, String bloodTypes, String productFamilies,
        String temperatureCategory, String shipmentType,
        String labelStatus, String quarantinedProducts, String unitNumbers , String productCodes , String shipmentItemStatus){



        var units = Arrays.stream(unitNumbers.split(",")).toList();
        var productCodeList = Arrays.stream(productCodes.split(",")).toList();

        context.setUnitNumber(units.getFirst());
        context.setProductCode(productCodeList.getFirst());
        context.setOrderNumber(Long.valueOf(orderNumber));

        context.setShipmentId(shipmentTestingController.createPackedShipment(orderNumber,customerId,customerName,temperatureCategory,shipmentType,labelStatus
            ,Boolean.parseBoolean(quarantinedProducts)
            ,units
            ,productCodeList
            ,shipmentItemStatus
            ,productFamilies
            ,bloodTypes
            ,totalRequested));

        Assert.assertNotNull(context.getShipmentId());
        context.setTotalPacked(units.size());
        if (shipmentItemStatus.equalsIgnoreCase("verified")){
            context.setTotalVerified(units.size());
        }


    }
}
