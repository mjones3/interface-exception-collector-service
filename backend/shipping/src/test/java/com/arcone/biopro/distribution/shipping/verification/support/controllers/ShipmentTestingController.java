package com.arcone.biopro.distribution.shipping.verification.support.controllers;

import com.arcone.biopro.distribution.shipping.domain.model.enumeration.BloodType;
import com.arcone.biopro.distribution.shipping.verification.support.ApiHelper;
import com.arcone.biopro.distribution.shipping.verification.support.DatabaseService;
import com.arcone.biopro.distribution.shipping.verification.support.KafkaHelper;
import com.arcone.biopro.distribution.shipping.verification.support.SharedContext;
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
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;
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
import java.util.Optional;
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

    @Autowired
    private SharedContext sharedContext;

    private static final Map<String, String> ineligibleStatusMap = Map.of(
        "Inventory Not Found", "INVENTORY_NOT_FOUND_IN_LOCATION",
        "Expired", "INVENTORY_IS_EXPIRED",
        "Unsuitable", "INVENTORY_IS_UNSUITABLE",
        "Quarantined", "INVENTORY_IS_QUARANTINED",
        "Discarded", "INVENTORY_IS_DISCARDED",
        "Inventory Not Exist", "INVENTORY_NOT_EXIST",
        "Already Shipped", "INVENTORY_IS_SHIPPED"
    );


    public void createShippingRequest(ShipmentRequestDetailsResponseType shipmentDetail) throws Exception {

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

        log.debug("Message sent to create the order: {}", shipmentDetail.getOrderNumber());
        sharedContext.setOrderNumber(shipmentDetail.getOrderNumber());
        sharedContext.setShipmentId(this.getShipmentId(sharedContext.getOrderNumber()));
    }

    public long createShippingRequest(long orderNumber, String priority, String shippingDate, String shippingMethod) throws Exception {

        var shippingDateFormat = Optional.ofNullable(shippingDate).map(shippingDateMap -> "\"" + shippingDateMap + "\"").orElse("null");

        var resource = utils.getResource("order-fulfilled.json")
            .replace("{order.number}", String.valueOf(orderNumber))
            .replace("{order.priority}", priority)
            .replace("\"{order.shipping_date}\"", shippingDateFormat);

        if (shippingMethod != null && !shippingMethod.isBlank()) {
            resource = resource.replace("{SHIPPING_METHOD}", shippingMethod);
        } else {
            resource = resource.replace("{SHIPPING_METHOD}", "TEST");
        }

        kafkaHelper.sendEvent(UUID.randomUUID().toString(), objectMapper.readValue(resource, OrderFulfilledEventType.class), Topics.ORDER_FULFILLED).block();
        // Add sleep to wait for the message to be consumed.
        Thread.sleep(3000);

        log.debug("Message sent to create the order: {}", orderNumber);
        return orderNumber;
    }

    public long createShippingRequest() throws Exception {
        long orderId = new Random().nextInt(10000);
        var resource = utils.getResource("order-fulfilled.json")
            .replace("{order.number}", String.valueOf(orderId))
            .replace("{order.shipping_date}", "2025-12-31")
            .replace("{order.priority}", "ASAP");

        kafkaHelper.sendEvent(UUID.randomUUID().toString(), objectMapper.readValue(resource, OrderFulfilledEventType.class), Topics.ORDER_FULFILLED).block();
        // Add sleep to wait for the message to be consumed.
        Thread.sleep(3000);

        log.debug("Message sent to create the order: {}", orderId);
        return orderId;
    }

    public long createShippingRequest(String orderNumber, String priority, String shipDate
        , String shipmentType, String labelStatus, boolean quarantinedProducts) throws Exception {
        long orderId = Long.parseLong(orderNumber);
        var resource = utils.getResource("order-fulfilled-with-parameters.json")
            .replace("{order.number}", orderNumber)
            .replace("{order.shipping_date}", shipDate)
            .replace("{order.priority}", priority)
            .replace("{LABEL_STATUS}", labelStatus)
            .replace("{SHIPPING_METHOD}", "FEDEX")
            .replace("{SHIPMENT_TYPE}", shipmentType)
            .replace("{QUARANTINED_PRODUCTS}", String.valueOf(quarantinedProducts));

        kafkaHelper.sendEvent(UUID.randomUUID().toString(), objectMapper.readValue(resource, OrderFulfilledEventType.class), Topics.ORDER_FULFILLED).block();
        // Add sleep to wait for the message to be consumed.
        Thread.sleep(kafkaWaitingTime);

        log.debug("Message sent to create the order: {} payload {}", orderId, resource);
        return orderId;
    }

    public long getShipmentId(long orderNumber) {
        var orders = this.listShipments();
        // Find the last order in the list with the same order number.
        var orderFilter = orders.stream().filter(x -> x.getOrderNumber().equals(orderNumber)).reduce((first, second) -> second).orElse(null);
        if (orderFilter != null) {
            var shipmentId = orderFilter.getId();
            log.debug("Found Shipment by Order Number");
            return shipmentId;
        } else {
            throw new RuntimeException("Shipments not found.");
        }
    }

    public List<ListShipmentsResponseType> listShipments() {
        log.debug("Listing orders.");
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
        log.debug("List orders response: {}", responseTypeList);
        return responseTypeList;
    }

    public Map getShipmentRequestDetails(long shipmentId) {
        log.debug("Getting order details for order: {}", shipmentId);
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
                                                                                      String productCodes,
                                                                                      String shipmentType,
                                                                                      String labelStatus,
                                                                                      Boolean quarantinedProducts
    ) {

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
            .departmentName(department)
            .departmentCode("DPT_CODE")
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
            .comments("DISTRIBUTION COMMENTS")
            .labelStatus(labelStatus)
            .shipmentType(shipmentType)
            .quarantinedProducts(quarantinedProducts)
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
        return this.buildShipmentRequestDetailsResponseType(orderNumber, "ASAP", "OPEN", customerID, 0L, locationCode, "TEST", "TEST", "FROZEN", LocalDate.now(), customerName, department, "", "123456789", "FL", "33016", "US", "1", "Miami", "Miami", addressLine1, addressLine2, String.valueOf(quantity), bloodType, productFamily, unitNumber, productCode, "CUSTOMER", "LABELED", false);
    }

    public ShipmentRequestDetailsResponseType buildShipmentRequestDetailsResponseType(long orderNumber, String locationCode, String customerID, String customerName, String department
        , String addressLine1, String addressLine2, String unitNumber, String productCode, String productFamily, String bloodType
        , String quantity, String shipmentType, String labelStatus, Boolean quarantinedProducts, String temperatureCategory) {


        return this.buildShipmentRequestDetailsResponseType(orderNumber, "ASAP", "OPEN", customerID, 0L, locationCode, "TEST", "TEST"
            , temperatureCategory, LocalDate.now(), department, customerName, "", "123456789", "FL"
            , "33016", "US", "1", "Miami"
            , "Miami", addressLine1, addressLine2, quantity, bloodType, productFamily, unitNumber, productCode
            , shipmentType, labelStatus, quarantinedProducts);
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

    public String getConfiguredDiscardReasons() {
        var query = "SELECT reason_key from lk_reason WHERE type = 'VISUAL_INSPECTION_FAILED' AND active = true ORDER BY order_number";
        var reasonsList = databaseService.fetchData(query);
        var records = reasonsList.all().switchIfEmpty(Flux.empty()).collectList().block();
        return String.join(",", records.stream().map(x -> x.get("reason_key").toString().replace("_", " ")).toList());
    }

    public Long createPackedShipment(String orderNumber, List<String> unitNumbers, List<String> productCodes, String itemStatus, String productFamily, String bloodType, Integer totalRequested) {
        return createPackedShipment(orderNumber, "B2346", "Advanced Medical Center", "FROZEN", "CUSTOMER", "LABELED", false, unitNumbers, productCodes, itemStatus, productFamily, bloodType, totalRequested);
    }

    public Long createPackedShipment(String orderNumber, String customerCode, String customerName, String temperatureCategory, String shipmentType, String labelStatus, boolean quarantinedProducts, List<String> unitNumbers, List<String> productCodes, String itemStatus, String productFamily, String bloodType, Integer totalRequested) {

        var insertShipment = "INSERT INTO bld_shipment " +
            "(order_number, customer_code, customer_name, customer_phone_number, location_code, delivery_type, priority, shipment_method, product_category, status, state, postal_code, country" +
            " , country_code, city, district, address_line1, address_line2, address_contact_name, shipping_date, create_date, modification_date, delete_date, \"comments\", department_name, created_by_employee_id" +
            " , completed_by_employee_id, complete_date, external_id , label_status , shipment_type , quarantined_products) " +
            " VALUES(%s,'%s', '%s', '234-567-8901', '123456789', 'STAT', 'STAT', 'FEDEX', '%s', 'OPEN', 'CA', '90210', 'US', 'US', 'Beverly Hills', 'LA', '456 Elm Street', 'Suite 200', NULL, '2024-10-07', '2024-10-07 12:45:34.084', '2024-10-07 12:45:34.084', NULL, '', 'Cardiology', 'mock-employee-id', NULL, NULL, '%s','%s','%s',%s);";

        var externalId = "DIS_EXT_" + RandomStringUtils.randomAlphanumeric(10);
        databaseService.executeSql(String.format(insertShipment, orderNumber, customerCode, customerName, temperatureCategory, externalId, labelStatus, shipmentType, quarantinedProducts)).block();

        var createdShipment = databaseService.fetchData(String.format("select id from bld_shipment where order_number = %s ", orderNumber)).first().block();
        if (createdShipment != null) {

            var shipmentId = createdShipment.get("id");

            var insertShipItem = "INSERT INTO bld_shipment_item " +
                "(shipment_id, product_family, blood_type, quantity, \"comments\", create_date, modification_date) " +
                "VALUES(%s, '%s', '%s', %s, 'For neonatal use', now(), now());";

            databaseService.executeSql(String.format(insertShipItem, shipmentId, productFamily, bloodType, totalRequested)).block();

            var createdShipmentItem = databaseService.fetchData(String.format("select id from bld_shipment_item where shipment_id = %s limit 1", createdShipment.get("id"))).first().block();

            if (createdShipmentItem != null) {

                var productStatus = quarantinedProducts ? "QUARANTINED" : null;
                for (int i = 0; i < unitNumbers.size(); i++) {
                    if (itemStatus.equalsIgnoreCase("packed")) {
                        var productDescription = String.format("%s-%s", "APH FFP C", i);
                        createPackedItem(createdShipmentItem.get("id").toString(), unitNumbers.get(i), productCodes.get(i), productDescription, productStatus);
                    } else if (itemStatus.equalsIgnoreCase("verified")) {
                        createVerifiedItem(createdShipmentItem.get("id").toString(), unitNumbers.get(i), productCodes.get(i));
                    } else if (itemStatus.equalsIgnoreCase("unsuitable-verified")) {
                        createUnsuitableVerifiedItem(createdShipmentItem.get("id").toString(), unitNumbers.get(i), productCodes.get(i));
                    }
                }
            }
            return Long.valueOf(shipmentId.toString());
        }
        return null;
    }

    private void createPackedItem(String shipmentItemId, String unitNumber, String productCode, String productDescription, String productStatus) {
        var insertPackedItem = "INSERT INTO bld_shipment_item_packed " +
            "(shipment_item_id, unit_number, product_code, product_description, abo_rh, packed_by_employee_id, expiration_date, collection_date, create_date, modification_date, visual_inspection, blood_type, product_family,second_verification,verification_date , verified_by_employee_id , product_status) " +
            " VALUES(%s, '%s', '%s', '%s', 'BP', '5db1da0b-6392-45ff-86d0-17265ea33226', '2025-11-02 13:15:47.152', '2024-10-04 06:15:47.152', now(), now(), 'SATISFACTORY', 'B', 'PLASMA_TRANSFUSABLE','PENDING',null , null,'%s');";

        databaseService.executeSql(String.format(insertPackedItem, shipmentItemId, unitNumber, productCode, productDescription, productStatus)).block();
    }

    private void createUnsuitableVerifiedItem(String shipmentItemId, String unitNumber, String productCode) {

        var insertPackedItem = "INSERT INTO bld_shipment_item_packed " +
            "(shipment_item_id, unit_number, product_code, product_description, abo_rh, packed_by_employee_id, expiration_date, collection_date, create_date, modification_date, visual_inspection, blood_type, product_family,second_verification,verification_date , verified_by_employee_id, ineligible_status, ineligible_action, ineligible_reason, ineligible_message) " +
            " VALUES(%s, '%s', '%s', 'APH FFP C', 'BP', '5db1da0b-6392-45ff-86d0-17265ea33226', '2025-11-02 13:15:47.152', '2024-10-04 06:15:47.152', now(), now(), 'SATISFACTORY', 'B', 'PLASMA_TRANSFUSABLE','COMPLETED',now() , '5db1da0b-6392-45ff-86d0-17265ea33226', STATUS, ACTION, REASON, MESSAGE);";

        databaseService.executeSql(String.format(insertPackedItem, shipmentItemId, unitNumber, productCode)).block();
    }

    private void createVerifiedItem(String shipmentItemId, String unitNumber, String productCode) {

        var insertPackedItem = "INSERT INTO bld_shipment_item_packed " +
            "(shipment_item_id, unit_number, product_code, product_description, abo_rh, packed_by_employee_id, expiration_date, collection_date, create_date, modification_date, visual_inspection, blood_type, product_family,second_verification,verification_date , verified_by_employee_id) " +
            " VALUES(%s, '%s', '%s', 'APH FFP C', 'BP', '5db1da0b-6392-45ff-86d0-17265ea33226', '2025-11-02 13:15:47.152', '2024-10-04 06:15:47.152', now(), now(), 'SATISFACTORY', 'B', 'PLASMA_TRANSFUSABLE','COMPLETED',now() , '5db1da0b-6392-45ff-86d0-17265ea33226');";

        databaseService.executeSql(String.format(insertPackedItem, shipmentItemId, unitNumber, productCode)).block();
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
        if (!unsuitable) {
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

    public List<Map> getShipmentItems(Long shipmentId) {
        var shipmentDetails = this.getShipmentRequestDetails(shipmentId);
        try {
            sharedContext.setShipmentItems((List<Map>) shipmentDetails.get("items"));
            log.debug("Shipment items: {}", sharedContext.getShipmentItems());
            return sharedContext.getShipmentItems();
        } catch (Exception e) {
            log.debug("Error getting shipment items: {}", e.getMessage());
            throw e;
        }
    }

    public List<LinkedHashMap> getUnlabeledProducts(Long shipmentItemId, String unitNumber, String facility) {
        var payload = GraphQLQueryMapper.getUnlabeledProductsQuery(shipmentItemId, unitNumber, facility);
        var response = apiHelper.graphQlRequest(payload, "getUnlabeledProducts");
        if (response.get("results") != null) {
            var results = (LinkedHashMap) response.get("results");
            if (results.get("results") != null) {
                var itemZero = (List<LinkedHashMap>) results.get("results");
                return (List<LinkedHashMap>) itemZero.get(0);
            }
            return null;
        } else {
            return null;
        }
    }

    public String getProductFamilyDescription(String productFamilyKey) {
        Map<String, String> productFamilyDescription = Map.of(
            "WHOLE_BLOOD", "Whole Blood",
            "WHOLE_BLOOD_LEUKOREDUCED", "Whole Blood Leukoreduced",
            "RED_BLOOD_CELLS", "Red Blood Cells",
            "RED_BLOOD_CELLS_LEUKOREDUCED", "Red Blood Cells Leukoreduced",
            "APHERESIS_PLATELETS_LEUKOREDUCED", "Apheresis Platelets Leukoreduced",
            "PRT_APHERESIS_PLATELETS", "PRT Apheresis Platelets",
            "CRYOPRECIPITATE", "Cryoprecipitate",
            "PLASMA_TRANSFUSABLE", "Plasma Transfusable",
            "PLASMA_MFG_NONINJECTABLE", "Plasma Mfg Noninjectable",
            "PLASMA_MFG_INJECTABLE", "Plasma Mfg Injectable"
        );
        return productFamilyDescription.get(productFamilyKey);
    }

public void verifyItem(Long shipmentId, String un, String productCode, String employeeId) {
        var verifyItemResponse = apiHelper.graphQlRequest(GraphQLMutationMapper.verifyItemMutation(shipmentId, un, productCode, employeeId), "verifyItem");
        if (verifyItemResponse.get("results") != null) {
            var verifiedList = ((ArrayList<LinkedHashMap>) ((LinkedHashMap) verifyItemResponse.get("results")).get("results"));
            var verifiedItems = ((ArrayList<LinkedHashMap>) verifiedList.get(0).get("verifiedItems"));
            sharedContext.setVerifiedProductsList(verifiedItems);
        }
    }
}


