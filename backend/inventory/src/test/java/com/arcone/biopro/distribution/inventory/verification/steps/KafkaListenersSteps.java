package com.arcone.biopro.distribution.inventory.verification.steps;

import com.arcone.biopro.distribution.inventory.infrastructure.persistence.InventoryEntity;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.InventoryEntityRepository;
import com.arcone.biopro.distribution.inventory.verification.utils.TestUtils;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class KafkaListenersSteps {

    @Value("${topic.label-applied.name}")
    private String labelAppliedTopic;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private InventoryEntityRepository inventoryEntityRepository;

    private static final String LABEL_APPLIED_MESSAGE = """
         {
            "unitNumber": "W123452622168",
            "productCode": "E0869VA0",
            "productDescription": "APH PLASMA 24H",
            "expirationDate": "2024-12-31T00:10:00Z",
            "collectionDate": "2024-12-30T00:10:00Z",
            "location": "ORLANDO",
            "productFamily": "PLASMA_TRANSFUSABLE",
            "aboRh": "OP",
            "performedBy": "USER_ID",
            "createDate": "2024-07-01T00:10:00Z"
        }
        """;

    private static final String EVENT_LABEL_APPLIED = "Label Applied";

    private Map<String, String> topicsMap;

    private Map<String, String> messagesMap;

    private final CountDownLatch waiter = new CountDownLatch(1);

    private String topicName;

    @Before
    public void before() {
        topicsMap = Map.of(
            EVENT_LABEL_APPLIED, labelAppliedTopic);

        messagesMap = Map.of(
            EVENT_LABEL_APPLIED, LABEL_APPLIED_MESSAGE);
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



}
