package com.arcone.biopro.distribution.inventory.verification.steps;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.EventMessage;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.discarded.ProductDiscardedMessage;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.label.LabelAppliedMessage;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.quarantine.AddQuarantinedMessage;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.quarantine.RemoveQuarantinedMessage;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.quarantine.UpdateQuarantinedMessage;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.recovered.ProductRecoveredMessage;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.shipment.ShipmentCompletedMessage;
import com.arcone.biopro.distribution.inventory.adapter.in.listener.storage.ProductStoredMessage;
import com.arcone.biopro.distribution.inventory.commm.TestUtil;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.ProductFamily;
import com.arcone.biopro.distribution.inventory.domain.model.vo.History;
import com.arcone.biopro.distribution.inventory.domain.model.vo.Quarantine;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.InventoryEntity;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.InventoryEntityRepository;
import com.arcone.biopro.distribution.inventory.verification.common.ScenarioContext;
import com.arcone.biopro.distribution.inventory.verification.utils.LogMonitor;
import com.arcone.biopro.distribution.inventory.verification.utils.TestUtils;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.r2dbc.spi.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class KafkaListenersSteps {

    @Value("${topic.label-applied.name}")
    private String labelAppliedTopic;

    @Value("${topic.product-stored.name}")
    private String productStoredTopic;

    @Value("${topic.shipment-completed.name}")
    private String shipmentCompletedTopic;

    @Value("${topic.product-discarded.name}")
    private String productDiscardedTopic;

    @Value("${topic.product-recovered.name}")
    private String productRecoveredTopic;

    @Value("${topic.product-quarantined.name}")
    private String productQuarantinedTopic;

    @Value("${topic.product-remove-quarantined.name}")
    private String quarantineRemovedTopic;

    @Value("${topic.product-update-quarantined.name}")
    private String quarantineUpdatedTopic;

    private final ScenarioContext scenarioContext;

    private final LogMonitor logMonitor;

    private final ObjectMapper objectMapper;

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

    private final TestUtils testUtils;

    private final InventoryEntityRepository inventoryEntityRepository;


    @Value("classpath:/db/data.sql")
    private Resource testDataSql;

    private final ConnectionFactory connectionFactory;

    private static final String LABEL_APPLIED_MESSAGE = """
         {
            "eventType":"LabelApplied",
            "eventVersion":"1.0",
            "payload":{
               "unitNumber":"%s",
               "productCode":"%s",
               "productDescription":"APH PLASMA 24H",
               "location":"%s",
               "aboRh":"OP",
               "weight": 123,
               "isLicensed": true,
               "productFamily": "PLASMA_TRANSFUSABLE",
               "collectionDate":"2025-01-08T06:00:00.000Z",
               "expirationDate":"2025-01-08T06:00:00.000Z",
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
            "productCode": "E0869A0",
            "reasonDescriptionKey": "ADDITIVE_SOLUTION_ISSUES",
             "comments": "The comments about discarded product",
            "triggeredBy": "USER_ID",
            "performedBy": "USER_ID",
            "createDate": "2024-07-01T00:10:00Z"
          }
        }
        """;

    private static final String PRODUCT_RECOVERED_MESSAGE = """
        {
           "eventId": "7eaefe46-e6cf-4434-93c4-a4b1e7d44285",
           "occurredOn": "2024-08-22T12:34:32.270657005Z",
           "eventVersion": "1.0",
           "eventType": "ProductRecovered",
           "payload": {
             "unitNumber": "%s",
            "productCode": "E0869A0",
             "performedBy": "USER_ID",
             "createDate": "2025-01-08T02:05:45.231Z"
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
            "productCode": "E0869A0",
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
             "productCode": "E0869A0",
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
            "productCode": "E0869A0",
            "reason": "OTHER",
            "stopsManufacturing": false,
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
    public static final String EVENT_PRODUCT_RECOVERED = "Product Recovered";




    private Map<String, String> topicsMap;

    private Map<String, String> messagesMap;

    private Map<String, Class<?>> fieldsMap;

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
            EVENT_QUARANTINE_UPDATED, quarantineUpdatedTopic,
            EVENT_PRODUCT_RECOVERED, productRecoveredTopic
        );

        messagesMap = Map.of(
            EVENT_LABEL_APPLIED, LABEL_APPLIED_MESSAGE,
            EVENT_PRODUCT_STORED, PRODUCT_STORED_MESSAGE,
            EVENT_SHIPMENT_COMPLETED, SHIPMENT_COMPLETED_MESSAGE,
            EVENT_PRODUCT_DISCARDED, PRODUCT_DISCARDED_MESSAGE,
            EVENT_PRODUCT_QUARANTINED, PRODUCT_QUARANTINED_MESSAGE,
            EVENT_QUARANTINE_REMOVED, QUARANTINE_REMOVED_MESSAGE,
            EVENT_QUARANTINE_UPDATED, QUARANTINE_UPDATED_MESSAGE,
            EVENT_PRODUCT_RECOVERED, PRODUCT_RECOVERED_MESSAGE
        );

        fieldsMap = Map.of(
            EVENT_LABEL_APPLIED, LabelAppliedMessage.class,
            EVENT_SHIPMENT_COMPLETED, ShipmentCompletedMessage.class,
            EVENT_PRODUCT_DISCARDED, ProductDiscardedMessage.class,
            EVENT_PRODUCT_QUARANTINED, AddQuarantinedMessage.class,
            EVENT_QUARANTINE_REMOVED, RemoveQuarantinedMessage.class,
            EVENT_QUARANTINE_UPDATED, UpdateQuarantinedMessage.class,
            EVENT_PRODUCT_RECOVERED, ProductRecoveredMessage.class,
            EVENT_PRODUCT_STORED, ProductStoredMessage.class
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
        String reason = null;
        String comment = null;

        InventoryStatus status = statusParam;

        if (topicName.equals(quarantineRemovedTopic) || topicName.equals(quarantineUpdatedTopic)) {
            quarantines = List.of(new Quarantine(1L, "OTHER", "a comment"));
            histories = List.of(new History(InventoryStatus.AVAILABLE, null, null));
            status = InventoryStatus.QUARANTINED;
        }

        if (topicName.equals(productRecoveredTopic)) {
            histories = List.of(new History(InventoryStatus.AVAILABLE, null, null));
            reason = "EXPIRED";
            comment = "Some comments";
            status = InventoryStatus.DISCARDED;
        }

        return inventoryEntityRepository.save(InventoryEntity.builder()
            .id(UUID.randomUUID())
            .productFamily(productFamily)
            .aboRh(aboRhType)
            .location(location)
            .collectionDate(ZonedDateTime.now())
            .inventoryStatus(status)
            .expirationDate(LocalDateTime.now().plusDays(daysToExpire))
            .unitNumber(unitNumber)
            .productCode(productCode)
            .quarantines(quarantines)
            .histories(histories)
            .shortDescription("Short description")
            .comments(comment)
            .statusReason(reason)
            .build()).block();

    }

    @When("I receive an event {string} event")
    public void iReceiveAnEvent(String event) throws Exception {
        scenarioContext.setProductCode("E0869VA0");
        var message = buildMessage(event);
        scenarioContext.setLastSentMessage(message);
        testUtils.kafkaSender(
            scenarioContext.getUnitNumber() + "-" + scenarioContext.getProductCode(),
            message,
            topicName);

        logMonitor.await("successfully consumed.*"+scenarioContext.getUnitNumber());
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
            scenarioContext.getUnitNumber() + "-" + scenarioContext.getProductCode().replaceAll("V", ""),
            buildMessage(event, unitNumber, productCode, location),
            topicName);
        logMonitor.await("successfully consumed.*"+scenarioContext.getUnitNumber());
    }

    @And("the expected fields for {string} are present")
    public void theExpectedFieldsForArePresent(String event) throws IllegalAccessException {
        String message = scenarioContext.getLastSentMessage();
        Assert.assertNotNull("No message found in the scenario context", message);

        Class<?> dtoClass = fieldsMap.get(event);
        Assert.assertNotNull("No DTO class found for event: " + event, dtoClass);

        JavaType eventMessageType = objectMapper.getTypeFactory()
            .constructParametricType(EventMessage.class, dtoClass);

        EventMessage<?> eventMessage;
        try {
            eventMessage = objectMapper.readValue(message, eventMessageType);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse message into EventMessage<" + dtoClass.getSimpleName() + ">", e);
        }

        Object payload = eventMessage.payload();
        Assert.assertNotNull("Payload is null in the event message", payload);
        assertThat(payload).hasNoNullFieldsOrProperties();
    }
}
