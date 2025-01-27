package com.arcone.biopro.distribution.shipping.verification.support.controllers;

import com.arcone.biopro.distribution.shipping.domain.model.enumeration.BloodType;
import com.arcone.biopro.distribution.shipping.verification.support.ApiHelper;
import com.arcone.biopro.distribution.shipping.verification.support.DatabaseService;
import com.arcone.biopro.distribution.shipping.verification.support.KafkaHelper;
import com.arcone.biopro.distribution.shipping.verification.support.TestUtils;
import com.arcone.biopro.distribution.shipping.verification.support.Topics;
import com.arcone.biopro.distribution.shipping.verification.support.graphql.GraphQLMutationMapper;
import com.arcone.biopro.distribution.shipping.verification.support.graphql.GraphQLQueryMapper;
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
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;


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

    @Value("${default.ui.facility}")
    private String facility;

    @Autowired
    private DatabaseService databaseService;

    private static final Map<String, String> ineligibleStatusMap = Map.of(
        "Inventory Not Found", "INVENTORY_NOT_FOUND_IN_LOCATION",
        "Expired", "INVENTORY_IS_EXPIRED",
        "Unsuitable", "INVENTORY_IS_UNSUITABLE",
        "Quarantined", "INVENTORY_IS_QUARANTINED",
        "Discarded", "INVENTORY_IS_DISCARDED",
        "Inventory Not Exist", "INVENTORY_NOT_EXIST",
        "Already Shipped", "INVENTORY_IS_SHIPPED"
    );


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

    public long createShippingRequest(long orderNumber, String priority) throws Exception {
        var resource = utils.getResource("order-fulfilled.json")
            .replace("{order.number}", String.valueOf(orderNumber))
            .replace("{order.priority}",priority);

        kafkaHelper.sendEvent(UUID.randomUUID().toString(), objectMapper.readValue(resource, OrderFulfilledEventType.class), Topics.ORDER_FULFILLED).block();
        // Add sleep to wait for the message to be consumed.
        Thread.sleep(3000);

        log.info("Message sent to create the order: {}", orderNumber);
        return orderNumber;
    }
    public long createShippingRequest() throws Exception {
        long orderId = new Random().nextInt(10000);
        var resource = utils.getResource("order-fulfilled.json")
            .replace("{order.number}", String.valueOf(orderId))
            .replace("{order.priority}","ASAP");;

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
        } else {
            throw new RuntimeException("Shipments not found.");
        }
    }

    public List<ListShipmentsResponseType> listShipments() {
        log.info("Listing orders.");
        var response = apiHelper.graphQlRequestObjectList(GraphQLQueryMapper.listShipmentsQuery(), "listShipments");
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
        return apiHelper.graphQlRequest(GraphQLQueryMapper.shipmentDetailsQuery(shipmentId), "getShipmentDetailsById");
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
            .priority((String) result.get("priority"))
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

    public void setVisualInspectionConfiguration(String status) {
        if (status.equalsIgnoreCase("enabled")) {
            status = "true";
        } else if (status.equalsIgnoreCase("disabled")) {
            status = "false";
        } else {
            throw new RuntimeException("Invalid status.");
        }
        var query = String.format("UPDATE lk_lookup SET option_value = '%s' WHERE type = 'SHIPPING_VISUAL_INSPECTION_ACTIVE'", status);
        databaseService.executeSql(query).block();
    }

    public boolean getCheckVisualInspectionConfig() {
        var query = "SELECT option_value from lk_lookup WHERE type = 'SHIPPING_VISUAL_INSPECTION_ACTIVE'";
        var checkDigitConfig = databaseService.fetchData(query);
        var records = checkDigitConfig.first().block();
        assert records != null;
        return records.get("option_value").equals("true");
    }

    public String getConfiguredDiscardReasons(){
        var query = "SELECT reason_key from lk_reason WHERE type = 'VISUAL_INSPECTION_FAILED' AND active = true ORDER BY order_number";
        var reasonsList = databaseService.fetchData(query);
        var records = reasonsList.all().switchIfEmpty(Flux.empty()).collectList().block();
        return String.join(",", records.stream().map(x-> x.get("reason_key").toString().replace("_"," ")).toList());
    }

    public Long createPackedShipment(String orderNumber, List<String> unitNumbers, List<String> productCodes, String itemStatus, String productFamily, String bloodType, Integer totalRequested) {

        var insertShipment = "INSERT INTO bld_shipment " +
            "(order_number, customer_code, customer_name, customer_phone_number, location_code, delivery_type, priority, shipment_method, product_category, status, state, postal_code, country" +
            " , country_code, city, district, address_line1, address_line2, address_contact_name, shipping_date, create_date, modification_date, delete_date, \"comments\", department_name, created_by_employee_id" +
            " , completed_by_employee_id, complete_date, external_id) " +
            " VALUES(%s,'B2346', 'Advanced Medical Center', '234-567-8901', '123456789', 'STAT', 'STAT', 'FEDEX', 'REFRIGERATED', 'OPEN', 'CA', '90210', 'US', 'US', 'Beverly Hills', 'LA', '456 Elm Street', 'Suite 200', NULL, '2024-10-07', '2024-10-07 12:45:34.084', '2024-10-07 12:45:34.084', NULL, '', 'Cardiology', 'mock-employee-id', NULL, NULL, 'DST108');";

        databaseService.executeSql(String.format(insertShipment, orderNumber)).block();

        var createdShipment = databaseService.fetchData(String.format("select id from bld_shipment where order_number = %s ", orderNumber)).first().block();
        if(createdShipment != null){

            var shipmentId = createdShipment.get("id");

            var insertShipItem = "INSERT INTO bld_shipment_item " +
                "(shipment_id, product_family, blood_type, quantity, \"comments\", create_date, modification_date) " +
                "VALUES(%s, '%s', '%s', %s, 'For neonatal use', now(), now());";

            databaseService.executeSql(String.format(insertShipItem, shipmentId,productFamily,bloodType, totalRequested)).block();


            var createdShipmentItem = databaseService.fetchData(String.format("select id from bld_shipment_item where shipment_id = %s limit 1", createdShipment.get("id"))).first().block();

            if(createdShipmentItem != null){

                for (int i = 0; i < unitNumbers.size(); i++) {
                    if (itemStatus.equalsIgnoreCase("packed")) {
                        createPackedItem(createdShipmentItem.get("id").toString(), unitNumbers.get(i), productCodes.get(i));
                    } else if (itemStatus.equalsIgnoreCase("verified")) {
                        createVerifiedItem(createdShipmentItem.get("id").toString(), unitNumbers.get(i), productCodes.get(i));
                    }else if (itemStatus.equalsIgnoreCase("unsuitable-verified")) {
                        createUnsuitableVerifiedItem(createdShipmentItem.get("id").toString(), unitNumbers.get(i), productCodes.get(i));
                    }
                }
            }
            return Long.valueOf(shipmentId.toString());
        }
        return null;
    }

    private void createPackedItem(String shipmentItemId,String unitNumber, String productCode){

            var insertPackedItem = "INSERT INTO bld_shipment_item_packed " +
                "(shipment_item_id, unit_number, product_code, product_description, abo_rh, packed_by_employee_id, expiration_date, collection_date, create_date, modification_date, visual_inspection, blood_type, product_family,second_verification,verification_date , verified_by_employee_id) " +
                " VALUES(%s, '%s', '%s', 'APH FFP C', 'BP', '5db1da0b-6392-45ff-86d0-17265ea33226', '2025-11-02 13:15:47.152', '2024-10-04 06:15:47.152', now(), now(), 'SATISFACTORY', 'B', 'PLASMA_TRANSFUSABLE','PENDING',null , null);";

            databaseService.executeSql(String.format(insertPackedItem, shipmentItemId, unitNumber,productCode)).block();
    }

    private void createUnsuitableVerifiedItem(String shipmentItemId,String unitNumber, String productCode){

            var insertPackedItem = "INSERT INTO bld_shipment_item_packed " +
                "(shipment_item_id, unit_number, product_code, product_description, abo_rh, packed_by_employee_id, expiration_date, collection_date, create_date, modification_date, visual_inspection, blood_type, product_family,second_verification,verification_date , verified_by_employee_id, ineligible_status, ineligible_action, ineligible_reason, ineligible_message) " +
                " VALUES(%s, '%s', '%s', 'APH FFP C', 'BP', '5db1da0b-6392-45ff-86d0-17265ea33226', '2025-11-02 13:15:47.152', '2024-10-04 06:15:47.152', now(), now(), 'SATISFACTORY', 'B', 'PLASMA_TRANSFUSABLE','COMPLETED',now() , '5db1da0b-6392-45ff-86d0-17265ea33226', STATUS, ACTION, REASON, MESSAGE);";

            databaseService.executeSql(String.format(insertPackedItem, shipmentItemId, unitNumber,productCode)).block();
    }

    private void createVerifiedItem(String shipmentItemId,String unitNumber, String productCode){

        var insertPackedItem = "INSERT INTO bld_shipment_item_packed " +
            "(shipment_item_id, unit_number, product_code, product_description, abo_rh, packed_by_employee_id, expiration_date, collection_date, create_date, modification_date, visual_inspection, blood_type, product_family,second_verification,verification_date , verified_by_employee_id) " +
            " VALUES(%s, '%s', '%s', 'APH FFP C', 'BP', '5db1da0b-6392-45ff-86d0-17265ea33226', '2025-11-02 13:15:47.152', '2024-10-04 06:15:47.152', now(), now(), 'SATISFACTORY', 'B', 'PLASMA_TRANSFUSABLE','COMPLETED',now() , '5db1da0b-6392-45ff-86d0-17265ea33226');";

        databaseService.executeSql(String.format(insertPackedItem, shipmentItemId, unitNumber,productCode)).block();
    }

    public void updateShipmentItemStatus(Long shipmentId, String unitNumber, String status, String message) {
        var query = String.format("UPDATE bld_shipment_item_packed SET ineligible_status = '%s', ineligible_message = '%s' WHERE unit_number = '%s' AND shipment_item_id = (SELECT id FROM bld_shipment_item WHERE shipment_id = %s)", ineligibleStatusMap.get(status), message, unitNumber, shipmentId);
        databaseService.executeSql(query).block();
    }

    public Map fillShipment(long shipmentId, String unitNumber, String productCode, String inspection, boolean unsuitable) throws Exception {
        var shipmentDetails = parseShipmentRequestDetail(
            getShipmentRequestDetails(shipmentId));
        Long shipmentItem;
        shipmentItem = shipmentDetails.getItems().getFirst().getId();

        var response = apiHelper.graphQlRequest(GraphQLMutationMapper.packItemMutation(shipmentItem, facility
            , TestUtils.removeUnitNumberScanDigits(unitNumber), "test-emplyee-id", TestUtils.removeProductCodeScanDigits(productCode), inspection), "packItem");
        if (!unsuitable){
        Assert.assertEquals("200 OK", response.get("ruleCode"));
        }
        Thread.sleep(kafkaWaitingTime);
        return response;
    }

    public Long getShipmentItemId(long shipmentId, String family, String bloodType) {
        var query = String.format("SELECT id FROM bld_shipment_item WHERE shipment_id = %s AND product_family = '%s' AND blood_type = '%s'", shipmentId, family, bloodType);
        var shipmentItem = databaseService.fetchData(query);
        var records = shipmentItem.first().block();
        assert records != null;
        return Long.valueOf(records.get("id").toString());
    }
}
