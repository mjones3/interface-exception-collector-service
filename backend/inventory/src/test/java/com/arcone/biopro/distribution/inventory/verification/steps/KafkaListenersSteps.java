package com.arcone.biopro.distribution.inventory.verification.steps;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.InventoryEntity;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.InventoryEntityRepository;
import com.arcone.biopro.distribution.inventory.verification.utils.TestUtils;
import io.cucumber.java.Before;
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

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class KafkaListenersSteps {

    @Value("${topic.label-applied.name}")
    private String labelAppliedTopic;

    @Value("${topic.shipment-completed.name}")
    private String shipmentCompletedTopic;

    private static final String SHIPMENT_COMPLETED_MESSAGE = """
         {
           "eventType":"ShipmentCompleted",
           "eventVersion":"1.0",
           "payload" : {
                "shipmentId":2,
                "orderNumber":1,
                "unitNumber":"W036898786800",
                "productCode":"E7644V00",
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
               "unitNumber":"W123452622168",
               "productCode":"E0869VA0",
               "shortDescription":"APH PLASMA 24H",
               "location":"MIAMI",
               "aboRh":"OP",
               "productFamily": "PLASMA_TRANSFUSABLE",
               "collectionDate":"2025-01-08T06:00:00.000Z",
               "expirationDate":"2025-01-08T06:00:00.000",
               "performedBy":"userId",
               "createDate":"2025-01-08T06:00:00.000Z"
            }
         }
        """;
    private static final String SHIPMENT_COMPLETED = "Shipment Completed";

    private static final String EVENT_LABEL_APPLIED = "Label Applied";

    private Map<String, String> topicsMap;

    private Map<String, String> messagesMap;

    private final CountDownLatch waiter = new CountDownLatch(1);

    private String topicName;

    @Before
    public void before() {
        populateTestData();
        topicsMap = Map.of(
            EVENT_LABEL_APPLIED, labelAppliedTopic,
            SHIPMENT_COMPLETED, shipmentCompletedTopic);

        messagesMap = Map.of(
            EVENT_LABEL_APPLIED, LABEL_APPLIED_MESSAGE,
            SHIPMENT_COMPLETED, SHIPMENT_COMPLETED_MESSAGE);
    }

    @Given("I am listening the {string} event")
    public void iAmListeningEvent(String event) {
        topicName = topicsMap.get(event);
    }

    @When("I receive an event {string} event")
    public void iReceiveAnEvent(String event) throws Exception {
        testUtils.kafkaSender(
            "W123452622168-E0869VA0",
            messagesMap.get(event),
            topicName);
    }

    @Then("The inventory status is {string}")
    public void theInventoryIsCreatedCorrectly(String status) throws InterruptedException {
        int maxTryCount = 60;
        int tryCount = 0;

        InventoryEntity entity = null;
        switch (topicName) {
            case EVENT_LABEL_APPLIED: {
                while (tryCount < maxTryCount && entity == null) {
                    entity = inventoryEntityRepository.findByUnitNumberAndProductCode("W123452622168", "E0869VA0").block();

                    tryCount++;
                    waiter.await(1, TimeUnit.SECONDS);
                }

                assertNotNull(entity);
                assertEquals("E0869VA0", entity.getProductCode());
                assertEquals("W123452622168", entity.getUnitNumber());
                assertEquals(status, entity.getInventoryStatus().name());
            }
            case SHIPMENT_COMPLETED: {
                while (tryCount < maxTryCount && entity == null) {
                    entity = inventoryEntityRepository.findByUnitNumberAndProductCode("W036898786800", "E0869VA0").block();
                    assertNotNull(entity);
                    assertNotEquals(entity.getInventoryStatus(), InventoryStatus.SHIPPED);
                    tryCount++;
                    waiter.await(1, TimeUnit.SECONDS);
                }

                assertEquals("E0869VA0", entity.getProductCode());
                assertEquals("W123452622168", entity.getUnitNumber());
                assertEquals(status, entity.getInventoryStatus().name());
            }
        }

    }

    public void populateTestData() {
        ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator();
        resourceDatabasePopulator.addScript(testDataSql);
        Mono.from(resourceDatabasePopulator.populate(connectionFactory)).block();
    }
}
