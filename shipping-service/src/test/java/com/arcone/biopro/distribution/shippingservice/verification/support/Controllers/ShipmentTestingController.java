package com.arcone.biopro.distribution.shippingservice.verification.support.Controllers;

import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.BloodType;
import com.arcone.biopro.distribution.shippingservice.verification.support.*;
import com.arcone.biopro.distribution.shippingservice.verification.support.Types.ListShipmentsResponseType;
import com.arcone.biopro.distribution.shippingservice.verification.support.Types.ShipmentFulfillmentRequest;
import com.arcone.biopro.distribution.shippingservice.verification.support.Types.ShipmentItemShortDateResponseType;
import com.arcone.biopro.distribution.shippingservice.verification.support.Types.ShipmentRequestDetailsResponseType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;


@Component
@Slf4j
public class ShipmentTestingController {

    @Autowired
    protected TestUtils utils;

    @Autowired
    protected ApiHelper apiHelper;

    @Autowired
    ObjectMapper objectMapper;


    public long createShippingRequest(ShipmentRequestDetailsResponseType shipmentDetail) throws Exception {

        utils.kafkaSender(objectMapper.writeValueAsString(shipmentDetail), Topics.ORDER_FULFILLED);
        // Add sleep to wait for the message to be consumed.
        Thread.sleep(2000);

        log.info("Message sent to create the order: {}", shipmentDetail.getOrderNumber());
        return shipmentDetail.getOrderNumber();
    }

    public long createShippingRequest() throws Exception {
        long orderId = new Random().nextInt(10000);
        var resource = utils.getResource("order-fulfilled.json")
            .replace("{order.number}", String.valueOf(orderId));

        utils.kafkaSender(resource, Topics.ORDER_FULFILLED);
        // Add sleep to wait for the message to be consumed.
        Thread.sleep(2000);

        log.info("Message sent to create the order: {}", orderId);
        return orderId;
    }

    public EntityExchangeResult<String> listShipments() {
        log.info("Listing orders.");
        return apiHelper.getRequest(Endpoints.LIST_SHIPMENTS);
    }

    public EntityExchangeResult<String> getShipmentRequestDetails(long shipmentId) {
        var endpoint = Endpoints.GET_SHIPMENT.replace("{shipment.id}", String.valueOf(shipmentId));
        log.info("Getting order details for order: {}", shipmentId);
        return apiHelper.getRequest(endpoint);
    }

    public List<ListShipmentsResponseType> parseShipmentList(EntityExchangeResult<String> result) throws Exception {
        var object = List.of(objectMapper.readValue(result.getResponseBody(), ListShipmentsResponseType[].class));
        log.debug("Order list: {}", object);
        return object;
    }

    public ShipmentRequestDetailsResponseType parseShipmentRequestDetail(EntityExchangeResult<String> result) throws Exception {
        var object = objectMapper.readValue(result.getResponseBody(), ShipmentRequestDetailsResponseType.class);
        log.debug("Order details: {}", object);
        return object;
    }

    public ShipmentRequestDetailsResponseType buildShipmentRequestDetailsResponseType(Long orderNumber,
                                                                                      String priority,
                                                                                      String status,
                                                                                      Long shippingCustomerCode,
                                                                                      Long billingCustomerCode,
                                                                                      Long locationCode,
                                                                                      String deliveryType,
                                                                                      String shippingMethod,
                                                                                      String productCategory,
                                                                                      LocalDate shippingDate,
                                                                                      String shippingCustomerName,
                                                                                      String billingCustomerName,
                                                                                      String customerPhoneNumber,
                                                                                      String customerAddressState,
                                                                                      String customerAddressPostalCode,
                                                                                      String customerAddressCountry,
                                                                                      String customerAddressCountryCode,
                                                                                      String customerAddressCity,
                                                                                      String customerAddressDistrict,
                                                                                      String customerAddressAddressLine1,
                                                                                      String customerAddressAddressLine2,
                                                                                      String quantities ,
                                                                                      String bloodTypes ,
                                                                                      String productFamilies,
                                                                                      String unitNumbers,
                                                                                      String productCodes) {

        Assert.assertNotNull(quantities);
        Assert.assertNotNull(bloodTypes);
        Assert.assertNotNull(productFamilies);

        List<String> quantityList = Arrays.stream(quantities.split(",")).toList();
        List<String> bloodTypeList = Arrays.stream(bloodTypes.split(",")).toList();
        List<String> familyList = Arrays.stream(productFamilies.split(",")).toList();
        List<String> unitNumberList =  unitNumbers != null && !unitNumbers.isEmpty() ? Arrays.stream(unitNumbers.split(",")).toList() : Collections.emptyList();
        List<String> productCodeList =  productCodes != null && !productCodes.isEmpty() ? Arrays.stream(productCodes.split(",")).toList() : Collections.emptyList();

        var shipmentDetailType = ShipmentRequestDetailsResponseType.builder()
            .orderNumber(Long.valueOf(orderNumber))
            .priority(priority)
            .status(status)
            .locationCode(locationCode)
            .shippingMethod(shippingMethod)
            .productCategory(productCategory)
            .shippingDate(shippingDate)
            .customerPhoneNumber(customerPhoneNumber)
            .customerAddressState(customerAddressState)
            .customerAddressPostalCode(customerAddressPostalCode)
            .customerAddressCountry(customerAddressCountry)
            .customerAddressCountryCode(customerAddressCountryCode)
            .customerAddressCity(customerAddressCity)
            .customerAddressDistrict(customerAddressDistrict)
            .customerAddressAddressLine1(customerAddressAddressLine1)
            .customerAddressAddressLine2(customerAddressAddressLine2)
            .billingCustomerCode(billingCustomerCode)
            .billingCustomerName(billingCustomerName)
            .deliveryType(deliveryType)
            .createDate(ZonedDateTime.now())
            .shippingCustomerCode(shippingCustomerCode)
            .shippingCustomerName(shippingCustomerName)
            .items(new ArrayList<>())
            .build();
        if (quantityList != null && !quantityList.isEmpty()) {
            for (int i = 0; i < quantityList.size(); i++) {

                shipmentDetailType.getItems().add(ShipmentFulfillmentRequest
                    .builder()
                    .quantity(Integer.valueOf(quantityList.get(i)))
                    .bloodType(BloodType.valueOf(bloodTypeList.get(i)))
                    .productFamily(familyList.get(i))
                    .shortDateProducts(new ArrayList<>())
                    .build());
            }
        }

        if(unitNumberList != null && !unitNumberList.isEmpty()){
            for (int u = 0; u < unitNumberList.size(); u++){
                shipmentDetailType.getItems().get(u).getShortDateProducts().add(ShipmentItemShortDateResponseType
                    .builder()
                    .unitNumber(unitNumberList.get(u))
                    .productCode(productCodeList.get(u))
                    .build());
            }
        }

        return shipmentDetailType;
    }
}
