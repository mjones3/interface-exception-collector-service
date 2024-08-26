package com.arcone.biopro.distribution.inventory.verification.steps;

import com.arcone.biopro.distribution.inventory.commm.TestUtil;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.ProductFamily;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.InventoryEntity;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.InventoryEntityRepository;
import com.arcone.biopro.distribution.inventory.verification.utils.TestUtils;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.r2dbc.spi.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
public class KafkaListenersSteps {

    @Value("${topic.label-applied.name}")
    private String labelAppliedTopic;

    @Value("${topic.product-stored.name}")
    private String productStoredTopic;

    @Value("${topic.shipment-completed.name}")
    private String shipmentCompletedTopic;

    @Value("${topic.product-discarded.name}")
    private String productDiscardedTopic;

    private static final String SHIPMENT_COMPLETED_MESSAGE = """
         {
           "eventType":"ShipmentCompleted",
           "eventVersion":"1.0",
           "payload" : {
                "shipmentId":2,
                "orderNumber":1,
                "unitNumber":"%s",
                "productCode":"%s",
                "performedBy":"test-emplyee-id",
                "createDate":"2024-06-14T15:17:25.666122Z"
            }
         }
        """;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private InventoryEntityRepository inventoryEntityRepository;


    @Value("classpath:/db/data.sql")
    private Resource testDataSql;

    @Autowired
    private ConnectionFactory connectionFactory;

    private static final String LABEL_APPLIED_MESSAGE = """
         {
            "eventType":"LabelApplied",
            "eventVersion":"1.0",
            "payload":{
               "unitNumber":"%s",
               "productCode":"%s",
               "shortDescription":"APH PLASMA 24H",
               "location":"%s",
               "aboRh":"OP",
               "productFamily": "PLASMA_TRANSFUSABLE",
               "collectionDate":"2025-01-08T06:00:00.000Z",
               "expirationDate":"2025-01-08T06:00:00.000",
               "performedBy":"userId",
               "createDate":"2025-01-08T06:00:00.000Z"
            }
         }
        """;
    private static final String EVENT_SHIPMENT_COMPLETED = "Shipment Completed";

    private static final String PRODUCT_STORED_MESSAGE = """
        {
          "eventType": "ProductStored",
          "eventVersion": "1.0",
          "payload": {
            "unitNumber": "%s",
            "productCode": "%s",
            "deviceStored": "Freezer001",
            "deviceUse": "Initial Freezer",
            "storageLocation": "Bin001,Shelf002,Tray001",
            "location": "%s",
            "locationType": "Blood Center",
            "storageTime": "2025-01-08T06:00:00.000Z",
            "performedBy": "userId"
          }
        }
        """;

    private static final String PRODUCT_DISCARDED_MESSAGE = """
        {
          "eventType": "ProductDiscarded",
          "eventVersion": "1.0",
          "payload": {
             "unitNumber": "%s",
            "productCode": "%s",
            "reasonDescriptionKey": "ADDITIVE_SOLUTION_ISSUES",
             "comments": "The comments about discarded product",
            "triggeredBy": "USER_ID",
            "performedBy": "USER_ID",
            "createDate": "2024-07-01T00:10:00Z"
          }
        }
        """;

    private static final String EVENT_LABEL_APPLIED = "Label Applied";
    private static final String EVENT_PRODUCT_STORED = "Product Stored";
    private static final String EVENT_PRODUCT_DISCARDED = "Product Discarded";

    private Map<String, String> topicsMap;

    private Map<String, String> messagesMap;

    private final CountDownLatch waiter = new CountDownLatch(1);

    private String topicName;

    private String untNumber;


    @Before
    public void before() {
        populateTestData();
        topicsMap = Map.of(
            EVENT_LABEL_APPLIED, labelAppliedTopic,
            EVENT_SHIPMENT_COMPLETED, shipmentCompletedTopic,
            EVENT_PRODUCT_STORED, productStoredTopic,
            EVENT_PRODUCT_DISCARDED, productDiscardedTopic
        );

        messagesMap = Map.of(
            EVENT_LABEL_APPLIED, LABEL_APPLIED_MESSAGE,
            EVENT_PRODUCT_STORED, PRODUCT_STORED_MESSAGE,
            EVENT_SHIPMENT_COMPLETED, SHIPMENT_COMPLETED_MESSAGE,
            EVENT_PRODUCT_DISCARDED, PRODUCT_DISCARDED_MESSAGE
            );
    }

    @Given("I am listening the {string} event")
    public void iAmListeningEvent(String event) {
        untNumber = TestUtil.randomString(13);
        topicName = topicsMap.get(event);
        if (!EVENT_LABEL_APPLIED.equals(event)) {
            createInventory(untNumber, "E0869VA0", ProductFamily.PLASMA_TRANSFUSABLE, AboRhType.OP, "Miami", 10, InventoryStatus.AVAILABLE);
        }

    }

    @Given("I am listening the {string} event for {string}")
    public void iAmListeningEventForUnitNumber(String event, String untNumber) {
        untNumber = untNumber;
        topicName = topicsMap.get(event);
        if (!EVENT_LABEL_APPLIED.equals(event)) {
            createInventory(untNumber, "E0869VA0", ProductFamily.PLASMA_TRANSFUSABLE, AboRhType.OP, "Miami", 10, InventoryStatus.AVAILABLE);
        }

    }

    private void createInventory(String unitNumber, String productCode, ProductFamily productFamily, AboRhType aboRhType, String location, Integer daysToExpire, InventoryStatus status) {
        inventoryEntityRepository.save(InventoryEntity.builder()
            .id(UUID.randomUUID())
            .productFamily(productFamily)
            .aboRh(aboRhType)
            .location(location)
            .collectionDate(ZonedDateTime.now().toString())
            .inventoryStatus(status)
            .expirationDate(LocalDateTime.now().plusDays(daysToExpire))
            .unitNumber(unitNumber)
            .productCode(productCode)
            .shortDescription("Short description")
            .build()).block();

    }

    @When("I receive an event {string} event")
    public void iReceiveAnEvent(String event) throws Exception {
        testUtils.kafkaSender(
            untNumber + "-E0869VA0",
            buildMessage(event),
            topicName);
    }

    @Then("The inventory status is {string}")
    public void theInventoryIsCreatedCorrectly(String status) throws InterruptedException {
        InventoryEntity entity = getInventoryWithRetry(untNumber, "E0869VA0", InventoryStatus.valueOf(status));

        assertNotNull(entity);
        assertEquals("E0869VA0", entity.getProductCode());
        assertEquals(untNumber, entity.getUnitNumber());
        assertEquals(status, entity.getInventoryStatus().name());
    }

    public void populateTestData() {
        ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator();
        resourceDatabasePopulator.addScript(testDataSql);
        Mono.from(resourceDatabasePopulator.populate(connectionFactory)).block();
    }

    public String buildMessage(String eventType, String unitNumber, String productCode, String location){
        return String.format(messagesMap.get(eventType),unitNumber, productCode, location);
    }

    public String buildMessage(String eventType){
        return String.format(messagesMap.get(eventType),untNumber, "E0869VA0", "MIAMI");
    }

    public InventoryEntity getInventoryWithRetry(String unitNumber, String productCode, InventoryStatus status) throws InterruptedException {
        int maxTryCount = 60;
        int tryCount = 0;

        InventoryEntity entity = null;
        while (tryCount < maxTryCount && entity == null) {
            entity = inventoryEntityRepository.findByUnitNumberAndProductCodeAndInventoryStatus(unitNumber, productCode, status).block();

            tryCount++;
            waiter.await(1, TimeUnit.SECONDS);
        }
        return entity;
    }

    public InventoryEntity getStoredInventory(String unitNumber, String productCode, InventoryStatus status) throws InterruptedException {
        int maxTryCount = 60;
        int tryCount = 0;

        InventoryEntity entity = null;
        while (tryCount < maxTryCount && entity == null) {
            entity = inventoryEntityRepository.findByUnitNumberAndProductCodeAndInventoryStatus(unitNumber, productCode, status).block();

            if (Objects.nonNull(entity) && Objects.isNull(entity.getDeviceStored())) {
                entity = null;
            }

            tryCount++;
            waiter.await(1, TimeUnit.SECONDS);
        }
        return entity;
    }

    @When("I receive a {string} message with unit number {string}, product code {string} and location {string}")
    public void iReceiveAMessageWithUnitNumberProductCodeAndLocation(String event, String unitNumber, String productCode, String location) throws Exception {
        testUtils.kafkaSender(
            unitNumber + "-E0869VA0",
            buildMessage(event, unitNumber, productCode, location),
            topicName);
    }

    @And("For unit number {string} and product code {string} the device stored is {string} and the storage location is {string}")
    public void forUnitNumberAndProductCodeTheDeviceStoredIsAndTheStorageLocationIs(String unitNumber, String productCode, String deviceStorage, String storageLocation) throws InterruptedException {
        InventoryEntity inventory = getStoredInventory(unitNumber, productCode, InventoryStatus.AVAILABLE);
        assertNotNull(inventory);
        assertEquals(deviceStorage, inventory.getDeviceStored());
        assertEquals(storageLocation, inventory.getStorageLocation());
    }

}
