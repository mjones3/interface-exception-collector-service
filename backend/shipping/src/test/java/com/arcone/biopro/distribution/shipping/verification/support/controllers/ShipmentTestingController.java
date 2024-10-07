package com.arcone.biopro.distribution.shipping.verification.support.controllers;

import com.arcone.biopro.distribution.shipping.domain.model.enumeration.BloodType;
import com.arcone.biopro.distribution.shipping.verification.support.*;
import com.arcone.biopro.distribution.shipping.verification.support.types.ListShipmentsResponseType;
import com.arcone.biopro.distribution.shipping.verification.support.types.OrderFulfilledEventType;
import com.arcone.biopro.distribution.shipping.verification.support.types.ShipmentFulfillmentRequest;
import com.arcone.biopro.distribution.shipping.verification.support.types.ShipmentItemShortDateResponseType;
import com.arcone.biopro.distribution.shipping.verification.support.types.ShipmentRequestDetailsResponseType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;


@Component
@Slf4j
public class ShipmentTestingController {

    @Autowired
    protected TestUtils utils;

    @Autowired
    protected ApiHelper apiHelper;

    @Autowired
    protected KafkaHelper kafkaHelper;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${kafka.waiting.time}")
    private long kafkaWaitingTime;
    @Autowired
    private DatabaseService databaseService;


    public long createShippingRequest(ShipmentRequestDetailsResponseType shipmentDetail) throws Exception {

        var fulfilledMessage = OrderFulfilledEventType.
            builder()
            .eventId(UUID.randomUUID())
            .occurredOn(Instant.now())
            .eventVersion("1.0")
            .eventType("OrderFulfilled")
            .payload(shipmentDetail)
            .build();
        kafkaHelper.sendEvent(fulfilledMessage.eventId().toString(), fulfilledMessage, Topics.ORDER_FULFILLED).block();
        // Add sleep to wait for the message to be consumed.
        Thread.sleep(kafkaWaitingTime);

        log.info("Message sent to create the order: {}", shipmentDetail.getOrderNumber());
        return shipmentDetail.getOrderNumber();
    }

    public long createShippingRequest() throws Exception {
        long orderId = new Random().nextInt(10000);
        var resource = utils.getResource("order-fulfilled.json")
            .replace("{order.number}", String.valueOf(orderId));

        kafkaHelper.sendEvent(UUID.randomUUID().toString(), objectMapper.readValue(resource, OrderFulfilledEventType.class), Topics.ORDER_FULFILLED).block();
        // Add sleep to wait for the message to be consumed.
        Thread.sleep(3000);

        log.info("Message sent to create the order: {}", orderId);
        return orderId;
    }

    public long getOrderShipmentId(long orderId) {
        var orders = this.listShipments();
        // Find the last order in the list with the same order number.
        var orderFilter = orders.stream().filter(x -> x.getOrderNumber().equals(orderId)).reduce((first, second) -> second).orElse(null);
        if (orderFilter != null) {
            var shipmentId = orderFilter.getId();
            log.info("Found Shipment by Order Number");
            return shipmentId;
        } else{
            throw new RuntimeException("Shipments not found.");
        }
    }

    public List<ListShipmentsResponseType> listShipments() {
        log.info("Listing orders.");
        var response = apiHelper.graphQlRequestObjectList(GraphQLQueryMapper.listShipmentsQuery(),"listShipments");
        List<ListShipmentsResponseType> responseTypeList = new ArrayList<>();
        Arrays.stream(response).toList().forEach(o -> {
            LinkedHashMap linkedHashMap = (LinkedHashMap) o;
            responseTypeList.add(ListShipmentsResponseType.
                builder()
                    .id(Long.valueOf((String) linkedHashMap.get("id")))
                    .status((String) linkedHashMap.get("status"))
                    .orderNumber(Long.valueOf((Integer) linkedHashMap.get("orderNumber")))
                    .priority((String) linkedHashMap.get("priority"))
                .build());

        });

        return responseTypeList;
    }

    public Map getShipmentRequestDetails(long shipmentId) {
        log.info("Getting order details for order: {}", shipmentId);
        return apiHelper.graphQlRequest(GraphQLQueryMapper.shipmentDetailsQuery(shipmentId),"getShipmentDetailsById");
    }

    public ShipmentRequestDetailsResponseType parseShipmentRequestDetail(Map result) {
        log.debug("Order details: {}", result);

        List<ShipmentFulfillmentRequest> lineItems = new ArrayList<>();
        if (result.get("items") != null) {
            List<LinkedHashMap> items = (List) result.get("items");
            var shortDateList = new ArrayList<ShipmentItemShortDateResponseType>();
            lineItems.addAll(items.stream().map(item -> {
                if (item.get("shortDateProducts") != null) {
                    List<LinkedHashMap> shortDateProducts = (List<LinkedHashMap>) item.get("shortDateProducts");
                    shortDateProducts.forEach(shortDate -> shortDateList.add(ShipmentItemShortDateResponseType.
                        builder()
                        .id(Long.valueOf((String) shortDate.get("id")))
                        .shipmentItemId(Long.valueOf((Integer) shortDate.get("shipmentItemId")))
                        .unitNumber((String) shortDate.get("unitNumber"))
                        .productCode((String) shortDate.get("productCode"))
                        .build()));

                }

                return ShipmentFulfillmentRequest.builder()
                    .id(Long.valueOf((String) item.get("id")))
                    .shipmentId(Long.valueOf((Integer) item.get("shipmentId")))
                    .productFamily((String) item.get("productFamily"))
                    .quantity((Integer) item.get("quantity"))
                    .bloodType(BloodType.valueOf((String) item.get("bloodType")))
                    .shortDateProducts(shortDateList)
                    .build();

            }).toList());
        }
        return ShipmentRequestDetailsResponseType.builder()
            .id(Long.valueOf((String) result.get("id")))
            .orderNumber(Long.valueOf((Integer) result.get("orderNumber")))
            .items(lineItems)
            .build();
    }

    public ShipmentRequestDetailsResponseType buildShipmentRequestDetailsResponseType(Long orderNumber,
                                                                                      String priority,
                                                                                      String status,
                                                                                      String shippingCustomerCode,
                                                                                      Long billingCustomerCode,
                                                                                      String locationCode,
                                                                                      String deliveryType,
                                                                                      String shippingMethod,
                                                                                      String productCategory,
                                                                                      LocalDate shippingDate,
                                                                                      String department,
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
                                                                                      String quantities,
                                                                                      String bloodTypes,
                                                                                      String productFamilies,
                                                                                      String unitNumbers,
                                                                                      String productCodes) {

        Assert.assertNotNull(quantities);
        Assert.assertNotNull(bloodTypes);
        Assert.assertNotNull(productFamilies);

        List<String> quantityList = Arrays.stream(quantities.split(",")).toList();
        List<String> bloodTypeList = Arrays.stream(bloodTypes.split(",")).toList();
        List<String> familyList = Arrays.stream(productFamilies.split(",")).toList();
        List<String> unitNumberList = unitNumbers != null && !unitNumbers.isEmpty() ? Arrays.stream(unitNumbers.split(",")).toList() : Collections.emptyList();
        List<String> productCodeList = productCodes != null && !productCodes.isEmpty() ? Arrays.stream(productCodes.split(",")).toList() : Collections.emptyList();

        var shipmentDetailType = ShipmentRequestDetailsResponseType.builder()
            .orderNumber(orderNumber)
            .externalId("EXTERNAL_ID")
            .priority(priority)
            .status(status)
            .locationCode(locationCode)
            .shippingMethod(shippingMethod)
            .productCategory(productCategory)
            .shippingDate(shippingDate)
            .department(department)
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
            .department(department)
            .deliveryType(deliveryType)
            .createDate(ZonedDateTime.now())
            .shippingCustomerCode(shippingCustomerCode)
            .shippingCustomerName(shippingCustomerName)
            .comments("DISTRIBUTION COMMENTS")
            .items(new ArrayList<>())
            .build();
        if (!quantityList.isEmpty()) {
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

        if (!unitNumberList.isEmpty()) {
            for (int u = 0; u < unitNumberList.size(); u++) {
                shipmentDetailType.getItems().get(u).getShortDateProducts().add(ShipmentItemShortDateResponseType
                    .builder()
                    .unitNumber(unitNumberList.get(u))
                    .productCode(productCodeList.get(u))
                    .build());
            }
        }

        return shipmentDetailType;
    }

    public ShipmentRequestDetailsResponseType buildShipmentRequestDetailsResponseType(long orderNumber, String locationCode, String customerID, String customerName, String department, String addressLine1, String addressLine2, String unitNumber, String productCode, String productFamily, String bloodType, String expiration, long quantity) {
        return this.buildShipmentRequestDetailsResponseType(orderNumber, "ASAP", "OPEN", customerID, 0L, locationCode, "TEST", "TEST", "Frozen", LocalDate.now(), customerName, department, "", "123456789", "FL", "33016", "US", "1", "Miami", "Miami", addressLine1, addressLine2, String.valueOf(quantity), bloodType, productFamily, unitNumber, productCode);
    }

    public boolean getCheckDigitConfiguration() {
        var query = "SELECT option_value from lk_lookup WHERE type = 'SHIPPING_CHECK_DIGIT_ACTIVE'";
        var checkDigitConfig = databaseService.fetchData(query);
        var records = checkDigitConfig.first().block();
        assert records != null;
        return records.get("option_value").equals("true");
    }
}
