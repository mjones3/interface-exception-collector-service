package com.arcone.biopro.distribution.inventory.verification.steps;

import com.arcone.biopro.distribution.inventory.commm.TestUtil;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.ProductFamily;
import com.arcone.biopro.distribution.inventory.domain.model.vo.History;
import com.arcone.biopro.distribution.inventory.domain.model.vo.Quarantine;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.InventoryEntity;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.InventoryEntityRepository;
import com.arcone.biopro.distribution.inventory.verification.common.ScenarioContext;
import com.arcone.biopro.distribution.inventory.verification.utils.TestUtils;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;


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

    @Value("${topic.product-quarantined.name}")
    private String productQuarantinedTopic;

    @Value("${topic.product-remove-quarantined.name}")
    private String quarantineRemovedTopic;

    @Value("${topic.product-update-quarantined.name}")
    private String quarantineUpdatedTopic;

    @Autowired
    private ScenarioContext scenarioContext;

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

    private static final String PRODUCT_QUARANTINED_MESSAGE = """
       {
          "eventId": "7eaefe46-e6cf-4434-93c4-a4b1e7d44285",
          "occurredOn": "2024-08-22T12:34:32.270657005Z",
          "eventVersion": "1.0",
          "eventType": "ProductQuarantined",
          "payload": {
            "id": 1,
            "unitNumber": "%s",
            "productCode": "%s",
            "reason": "OTHER",
            "comments": "a comment",
            "stopsManufacturing": false,
            "performedBy": "USER_ID",
            "createDate": "2025-01-08T02:05:45.231Z"
          }
       }
       """;

    private static final String QUARANTINE_UPDATED_MESSAGE = """
        {
           "eventId": "7eaefe46-e6cf-4434-93c4-a4b1e7d44285",
           "occurredOn": "2024-08-22T12:34:32.270657005Z",
           "eventVersion": "1.0",
           "eventType": "QuarantineUpdated",
           "payload": {
             "id": 1,
             "unitNumber": "%s",
             "productCode": "%s",
             "oldReason": "OTHER",
             "newReason": "UNDER_INVESTIGATION",
             "comments": "a under investigation comment",
             "stopsManufacturing": true,
             "performedBy": "USER_ID",
             "createDate": "2025-01-08T02:05:45.231Z"
           }
         }
       """;

    private static final String QUARANTINE_REMOVED_MESSAGE = """
       {
          "eventId": "7eaefe46-e6cf-4434-93c4-a4b1e7d44285",
          "occurredOn": "2024-08-22T12:34:32.270657005Z",
          "eventVersion": "1.0",
          "eventType": "QuarantineRemoved",
          "payload": {
            "id": 1,
            "unitNumber": "%s",
            "productCode": "%s",
            "reason": "OTHER",
            "performedBy": "USER_ID",
            "createDate": "2025-01-08T02:05:45.231Z"
          }
       }
       """;

    public static final String EVENT_LABEL_APPLIED = "Label Applied";
    public static final String EVENT_PRODUCT_STORED = "Product Stored";
    public static final String EVENT_PRODUCT_DISCARDED = "Product Discarded";
    public static final String EVENT_PRODUCT_QUARANTINED = "Product Quarantined";
    public static final String EVENT_QUARANTINE_UPDATED = "Quarantine Updated";
    public static final String EVENT_QUARANTINE_REMOVED = "Quarantine Removed";



    private Map<String, String> topicsMap;

    private Map<String, String> messagesMap;

    private String topicName;

    @Before
    public void before() {
        populateTestData();
        topicsMap = Map.of(
            EVENT_LABEL_APPLIED, labelAppliedTopic,
            EVENT_SHIPMENT_COMPLETED, shipmentCompletedTopic,
            EVENT_PRODUCT_STORED, productStoredTopic,
            EVENT_PRODUCT_DISCARDED, productDiscardedTopic,
            EVENT_PRODUCT_QUARANTINED, productQuarantinedTopic,
            EVENT_QUARANTINE_REMOVED, quarantineRemovedTopic,
            EVENT_QUARANTINE_UPDATED, quarantineUpdatedTopic
        );

        messagesMap = Map.of(
            EVENT_LABEL_APPLIED, LABEL_APPLIED_MESSAGE,
            EVENT_PRODUCT_STORED, PRODUCT_STORED_MESSAGE,
            EVENT_SHIPMENT_COMPLETED, SHIPMENT_COMPLETED_MESSAGE,
            EVENT_PRODUCT_DISCARDED, PRODUCT_DISCARDED_MESSAGE,
            EVENT_PRODUCT_QUARANTINED, PRODUCT_QUARANTINED_MESSAGE,
            EVENT_QUARANTINE_REMOVED, QUARANTINE_REMOVED_MESSAGE,
            EVENT_QUARANTINE_UPDATED, QUARANTINE_UPDATED_MESSAGE
        );
    }

    @Given("I am listening the {string} event")
    public void iAmListeningEvent(String event) {
        scenarioContext.setUnitNumber(TestUtil.randomString(13));
        scenarioContext.setProductCode("E0869VA0");
        scenarioContext.setEvent(event);
        topicName = topicsMap.get(event);
        if (!EVENT_LABEL_APPLIED.equals(event)) {
            createInventory(scenarioContext.getUnitNumber(), scenarioContext.getProductCode(), ProductFamily.PLASMA_TRANSFUSABLE, AboRhType.OP, "Miami", 10, InventoryStatus.AVAILABLE);
        }

    }

    @Given("I am listening the {string} event for {string}")
    public void iAmListeningEventForUnitNumber(String event, String untNumber) {
        scenarioContext.setUnitNumber(untNumber);
        scenarioContext.setProductCode("E0869VA0");
        topicName = topicsMap.get(event);
        if (!EVENT_LABEL_APPLIED.equals(event)) {
            createInventory(scenarioContext.getUnitNumber(), scenarioContext.getProductCode(), ProductFamily.PLASMA_TRANSFUSABLE, AboRhType.OP, "Miami", 10, InventoryStatus.AVAILABLE);
        }
    }

    private InventoryEntity createInventory(String unitNumber, String productCode, ProductFamily productFamily, AboRhType aboRhType, String location, Integer daysToExpire, InventoryStatus statusParam) {

        List<Quarantine> quarantines = null;
        List<History> histories = null;

        InventoryStatus status = statusParam;

        if (topicName.equals(quarantineRemovedTopic) || topicName.equals(quarantineUpdatedTopic)) {
            quarantines = List.of(new Quarantine(1L, "OTHER", "a comment"));
            histories = List.of(new History(InventoryStatus.AVAILABLE, null, null));
            status = InventoryStatus.QUARANTINED;
        }

        return inventoryEntityRepository.save(InventoryEntity.builder()
            .id(UUID.randomUUID())
            .productFamily(productFamily)
            .aboRh(aboRhType)
            .location(location)
            .collectionDate(ZonedDateTime.now().toString())
            .inventoryStatus(status)
            .expirationDate(LocalDateTime.now().plusDays(daysToExpire))
            .unitNumber(unitNumber)
            .productCode(productCode)
            .quarantines(quarantines)
            .histories(histories)
            .shortDescription("Short description")
            .build()).block();

    }

    @When("I receive an event {string} event")
    public void iReceiveAnEvent(String event) throws Exception {
        scenarioContext.setProductCode("E0869VA0");
        testUtils.kafkaSender(
            scenarioContext.getUnitNumber() + "-" + scenarioContext.getProductCode(),
            buildMessage(event),
            topicName);
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
        return String.format(messagesMap.get(eventType),scenarioContext.getUnitNumber(), scenarioContext.getProductCode(), "MIAMI");
    }

    @When("I receive a {string} message with unit number {string}, product code {string} and location {string}")
    public void iReceiveAMessageWithUnitNumberProductCodeAndLocation(String event, String unitNumber, String productCode, String location) throws Exception {
        testUtils.kafkaSender(
            scenarioContext.getUnitNumber() + "-" + scenarioContext.getProductCode(),
            buildMessage(event, unitNumber, productCode, location),
            topicName);
    }

}
